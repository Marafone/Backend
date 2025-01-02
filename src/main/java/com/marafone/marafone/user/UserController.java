package com.marafone.marafone.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<?> getUsername(Principal principal) {
        if (principal != null)
            return ResponseEntity.ok(Map.of("username", principal.getName()));
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
    }

}
