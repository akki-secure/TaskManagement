package com.taskmanagement.controller;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.ChangePasswordRequest;
import com.taskmanagement.dto.UpdateProfileRequest;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepo, BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getMe(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getUsername(), user.getEmail()));
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(@RequestBody UpdateProfileRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            if (!req.getUsername().matches("[a-zA-Z0-9_\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF\\u3400-\\u4DBF]{3,50}"))
                throw new RuntimeException("ユーザー名は3〜50文字で入力してください（英数字・アンダースコア・日本語が使えます）");
            if (!req.getUsername().equals(user.getUsername()) && userRepo.existsByUsername(req.getUsername()))
                throw new RuntimeException("このユーザー名はすでに使用されています");
            user.setUsername(req.getUsername());
        }

        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            if (!req.getEmail().equals(user.getEmail()) && userRepo.existsByEmail(req.getEmail()))
                throw new RuntimeException("このメールアドレスはすでに登録されています");
            user.setEmail(req.getEmail());
        }

        userRepo.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail()));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

        if (req.getCurrentPassword() == null || !encoder.matches(req.getCurrentPassword(), user.getPasswordHash()))
            throw new RuntimeException("現在のパスワードが正しくありません");
        if (req.getNewPassword() == null || req.getNewPassword().length() < 8)
            throw new RuntimeException("新しいパスワードは8文字以上で入力してください");

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
        return ResponseEntity.noContent().build();
    }
}
