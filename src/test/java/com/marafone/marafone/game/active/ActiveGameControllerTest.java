package com.marafone.marafone.game.active;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.model.GameType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActiveGameController.class)
class ActiveGameControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActiveGameService activeGameService;

    @MockBean
    private EndedGameService endedGameService;

    @MockBean
    private ActiveGameRepository activeGameRepository;

    @Test
    @WithMockUser(username = "testUser" )
    void createGame_WhenGameAlreadyExists_ShouldReturnBadRequestInfo() throws Exception {
        // given
        CreateGameRequest createGameRequest =
                new CreateGameRequest("game", GameType.MARAFFA, "", 21);

        Mockito.when(activeGameService.doesNotStartedGameAlreadyExist(Mockito.any())).thenReturn(true);
        String jsonRequest = new ObjectMapper().writeValueAsString(createGameRequest);

        // when & then
        mockMvc.perform(post("/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf())) // to include csrf token for authentication
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testUser" )
    void createGame_WhenGameDoesNotExist_ShouldReturnOkResponseInfo() throws Exception {
        // given
        CreateGameRequest createGameRequest =
                new CreateGameRequest("game", GameType.MARAFFA, "", 21);

        Mockito.when(activeGameService.doesNotStartedGameAlreadyExist(Mockito.any())).thenReturn(false);
        String jsonRequest = new ObjectMapper().writeValueAsString(createGameRequest);

        // when & then
        mockMvc.perform(post("/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf())) // to include csrf token for authentication
                .andExpect(status().isOk());
    }
}
