package tournament_trail.demo.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelRequest;
import tournament_trail.demo.entities.enums.TravelGroupStatus;
import tournament_trail.demo.entities.enums.TravelRequestStatus;
import tournament_trail.demo.exceptions.AlreadyPartOfGroupException;
import tournament_trail.demo.exceptions.RequestAlreadyExistsException;
import tournament_trail.demo.exceptions.TravelGroupFullException;
import tournament_trail.demo.repositories.TravelRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TravelRequestService {
    private final TravelRequestRepository travelRequestRepository;
    private final UserService userService;

    public TravelRequestService(TravelRequestRepository travelRequestRepository, UserService userService) {
        this.travelRequestRepository = travelRequestRepository;
        this.userService = userService;
    }

    public List<TravelRequest> findAcceptedRequests(UUID userId) {
        return travelRequestRepository.findAllByApplicantIdAndStatus(userId, TravelRequestStatus.APPROVED);
    }

    @Transactional
    public void createTravelRequest(TravelGroup travelGroup, UUID userId, String travelRequestMessage) {

        if (travelGroup.getOwner().getId().equals(userId)) {
            throw new AlreadyPartOfGroupException();
        }
        if (travelGroup.getStatus() != TravelGroupStatus.OPEN) {
            throw new AccessDeniedException("You can not enter this travel group");
        }
        if (countAvailableSpots(travelGroup) <= 0) {
            throw new TravelGroupFullException();
        }
        if (doesRequestExist(travelGroup.getId(), userId)) {
            throw new RequestAlreadyExistsException();
        }


        TravelRequest.TravelRequestBuilder builder = TravelRequest.builder()
                .travelGroup(travelGroup)
                .applicant(userService.findById(userId))
                .status(TravelRequestStatus.PENDING)
                .requestedOn(LocalDateTime.now())
                .respondedOn(null);
        if (travelRequestMessage != null && !travelRequestMessage.isBlank()) {
            builder.message(travelRequestMessage.trim());
        }

        travelRequestRepository.save(builder.build());

    }

    public int countAvailableSpots(TravelGroup travelGroup) {
        int maximumMembers = travelGroup.getMaximumMembers();
        int confirmedRequests = countAllConfirmedRequests(travelGroup.getId(), TravelRequestStatus.APPROVED);
        return maximumMembers - confirmedRequests - 1;
    }

    public boolean hasRequestFromUser(UUID travelGroupId, UUID userId) {
        return travelRequestRepository.existsByTravelGroupIdAndApplicantId(travelGroupId, userId);
    }

    private int countAllConfirmedRequests(UUID travelGroupId, TravelRequestStatus travelRequestStatus) {
        return travelRequestRepository.countByTravelGroupIdAndStatus(travelGroupId, travelRequestStatus);
    }

    private boolean doesRequestExist(UUID travelGroupId, UUID userId) {
        return travelRequestRepository.existsByTravelGroupIdAndApplicantId(travelGroupId, userId);

    }

    public List<TravelRequest> getAllPendingRequests(UUID travelGroupId, TravelRequestStatus status) {
        return travelRequestRepository.findAllByTravelGroupIdAndStatusOrderByRequestedOnDesc(travelGroupId, status);
    }

    @Transactional
    public void acceptTravelRequest(UUID travelGroupId, UUID travelRequestId, UUID userId, int availableSpots) {

        TravelRequest travelRequest = findByIdAndGroupId(travelRequestId, travelGroupId);
        if (!travelRequest.getTravelGroup().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to manage this group");
        }
        if(travelRequest.getTravelGroup().getStatus() != TravelGroupStatus.OPEN){
            throw new AccessDeniedException("Travel requests can only be accepted by travel groups that are open");
        }

        if (travelRequest.getStatus() != TravelRequestStatus.PENDING) {
            throw new AccessDeniedException("Only Pending requests can be approved");
        }
        if(availableSpots<=0){
            throw new TravelGroupFullException();
        }
        travelRequest.setStatus(TravelRequestStatus.APPROVED);
        travelRequest.setRespondedOn(LocalDateTime.now());
        travelRequestRepository.save(travelRequest);
    }

    @Transactional
    public void rejectTravelRequest(UUID travelGroupId, UUID travelRequestId, UUID userId) {
        TravelRequest travelRequest = findByIdAndGroupId(travelRequestId, travelGroupId);
        if (!travelRequest.getTravelGroup().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to view this information");
        }

        if (travelRequest.getStatus() != TravelRequestStatus.PENDING) {
            throw new AccessDeniedException("Only Pending registrations can be rejected");
        }

        travelRequest.setStatus(TravelRequestStatus.REJECTED);
        travelRequest.setRespondedOn(LocalDateTime.now());
        travelRequestRepository.save(travelRequest);
    }
    private TravelRequest findByIdAndGroupId(UUID requestId, UUID travelGroupId){
        return travelRequestRepository.findByIdAndTravelGroupId(requestId, travelGroupId).orElseThrow();
    }
}
