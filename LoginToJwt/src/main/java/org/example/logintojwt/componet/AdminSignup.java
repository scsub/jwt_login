package org.example.logintojwt.componet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.Role;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.properties.AdminProperties;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSignup implements CommandLineRunner {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AdminProperties adminProperties;
    @Override
    public void run(String... args) throws Exception {
        String adminUsername = adminProperties.getUsername();
        String adminPassword = adminProperties.getPassword();

        if(userRepository.findByUsername(adminUsername).isPresent()){
            log.info("관리자가 이미 존재합니다");
        }else{
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .email("admin")
                    .phoneNumber("admin")
                    .address("admin")
                    .roles(List.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                    .build();
            userRepository.save(admin);
            log.info("관리자 생성완료");
        }
    }
}
