package com.authentication;

import com.authentication.cdlauthentication.domain.model.AppUser;
import com.authentication.cdlauthentication.domain.model.Role;
import com.authentication.cdlauthentication.domain.repository.AppUserRepository;
import com.authentication.cdlauthentication.domain.repository.RoleRepository;
import com.authentication.loan_mandate.service.MandateService;
import com.authentication.loan_mandate.service.MandateServiceImpl;
import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CdlAuthenticationApplication implements AsyncConfigurer {
	@Autowired
	private RoleRepository roleRepository;

	@Value( "${CDL_FCMB_LDAP_BASE_USERNAME}" )
	private String username;

	@Value( "${CDL_FCMB_LDAP_BASE_PASSWORD}" )
	private String password;

	public static void main(String[] args) {

		SpringApplication.run(CdlAuthenticationApplication.class, args);

//		// Set the schedule to run the task every 60 seconds (60, TimeUnit.SECONDS)
//		executor.scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS);

	}

	@Bean
	CommandLineRunner commandLineRunner(AppUserRepository users, PasswordEncoder encoder) {
		Role admin = new Role("Admin");
		Role user = new Role("User");

		if(!roleRepository.existsByName(admin.getName())){
			roleRepository.save(admin);
			roleRepository.save(user);
		}

		return args -> {
			if(!users.existsByUsername(username)) {
				Role newRole = roleRepository.findByName(admin.getName()).orElseThrow(
						()->new IllegalArgumentException("No role found")
				);
				Set<Role> role = new HashSet<>();
				role.add(newRole);

				users.save(new AppUser(username, encoder.encode(password), role)
				);
			}
		};
	}



}
