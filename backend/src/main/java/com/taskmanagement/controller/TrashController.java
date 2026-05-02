package com.taskmanagement.controller;

import com.taskmanagement.model.Card;
import com.taskmanagement.model.TaskList;
import com.taskmanagement.service.CardService;
import com.taskmanagement.service.ListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trash")
public class TrashController {

    private final CardService cardService;
    private final ListService listService;

    public TrashController(CardService cardService, ListService listService) {
        this.cardService = cardService;
        this.listService = listService;
    }

    @GetMapping("/cards")
    public ResponseEntity<List<Card>> getTrashedCards() {
        return ResponseEntity.ok(cardService.findTrashed());
    }

    @GetMapping("/lists")
    public ResponseEntity<List<TaskList>> getTrashedLists() {
        return ResponseEntity.ok(listService.findTrashed());
    }

    @PutMapping("/cards/{id}/restore")
    public ResponseEntity<Card> restoreCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.restore(id));
    }

    @PutMapping("/lists/{id}/restore")
    public ResponseEntity<TaskList> restoreList(@PathVariable Long id) {
        return ResponseEntity.ok(listService.restore(id));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCardPermanently(@PathVariable Long id) {
        cardService.deletePermanently(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lists/{id}")
    public ResponseEntity<Void> deleteListPermanently(@PathVariable Long id) {
        listService.deletePermanently(id);
        return ResponseEntity.noContent().build();
    }
}
