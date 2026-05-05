package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.LoginRequest;
import com.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.model.Board;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final BoardRepository boardRepo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, BoardRepository boardRepo,
                       BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.boardRepo = boardRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank())
            throw new RuntimeException("ユーザー名を入力してください");
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new RuntimeException("メールアドレスを入力してください");
        if (req.getPassword() == null || req.getPassword().length() < 8)
            throw new RuntimeException("パスワードは8文字以上で入力してください");

        String username = Normalizer.normalize(req.getUsername().strip(), Normalizer.Form.NFC);
        if (username.isBlank())
            throw new RuntimeException("ユーザー名を入力してください");
        if (!username.matches("[a-zA-Z0-9_ \\u3000\\u3005\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF\\u3400-\\u4DBF]{2,50}"))
            throw new RuntimeException("ユーザー名は2〜50文字で入力してください（英数字・スペース・日本語が使えます）");

        if (userRepo.existsByUsername(username))
            throw new RuntimeException("このユーザー名はすでに使用されています");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("このメールアドレスはすでに登録されています");

        User user = new User();
        user.setUsername(username);
        user.setEmail(req.getEmail());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        userRepo.save(user);

        Board board = new Board();
        board.setTitle("マイボード");
        board.setUser(user);
        boardRepo.save(board);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        if (req.getIdentifier() == null || req.getIdentifier().isBlank())
            throw new RuntimeException("ユーザー名またはメールアドレスを入力してください");

        String identifier = Normalizer.normalize(req.getIdentifier().trim(), Normalizer.Form.NFC);
        Optional<User> userOpt = identifier.contains("@")
                ? userRepo.findByEmail(identifier)
                : userRepo.findByUsername(identifier);

        User user = userOpt.orElseThrow(() -> new RuntimeException("ユーザー名またはパスワードが正しくありません"));

        if (user.getPasswordHash() == null || !encoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new RuntimeException("ユーザー名またはパスワードが正しくありません");

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }
}
