package com.taskmanagement.service;

import com.taskmanagement.dto.AuthResponse;
import com.taskmanagement.dto.LoginRequest;
import com.taskmanagement.dto.RegisterRequest;
import com.taskmanagement.model.Board;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // ダミー値は test- で始まるものだけを使う（.gitleaks.toml の許可形式）
    static final String PASSWORD = "test-password-1234";
    static final String TOKEN = "test-token";

    @Mock
    UserRepository userRepo;

    @Mock
    BoardRepository boardRepo;

    @Mock
    JwtUtil jwtUtil;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepo, boardRepo, encoder, jwtUtil);
    }

    // ---- ヘルパー ----

    private RegisterRequest registerRequest(String username, String email, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    private LoginRequest loginRequest(String identifier, String password) {
        LoginRequest req = new LoginRequest();
        req.setIdentifier(identifier);
        req.setPassword(password);
        return req;
    }

    private User makeUser(Long id, String username, String email, String rawPassword) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(rawPassword == null ? null : encoder.encode(rawPassword));
        return user;
    }

    // ---- register: 正常系 ----

    @Test
    void register_成功するとユーザーとデフォルトボードを保存しトークンを返す() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(jwtUtil.generateToken(any(), any())).thenReturn(TOKEN);

        AuthResponse res = authService.register(registerRequest("alice", "alice@example.com", PASSWORD));

        assertThat(res.getToken()).isEqualTo(TOKEN);
        assertThat(res.getUsername()).isEqualTo("alice");
        assertThat(res.getEmail()).isEqualTo("alice@example.com");

        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepo).save(boardCaptor.capture());
        assertThat(boardCaptor.getValue().getTitle()).isEqualTo("マイボード");
    }

    @Test
    void register_パスワードは平文ではなくハッシュで保存される() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(jwtUtil.generateToken(any(), any())).thenReturn(TOKEN);

        authService.register(registerRequest("alice", "alice@example.com", PASSWORD));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userCaptor.capture());
        String saved = userCaptor.getValue().getPasswordHash();
        assertThat(saved).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, saved)).isTrue();
    }

    @Test
    void register_日本語ユーザー名と前後の空白除去() {
        when(userRepo.existsByUsername("山田太郎")).thenReturn(false);
        when(userRepo.existsByEmail("taro@example.com")).thenReturn(false);
        when(jwtUtil.generateToken(any(), any())).thenReturn(TOKEN);

        AuthResponse res = authService.register(registerRequest("  山田太郎  ", "taro@example.com", PASSWORD));

        assertThat(res.getUsername()).isEqualTo("山田太郎");
    }

    // ---- register: 入力検証 ----

    @Test
    void register_ユーザー名が空ならエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("  ", "a@example.com", PASSWORD)))
                .hasMessage("ユーザー名を入力してください");
        verify(userRepo, never()).save(any());
    }

    @Test
    void register_メールアドレスが空ならエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("alice", "", PASSWORD)))
                .hasMessage("メールアドレスを入力してください");
        verify(userRepo, never()).save(any());
    }

    @Test
    void register_パスワードが8文字未満ならエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("alice", "a@example.com", "test-1")))
                .hasMessage("パスワードは8文字以上で入力してください");
        verify(userRepo, never()).save(any());
    }

    @Test
    void register_パスワードがnullならエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("alice", "a@example.com", null)))
                .hasMessage("パスワードは8文字以上で入力してください");
    }

    @Test
    void register_ユーザー名が1文字ならエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("a", "a@example.com", PASSWORD)))
                .hasMessageContaining("ユーザー名は2〜50文字");
    }

    @Test
    void register_ユーザー名に使えない記号があるとエラー() {
        assertThatThrownBy(() -> authService.register(registerRequest("al<ice>", "a@example.com", PASSWORD)))
                .hasMessageContaining("ユーザー名は2〜50文字");
    }

    // ---- register: 重複 ----

    @Test
    void register_ユーザー名が重複しているとエラー() {
        when(userRepo.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest("alice", "a@example.com", PASSWORD)))
                .hasMessage("このユーザー名はすでに使用されています");
        verify(userRepo, never()).save(any());
    }

    @Test
    void register_メールアドレスが重複しているとエラー() {
        when(userRepo.existsByUsername("alice")).thenReturn(false);
        when(userRepo.existsByEmail("a@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest("alice", "a@example.com", PASSWORD)))
                .hasMessage("このメールアドレスはすでに登録されています");
        verify(userRepo, never()).save(any());
    }

    // ---- login ----

    @Test
    void login_ユーザー名とパスワードが正しければトークンを返す() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(makeUser(1L, "alice", "a@example.com", PASSWORD)));
        when(jwtUtil.generateToken(1L, "alice")).thenReturn(TOKEN);

        AuthResponse res = authService.login(loginRequest("alice", PASSWORD));

        assertThat(res.getToken()).isEqualTo(TOKEN);
        assertThat(res.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_メールアドレスでもログインできる() {
        when(userRepo.findByEmail("a@example.com")).thenReturn(Optional.of(makeUser(1L, "alice", "a@example.com", PASSWORD)));
        when(jwtUtil.generateToken(1L, "alice")).thenReturn(TOKEN);

        AuthResponse res = authService.login(loginRequest("a@example.com", PASSWORD));

        assertThat(res.getUsername()).isEqualTo("alice");
        verify(userRepo, never()).findByUsername(any());
    }

    @Test
    void login_パスワードが違うとエラー() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(makeUser(1L, "alice", "a@example.com", PASSWORD)));

        assertThatThrownBy(() -> authService.login(loginRequest("alice", "test-wrong-password")))
                .hasMessage("ユーザー名またはパスワードが正しくありません");
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    void login_存在しないユーザーでもパスワード違いと同じメッセージ() {
        when(userRepo.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest("nobody", PASSWORD)))
                .hasMessage("ユーザー名またはパスワードが正しくありません");
    }

    @Test
    void login_パスワードハッシュが無いユーザーはログインできない() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(makeUser(1L, "alice", "a@example.com", null)));

        assertThatThrownBy(() -> authService.login(loginRequest("alice", PASSWORD)))
                .hasMessage("ユーザー名またはパスワードが正しくありません");
    }

    @Test
    void login_識別子が空ならエラー() {
        assertThatThrownBy(() -> authService.login(loginRequest(" ", PASSWORD)))
                .hasMessage("ユーザー名またはメールアドレスを入力してください");
    }
}
