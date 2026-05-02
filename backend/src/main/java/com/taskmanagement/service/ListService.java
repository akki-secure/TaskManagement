package com.taskmanagement.service;

import com.taskmanagement.model.Board;
import com.taskmanagement.model.TaskList;
import com.taskmanagement.repository.BoardRepository;
import com.taskmanagement.repository.TaskListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ListService {

    private final TaskListRepository listRepository;
    private final BoardRepository boardRepository;

    public ListService(TaskListRepository listRepository, BoardRepository boardRepository) {
        this.listRepository = listRepository;
        this.boardRepository = boardRepository;
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
        listRepository.deleteById(id);
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
}
