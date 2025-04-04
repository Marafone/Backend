package com.marafone.marafone.user;

import com.marafone.marafone.user.dto.RankingPageDTO;
import com.marafone.marafone.user.dto.UserRankingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/info")
    public ResponseEntity<?> getUsername(Principal principal) {
        if (principal != null)
            return ResponseEntity.ok(Map.of("username", principal.getName()));
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }

    @GetMapping("/users/ranking")
    public List<UserRankingDTO> getUsersRanking(Pageable pageable) {
        return userService.getUsersRanking(pageable);
    }

    @GetMapping("/user/ranking")
    public UserRankingDTO getUserRanking(Principal principal) {
        return userService.getUserRanking(principal.getName());
    }

    @GetMapping("/users/{username}/ranking")
    public ResponseEntity<RankingPageDTO> getRankingPageForUser(
            @PathVariable("username") String username,
            @RequestParam("pageSize") int pageSize
    ) {
        Optional<RankingPageDTO> rankingPageOptional = userService.getRankingPageForUser(username, pageSize);
        return rankingPageOptional.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

}
