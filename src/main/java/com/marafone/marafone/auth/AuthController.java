package com.marafone.marafone.auth;

import com.marafone.marafone.auth.model.LoginRequest;
import com.marafone.marafone.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;
import com.marafone.marafone.auth.model.RegisterRequest;

import java.util.Optional;

//TODO map user to userDto to hide password

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try{
            authService.login(loginRequest, request, response);
        }catch (AuthenticationException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest registerRequest){
        Optional<User> userOptional = authService.findUserByUsername(registerRequest.email);

        if(userOptional.isEmpty()){
            User user = authService.register(registerRequest);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

}
