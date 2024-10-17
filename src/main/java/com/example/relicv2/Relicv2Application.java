package com.example.relicv2;

import com.example.relicv2.util.AdminUserSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Relicv2Application implements CommandLineRunner {
	private final AdminUserSeeder adminUserSeeder;

	public Relicv2Application(AdminUserSeeder adminUserSeeder) {
		this.adminUserSeeder = adminUserSeeder;
	}

	public static void main(String[] args) {
		SpringApplication.run(Relicv2Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		adminUserSeeder.seed();
	}
}
