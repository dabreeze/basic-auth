package com.cdl.basicauth;

import com.cdl.basicauth.data.model.AppUser;
import com.cdl.basicauth.data.repo.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BasicAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasicAuthApplication.class, args);
	}

//	@Bean
//	CommandLineRunner commandLineRunner(AppUserRepository users, PasswordEncoder encoder) {
//		return args -> {
//			users.save(new AppUser("user",encoder.encode("password"),"ROLE_USER"));
//			users.save(new AppUser("admin",encoder.encode("password"),"ROLE_USER,ROLE_ADMIN"));
//		};
//	}
}
