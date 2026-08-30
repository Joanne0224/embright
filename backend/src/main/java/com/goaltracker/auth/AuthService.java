package com.goaltracker.auth;

import com.goaltracker.dto.AuthResponse;
import com.goaltracker.dto.LoginRequest;
import com.goaltracker.dto.RegisterRequest;
import com.goaltracker.entity.User;
import com.goaltracker.repository.UserRepository;
import com.goaltracker.service.OnboardingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final OnboardingService onboardingService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenService tokenService, OnboardingService onboardingService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.onboardingService = onboardingService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("這個帳號已經被註冊過了,換一個試試看");
        }

        User user = new User();
        user.setUsername(request.username());
        // encode() 用 BCrypt 把明文密碼變成雜湊值,存進資料庫的是這個雜湊值,不是原始密碼
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        // 新帳號自動塞一些範例資料,避免第一次打開整個空蕩蕩不知道從何開始
        onboardingService.seedStarterData(saved.getId());

        String token = tokenService.issueToken(saved.getId());
        return new AuthResponse(token, saved.getId(), saved.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));

        // matches() 把使用者剛輸入的明文密碼,用同樣的演算法算一次,拿去跟資料庫存的雜湊值比對
        // 全程不需要、也不會把資料庫裡的雜湊值「還原」回明文
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }

        String token = tokenService.issueToken(user.getId());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
