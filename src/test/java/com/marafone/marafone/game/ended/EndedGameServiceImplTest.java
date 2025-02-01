package com.marafone.marafone.game.ended;

import com.marafone.marafone.game.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EndedGameServiceImplTest {

    private EndedGameServiceImpl endedGameService;

    @Mock
    private EndedGameRepository endedGameRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        endedGameService = new EndedGameServiceImpl(endedGameRepository);
    }

    @Test
    void getEndedGameById_WhenGameExists_ShouldReturnGame() {
        // given
        Game game = new Game();
        game.setId(1L);
        when(endedGameRepository.findById(1L)).thenReturn(Optional.of(game));

        // when
        Optional<Game> result = endedGameService.getEndedGameById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(game);
        verify(endedGameRepository, times(1)).findById(1L);
    }

    @Test
    void getEndedGameById_WhenGameDoesNotExist_ShouldReturnEmpty() {
        // given
        when(endedGameRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<Game> result = endedGameService.getEndedGameById(1L);

        // then
        assertThat(result).isEmpty();
        verify(endedGameRepository, times(1)).findById(1L);
    }

    @Test
    void saveEndedGame_ShouldSaveAndReturnGame() {
        // given
        Game game = new Game();
        when(endedGameRepository.save(any(Game.class))).thenReturn(game);

        // when
        Game result = endedGameService.saveEndedGame(game);

        // then
        assertThat(result).isEqualTo(game);
        verify(endedGameRepository, times(1)).save(game);
    }

    @Test
    void getPlayerEndedGames_WhenGamesExist_ShouldReturnListOfGames() {
        // given
        Game game1 = new Game();
        Game game2 = new Game();
        List<Game> games = Arrays.asList(game1, game2);
        when(endedGameRepository.findAllByPlayerName("testUser")).thenReturn(games);

        // when
        List<Game> result = endedGameService.getPlayerEndedGames("testUser");

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(game1, game2);
        verify(endedGameRepository, times(1)).findAllByPlayerName("testUser");
    }

    @Test
    void getPlayerEndedGames_WhenNoGamesExist_ShouldReturnEmptyList() {
        // given
        when(endedGameRepository.findAllByPlayerName("testUser")).thenReturn(List.of());

        // when
        List<Game> result = endedGameService.getPlayerEndedGames("testUser");

        // then
        assertThat(result).isEmpty();
        verify(endedGameRepository, times(1)).findAllByPlayerName("testUser");
    }
}