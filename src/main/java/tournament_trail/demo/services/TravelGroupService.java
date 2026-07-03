package tournament_trail.demo.services;
import org.springframework.stereotype.Service;
import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.repositories.TravelGroupRepository;

import java.util.List;
import java.util.UUID;

@Service
public class TravelGroupService {
    private final TravelGroupRepository travelGroupRepository;

    public TravelGroupService(TravelGroupRepository travelGroupRepository) {
        this.travelGroupRepository = travelGroupRepository;
    }

    public List<TravelGroup> getTravelGroupsByUser(UUID ownerId){
        return travelGroupRepository.findAllByOwnerIdOrderByDepartureTimeAsc(ownerId);
    }
}
