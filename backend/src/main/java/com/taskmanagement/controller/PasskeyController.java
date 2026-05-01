package com.taskmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/passkey")
public class PasskeyController {

    // WebAuthn full implementation is planned for a future issue.

    @PostMapping("/registration/options")
    public ResponseEntity<Map<String, String>> registrationOptions() {
        return ResponseEntity.status(501).body(Map.of("message", "パスキー登録は現在準備中です"));
    }

    @PostMapping("/registration/verify")
    public ResponseEntity<Map<String, String>> registrationVerify(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(501).body(Map.of("message", "パスキー登録は現在準備中です"));
    }

    @PostMapping("/authentication/options")
    public ResponseEntity<Map<String, String>> authenticationOptions() {
        return ResponseEntity.status(501).body(Map.of("message", "パスキー認証は現在準備中です"));
    }

    @PostMapping("/authentication/verify")
    public ResponseEntity<Map<String, String>> authenticationVerify(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(501).body(Map.of("message", "パスキー認証は現在準備中です"));
    }
}
