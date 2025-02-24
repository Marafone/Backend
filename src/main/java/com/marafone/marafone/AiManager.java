package com.marafone.marafone;

import com.marafone.marafone.auth.AuthService;
import com.marafone.marafone.auth.model.RegisterRequest;
import com.marafone.marafone.user.User;
import com.marafone.marafone.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
public class AiManager {

    private final UserRepository userRepo;
    private static final Queue<User> availableAIs = new ConcurrentLinkedQueue<>();

    @Bean
    public CommandLineRunner initUsers(AuthService authService) {
        return args -> {
            List<String> names = List.of("John", "Emma", "Liam", "Olivia", "Noah", "Ava", "James", "Sophia", "Logan", "Isabella", "Mason", "Mia", "Ethan", "Charlotte", "Alexander", "Amelia", "Henry", "Harper", "Jacob", "Evelyn");

            IntStream.range(1, 9).forEach(i -> {
                RegisterRequest rr = new RegisterRequest();
                rr.username = "user" + i;
                rr.password = "user" + i;
                User user = authService.register(rr);
                user.setWins((int) (Math.random() * 101));
                user.setLosses((int) (Math.random() * 50));
                userRepo.save(user);
            });

            IntStream.range(0, 100).forEach(i -> {
                RegisterRequest rr = new RegisterRequest();
                rr.username = "AI_" + names.get(i % names.size()) + i;
                rr.password = "ai_password";
                User aiUser = authService.register(rr);
                userRepo.save(aiUser);
                availableAIs.add(aiUser);
            });

            System.out.println("Default and AI users created.");
        };
    }

    public static User getAvailableAI() {
        return availableAIs.poll();
    }

    public static void releaseAI(User aiUser) {
        availableAIs.offer(aiUser);
    }
}
