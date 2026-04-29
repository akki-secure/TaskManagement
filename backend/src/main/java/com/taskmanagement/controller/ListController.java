package com.taskmanagement.controller;

import com.taskmanagement.model.TaskList;
import com.taskmanagement.service.ListService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lists")
public class ListController {

    private final ListService listService;

    public ListController(ListService listService) {
        this.listService = listService;
    }

    @PostMapping
    public ResponseEntity<TaskList> create(@RequestBody Map<String, Object> body) {
        Long boardId = Long.valueOf(body.get("boardId").toString());
        String title = (String) body.getOrDefault("title", "新しいリスト");
        return ResponseEntity.ok(listService.create(boardId, title));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskList> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = (String) body.get("title");
        return ResponseEntity.ok(listService.updateTitle(id, title));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        listService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody List<Map<String, Object>> items) {
        listService.reorder(items);
        return ResponseEntity.noContent().build();
    }
}
