package tournament_trail.demo.web.dtos;

import tournament_trail.demo.entities.TravelGroup;
import tournament_trail.demo.entities.TravelGroupComment;

import java.util.List;

public record TravelGroupCommentsPageData(TravelGroup travelGroup, List<TravelGroupComment> comments
        , int countVisibleComments) {

}