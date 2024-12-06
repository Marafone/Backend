package com.marafone.marafone.auth;
import com.marafone.marafone.auth.model.LoginRequest;
import com.marafone.marafone.auth.model.RegisterRequest;
import com.marafone.marafone.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
public interface AuthService {
    void login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);
    User register(RegisterRequest registerRequest);
    Optional<User> findUserByUsername(String username);
}
