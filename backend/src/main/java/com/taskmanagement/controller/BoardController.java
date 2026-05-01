package com.taskmanagement.controller;

import com.taskmanagement.model.Board;
import com.taskmanagement.repository.BoardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardRepository boardRepository;

    public BoardController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @GetMapping
    public List<Board> getMyBoards(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return boardRepository.findByUser_Id(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoard(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return boardRepository.findById(id)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardRepository.save(board);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return boardRepository.findById(id)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(userId))
                .<ResponseEntity<Void>>map(b -> {
                    boardRepository.delete(b);
                    return ResponseEntity.<Void>noContent().build();
                })
                .orElse(ResponseEntity.<Void>notFound().build());
    }
}
