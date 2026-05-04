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

    public TaskList create(Long boardId, String title, Long userId) {
        Board board = boardRepository.findById(boardId)
                .filter(b -> b.getUser() != null && b.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Board not found: " + boardId));
        int position = listRepository.findByBoardIdOrderByPosition(boardId).size();
        TaskList list = new TaskList();
        list.setTitle(title);
        list.setPosition(position);
        list.setBoard(board);
        return listRepository.save(list);
    }

    public TaskList updateTitle(Long id, String title, Long userId) {
        TaskList list = listRepository.findByIdAndBoard_User_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("List not found: " + id));
        list.setTitle(title);
        return listRepository.save(list);
    }

    public void delete(Long id, Long userId) {
        TaskList list = listRepository.findByIdAndBoard_User_Id(id, userId)
                .orElseThrow(() -> new RuntimeException("List not found: " + id));
        listRepository.delete(list);
    }

    public void reorder(List<Map<String, Object>> items, Long userId) {
        for (Map<String, Object> item : items) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer position = Integer.valueOf(item.get("position").toString());
            listRepository.findByIdAndBoard_User_Id(id, userId).ifPresent(list -> {
                list.setPosition(position);
                listRepository.save(list);
            });
        }
    }
}
