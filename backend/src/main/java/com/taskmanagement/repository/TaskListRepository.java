package com.taskmanagement.repository;

import com.taskmanagement.model.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findByBoardIdOrderByPosition(Long boardId);
}
