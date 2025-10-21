package org.example.logintojwt.debug;

import lombok.RequiredArgsConstructor;
import org.example.logintojwt.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {
    private final UserRepository userRepository;
    @GetMapping("/db")
    public Map<String ,Long> checkDB(){
        Long count = userRepository.count();
        return Map.of("checkDB", count);
    }
}
