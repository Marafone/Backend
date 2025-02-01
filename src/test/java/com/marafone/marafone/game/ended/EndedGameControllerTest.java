package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EndedGameController.class)
class EndedGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EndedGameService endedGameService;


    @Test
    @WithMockUser(username = "testUser")
    void getEndedGame_WhenGameDoesNotExist_ShouldReturnNotFoundResponse() throws Exception {
        // given
        Mockito.when(endedGameService.getEndedGameById(1L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/game/1/ended")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "testUser")
    void getPlayerEndedGames_WhenNoGamesExist_ShouldReturnNotFoundResponse() throws Exception {
        // given
        Mockito.when(endedGameService.getPlayerEndedGames("testUser")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/games/ended/player")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}