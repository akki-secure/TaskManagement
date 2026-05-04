package com.taskmanagement.repository;

import com.taskmanagement.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByListIdOrderByPosition(Long listId);
    Optional<Card> findByIdAndList_Board_User_Id(Long id, Long userId);
}
