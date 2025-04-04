package com.marafone.marafone.user;

import com.marafone.marafone.game.model.Team;
import com.marafone.marafone.user.dto.RankingPageDTO;
import com.marafone.marafone.user.dto.UserRankingDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserRankingDTO> getUsersRanking(Pageable pageable);
    UserRankingDTO getUserRanking(String username);
    Optional<RankingPageDTO> getRankingPageForUser(String username, int pageSize);
    void updateUsersStats(List<User> teamRed, List<User> teamBlue, Team winnerTeam);
}