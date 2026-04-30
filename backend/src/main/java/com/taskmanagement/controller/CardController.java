package com.taskmanagement.controller;

import com.taskmanagement.model.Card;
import com.taskmanagement.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Card>> getByListId(@RequestParam Long listId) {
        return ResponseEntity.ok(cardService.findByListId(listId));
    }

    @PostMapping
    public ResponseEntity<Card> create(@RequestBody Map<String, Object> body) {
        Long listId = Long.valueOf(body.get("listId").toString());
        return ResponseEntity.ok(cardService.create(listId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(cardService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<Card> move(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long toListId = Long.valueOf(body.get("toListId").toString());
        int newPosition = Integer.parseInt(body.get("position").toString());
        return ResponseEntity.ok(cardService.move(id, toListId, newPosition));
    }
}
