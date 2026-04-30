package com.taskmanagement.service;

import com.taskmanagement.model.Card;
import com.taskmanagement.model.TaskList;
import com.taskmanagement.repository.CardRepository;
import com.taskmanagement.repository.TaskListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final TaskListRepository listRepository;

    public CardService(CardRepository cardRepository, TaskListRepository listRepository) {
        this.cardRepository = cardRepository;
        this.listRepository = listRepository;
    }

    @Transactional(readOnly = true)
    public Card findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Card> findByListId(Long listId) {
        if (!listRepository.existsById(listId)) {
            throw new RuntimeException("List not found: " + listId);
        }
        return cardRepository.findByListIdOrderByPosition(listId);
    }

    public Card create(Long listId) {
        TaskList list = listRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found: " + listId));
        int position = cardRepository.findByListIdOrderByPosition(listId).size();
        Card card = new Card();
        card.setTitle("無題のカード");
        card.setPosition(position);
        card.setList(list);
        return cardRepository.save(card);
    }

    public Card update(Long id, Map<String, Object> body) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found: " + id));

        if (body.containsKey("title")) {
            String title = (String) body.get("title");
            card.setTitle(title == null || title.isBlank() ? "無題のカード" : title);
        }
        if (body.containsKey("description")) {
            card.setDescription((String) body.get("description"));
        }
        if (body.containsKey("dueDate")) {
            String due = (String) body.get("dueDate");
            card.setDueDate(due == null || due.isBlank() ? null : LocalDate.parse(due));
        }
        if (body.containsKey("priority")) {
            String p = (String) body.get("priority");
            card.setPriority(p == null || p.isBlank() ? null : p);
        }
        return cardRepository.save(card);
    }

    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    public Card move(Long cardId, Long toListId, int newPosition) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found: " + cardId));
        TaskList toList = listRepository.findById(toListId)
                .orElseThrow(() -> new RuntimeException("List not found: " + toListId));

        Long fromListId = card.getList().getId();

        // Remove from source list and reindex
        if (!fromListId.equals(toListId)) {
            List<Card> fromCards = cardRepository.findByListIdOrderByPosition(fromListId);
            fromCards.remove(card);
            for (int i = 0; i < fromCards.size(); i++) {
                fromCards.get(i).setPosition(i);
                cardRepository.save(fromCards.get(i));
            }
        }

        // Insert into destination list
        List<Card> toCards = cardRepository.findByListIdOrderByPosition(toListId);
        toCards.remove(card); // in case same list
        toCards.add(Math.min(newPosition, toCards.size()), card);
        for (int i = 0; i < toCards.size(); i++) {
            toCards.get(i).setPosition(i);
            cardRepository.save(toCards.get(i));
        }

        card.setList(toList);
        return cardRepository.save(card);
    }
}
