package com.taskmanagement.repository;

import com.taskmanagement.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByListIdOrderByPosition(Long listId);

    @Query(value = "SELECT * FROM cards WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC", nativeQuery = true)
    List<Card> findTrashedCards();

    @Query(value = "SELECT * FROM cards WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Card> findTrashedById(@Param("id") Long id);

    @Query(value = "SELECT * FROM cards WHERE list_id = :listId AND deleted_at IS NOT NULL", nativeQuery = true)
    List<Card> findTrashedByListId(@Param("listId") Long listId);
}
