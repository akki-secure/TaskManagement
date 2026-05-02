package com.taskmanagement.service;

import com.taskmanagement.model.Board;
import com.taskmanagement.model.TaskList;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.CardRepository;
import com.taskmanagement.repository.TaskListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ListService {

    private final TaskListRepository listRepository;
    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;

    public ListService(TaskListRepository listRepository, BoardRepository boardRepository, CardRepository cardRepository) {
        this.listRepository = listRepository;
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
    }

    public TaskList create(Long boardId, String title) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found: " + boardId));
        int position = listRepository.findByBoardIdOrderByPosition(boardId).size();
        TaskList list = new TaskList();
        list.setTitle(title);
        list.setPosition(position);
        list.setBoard(board);
        return listRepository.save(list);
    }

    public TaskList updateTitle(Long id, String title) {
        TaskList list = listRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("List not found: " + id));
        list.setTitle(title);
        return listRepository.save(list);
    }

    public void delete(Long id) {
        TaskList list = listRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("List not found: " + id));
        LocalDateTime now = LocalDateTime.now();
        list.getCards().forEach(card -> {
            card.setDeletedAt(now);
            cardRepository.save(card);
        });
        list.setDeletedAt(now);
        listRepository.save(list);
    }

    public void reorder(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer position = Integer.valueOf(item.get("position").toString());
            listRepository.findById(id).ifPresent(list -> {
                list.setPosition(position);
                listRepository.save(list);
            });
        }
    }

    @Transactional(readOnly = true)
    public List<TaskList> findTrashed() {
        return listRepository.findTrashedLists();
    }

    public TaskList restore(Long id) {
        TaskList list = listRepository.findTrashedById(id)
                .orElseThrow(() -> new RuntimeException("Trashed list not found: " + id));
        list.setDeletedAt(null);
        listRepository.save(list);
        cardRepository.findTrashedByListId(id).forEach(card -> {
            card.setDeletedAt(null);
            cardRepository.save(card);
        });
        return list;
    }

    public void deletePermanently(Long id) {
        TaskList list = listRepository.findTrashedById(id)
                .orElseThrow(() -> new RuntimeException("Trashed list not found: " + id));
        listRepository.delete(list);
    }
}
