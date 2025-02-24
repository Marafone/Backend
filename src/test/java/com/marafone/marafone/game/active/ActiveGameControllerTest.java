package com.marafone.marafone.game.active;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marafone.marafone.game.ended.EndedGameService;
import com.marafone.marafone.game.event.incoming.CreateGameRequest;
import com.marafone.marafone.game.model.GameType;
import com.marafone.marafone.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

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
    void createGame_WhenUserInAnotherGame_ShouldReturnBadRequestInfo() throws Exception {
        // given
        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getUsername()).thenReturn("testUser");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(mockedUser, null, List.of());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        CreateGameRequest createGameRequest =
                new CreateGameRequest("game", GameType.MARAFFA, "", 21);

        Mockito.when(activeGameService.doesNotStartedGameAlreadyExist(Mockito.any())).thenReturn(false);
        Mockito.when(mockedUser.isInGame()).thenReturn(true);
        String jsonRequest = new ObjectMapper().writeValueAsString(createGameRequest);

        // when & then
        mockMvc.perform(post("/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf())) // to include csrf token for authentication
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGame_WhenGameDoesNotExist_ShouldReturnOkResponseInfo() throws Exception {
        // given
        User mockedUser = Mockito.mock(User.class);
        Mockito.when(mockedUser.getUsername()).thenReturn("testUser");
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(mockedUser, null, List.of());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        CreateGameRequest createGameRequest =
                new CreateGameRequest("game", GameType.MARAFFA, "", 21);

        Mockito.when(activeGameService.doesNotStartedGameAlreadyExist(Mockito.any())).thenReturn(false);
        Mockito.when(activeGameService.getActiveGameForPlayer(Mockito.any())).thenReturn(Optional.empty());
        String jsonRequest = new ObjectMapper().writeValueAsString(createGameRequest);

        // when & then
        mockMvc.perform(post("/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(csrf())) // to include csrf token for authentication
                .andExpect(status().isOk());
    }
}
