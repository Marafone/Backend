package com.marafone.marafone.user;
import com.marafone.marafone.user.dto.UserRankingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    String ORDER_DESCENDING_BY_WIN_RATIO =
            """
            ORDER BY
                CASE WHEN (u.wins + u.losses) = 0 THEN 0
                ELSE (u.wins * 1.0 / (u.wins + u.losses)) END
            DESC
            """;

    Optional<User> findByUsername(String username);

    @Query("SELECT new com.marafone.marafone.user.dto.UserRankingDTO(DENSE_RANK() OVER ("
            + ORDER_DESCENDING_BY_WIN_RATIO + """
            ) AS position,
            u.username,
            u.wins,
            u.losses)
            FROM User u
            """)
    Page<UserRankingDTO> getTopUsers(Pageable pageable);

    @Query("""
            WITH RankedUsers AS (
                SELECT
                    DENSE_RANK() OVER ("""
                        + ORDER_DESCENDING_BY_WIN_RATIO + """
                    ) AS position,
                    u.username AS username,
                    u.wins AS wins,
                    u.losses AS losses
                FROM User u
            )
            SELECT new com.marafone.marafone.user.dto.UserRankingDTO(ru.position, ru.username, ru.wins, ru.losses)
            FROM RankedUsers ru
            WHERE ru.username = :username
            """)
    UserRankingDTO getTopUser(@Param("username") String username);

    @Query("""
            WITH RankedUsers AS (
                SELECT
                    u.username AS username,
                    ROW_NUMBER() OVER ("""
                        + ORDER_DESCENDING_BY_WIN_RATIO + """
                    ) AS position
                FROM User u
            )
            SELECT position FROM RankedUsers WHERE username = :username
            """)
    Optional<Integer> getUserRankingPosition(@Param("username") String username);
}
