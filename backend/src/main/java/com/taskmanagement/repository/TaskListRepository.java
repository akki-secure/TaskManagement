package com.taskmanagement.repository;

import com.taskmanagement.model.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {
    List<TaskList> findByBoardIdOrderByPosition(Long boardId);

    @Query(value = "SELECT * FROM lists WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC", nativeQuery = true)
    List<TaskList> findTrashedLists();

    @Query(value = "SELECT * FROM lists WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<TaskList> findTrashedById(@Param("id") Long id);
}
