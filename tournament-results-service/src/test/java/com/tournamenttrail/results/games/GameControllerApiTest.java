package com.tournamenttrail.results.games;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournamenttrail.results.fixtures.GameFixture;
import com.tournamenttrail.results.fixtures.GameRequestFixture;
import com.tournamenttrail.results.fixtures.StandingResponseFixture;
import com.tournamenttrail.results.games.dtos.GameRequest;
import com.tournamenttrail.results.games.dtos.GameResponse;
import com.tournamenttrail.results.games.dtos.StandingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
    }

    @Test
    void getGamesByTournament_whenNoGames_shouldReturnEmptyList() throws Exception {
        UUID tournamentId = UUID.randomUUID();

        mockMvc.perform(get("/api/tournaments/{tournamentId}/games", tournamentId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void getGamesByTournament_whenThereAreGames_shouldReturnList() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);

        MvcResult result = mockMvc.perform(
                        get("/api/tournaments/{tournamentId}/games", game.getTournamentId()))
                .andExpect(status().isOk())
                .andReturn();

        GameResponse[] responses = objectMapper.readValue(result.getResponse().getContentAsString(),
                GameResponse[].class);

        assertThat(responses).hasSize(1);

        assertThat(responses[0])
                .usingRecursiveComparison()
                .ignoringFields("playedOn", "createdOn", "updatedOn", "whitePoints", "blackPoints")
                .isEqualTo(game);

        assertNotNull(responses[0].getPlayedOn());
        assertNotNull(responses[0].getCreatedOn());
        assertNotNull(responses[0].getUpdatedOn());
    }


    @Test
    void createGame_whenValidRequest_shouldCreateGame() throws Exception {
        UUID tournamentId = UUID.randomUUID();

        GameRequest request = GameRequestFixture.create();

        MvcResult mvcResult = mockMvc.perform(
                        post("/api/tournaments/{tournamentId}/games", tournamentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        GameResponse response =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(), GameResponse.class);

        assertThat(response)
                .usingRecursiveComparison()
                .ignoringFields(
                        "playedOn"
                        , "createdOn"
                        , "updatedOn"
                        , "whitePoints"
                        , "blackPoints"
                        , "tournamentId"
                        , "id")
                .isEqualTo(request);
        assertNotNull(response.getCreatedOn());
        assertNotNull(response.getUpdatedOn());
        assertNotNull(response.getPlayedOn());
        assertEquals(BigDecimal.ZERO, response.getWhitePoints());
        assertEquals(BigDecimal.ONE, response.getBlackPoints());

        List<Game> games = gameRepository.findAll();
        assertEquals(1, games.size());

        assertEquals(response.getId(), games.get(0).getId());
    }

    @Test
    public void createGame_whenInvalidData_shouldReturn400() throws Exception {
        GameRequest gameRequest = GameRequestFixture.createInvalid();
        UUID tournamentId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/tournaments/{tournamentId}/games", tournamentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors.roundNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.boardNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.totalMoves").exists())
                .andExpect(jsonPath("$.fieldErrors.whitePlayerUsername").exists())
                .andExpect(jsonPath("$.fieldErrors.blackPlayerUsername").exists());

        assertEquals(0, gameRepository.count());
    }

    @Test
    public void updateGame_whenValidData_shouldUpdateGame() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);

        GameRequest gameRequest = GameRequestFixture.createUpdated();

        mockMvc.perform(
                        put("/api/tournaments/{tournamentId}/games/{gameId}"
                                , game.getTournamentId()
                                , game.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().isOk());

        List<Game> games = gameRepository.findAll();

        assertEquals(1, games.size());

        Game result = games.get(0);

        assertEquals(game.getId(), result.getId());
        assertThat(gameRequest).usingRecursiveComparison()
                .ignoringFields(
                        "playedOn"
                        , "createdOn"
                        , "updatedOn"
                        , "whitePoints"
                        , "blackPoints"
                        , "tournamentId"
                        , "id")
                .isEqualTo(result);
    }

    @Test
    public void updateGame_whenInvalidData_shouldNotUpdateGameAndReturn400status() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);

        GameRequest gameRequest = GameRequestFixture.createInvalid();

        mockMvc.perform(
                        put("/api/tournaments/{tournamentId}/games/{gameId}"
                                , game.getTournamentId()
                                , game.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(gameRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.fieldErrors.roundNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.boardNumber").exists())
                .andExpect(jsonPath("$.fieldErrors.totalMoves").exists())
                .andExpect(jsonPath("$.fieldErrors.whitePlayerUsername").exists())
                .andExpect(jsonPath("$.fieldErrors.blackPlayerUsername").exists());

        Game result = gameRepository.findById(game.getId()).orElseThrow();

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields(
                        "playedOn"
                        , "createdOn"
                        , "updatedOn"
                        , "whitePoints"
                        , "blackPoints")
                .isEqualTo(game);

        assertThat(result.getWhitePoints()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getBlackPoints()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    public void getStandings_shouldReturnStandingsAndStatusOk() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);
        List<StandingResponse> list = StandingResponseFixture.createList(game);

        MvcResult mvcResult = mockMvc.perform(get("/api/tournaments/{tournamentId}/standings", game.getTournamentId()))
                .andExpect(status().isOk())
                .andReturn();
        StandingResponse[] standingResponses = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), StandingResponse[].class);

        assertThat(standingResponses[0]).usingRecursiveComparison().isEqualTo(list.get(0));
        assertThat(standingResponses[1]).usingRecursiveComparison().isEqualTo(list.get(1));
    }

    @Test
    public void deleteGame_shouldDeleteGameAndReturnStatus() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);

        mockMvc.perform(delete("/api/tournaments/{tournamentId}/games/{gameId}"
                , game.getTournamentId(), game.getId()))
                .andExpect(status().isNoContent());

        assertFalse(gameRepository.existsById(game.getId()));
    }

    @Test
    public void deleteGame_whenInvalidGameId_shouldReturnStatus404() throws Exception {
        Game game = GameFixture.create();
        gameRepository.save(game);
        mockMvc.perform(delete("/api/tournaments/{tournamentId}/games/{gameId}"
                , game.getTournamentId(), UUID.randomUUID()))
                .andExpect(status().isNotFound());

        assertEquals(1, gameRepository.count());
    }
}