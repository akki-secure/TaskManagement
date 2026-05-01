package com.taskmanagement.repository;

import com.taskmanagement.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUser_Id(Long userId);
}
