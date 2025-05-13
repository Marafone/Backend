package com.marafone.marafone;

import com.marafone.ai.TrainingLoop;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"com.marafone.marafone", "com.marafone.ai"})
public class MarafoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarafoneApplication.class, args);
	}
	/*
	// UNCOMMENT TO TRAIN THE AI ANEW
	@Bean
	CommandLineRunner runTrainingLoop(ApplicationContext context) {
		return args -> {
			TrainingLoop trainingLoop = context.getBean(TrainingLoop.class);
			trainingLoop.runTraining(1000); // Run training for 1000 episodes
		};
	}
	*/
}
