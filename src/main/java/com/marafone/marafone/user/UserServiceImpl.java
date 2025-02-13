package com.marafone.marafone.user;

import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.user.dto.RankingPageDTO;
import com.marafone.marafone.user.dto.UserRankingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public List<UserRankingDTO> getUsersRanking(Pageable pageable) {
        return userRepository.getTopUsers(pageable).getContent();
    }

    @Override
    public UserRankingDTO getUserRanking(String username) {
        return userRepository.getTopUser(username);
    }

    @Override
    public Optional<RankingPageDTO> getRankingPageForUser(String username, int pageSize) {
        Optional<Integer> userRankingPositionOptional = userRepository.getUserRankingPosition(username);

        if (userRankingPositionOptional.isEmpty())
            return Optional.empty();

        Integer userRankingPosition = userRankingPositionOptional.get();

        int pageNumber = calculatePageNumber(userRankingPosition, pageSize);
        List<UserRankingDTO> userRankingDTOS = getUsersRanking(PageRequest.of(pageNumber, pageSize));

        return Optional.of(new RankingPageDTO(userRankingDTOS, pageNumber));
    }

    public void updateUsersStats(List<User> teamRed, List<User> teamBlue, Team winnerTeam) {
        List<User> winningTeam = (winnerTeam == Team.RED) ? teamRed : teamBlue;
        List<User> losingTeam = (winnerTeam == Team.RED) ? teamBlue : teamRed;

        for (var user: winningTeam)
            user.increaseWins();

        for (var user: losingTeam)
            user.increaseLosses();

        userRepository.saveAll(winningTeam);
        userRepository.saveAll(losingTeam);
    }

    private int calculatePageNumber(int position, int pageSize) {
        return ((position - 1) / pageSize);
    }
}
