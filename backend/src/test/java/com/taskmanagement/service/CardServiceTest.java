package com.taskmanagement.service;

import com.taskmanagement.model.Card;
import com.taskmanagement.model.TaskList;
import com.taskmanagement.repository.CardRepository;
import com.taskmanagement.repository.TaskListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    static final Long USER_ID = 99L;

    @Mock
    CardRepository cardRepository;

    @Mock
    TaskListRepository listRepository;

    @InjectMocks
    CardService cardService;

    // ---- ヘルパー ----

    private TaskList makeList(Long id) {
        TaskList list = new TaskList();
        list.setId(id);
        list.setTitle("List " + id);
        list.setPosition(0);
        return list;
    }

    private Card makeCard(Long id, int pos, TaskList list) {
        Card card = new Card();
        card.setId(id);
        card.setTitle("Card " + id);
        card.setPosition(pos);
        card.setList(list);
        return card;
    }

    // ---- update() テスト ----

    @Test
    void update_setDueDate_storesLocalDate() {
        TaskList list = makeList(1L);
        Card card = makeCard(1L, 0, list);
        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.update(1L, Map.of("dueDate", "2025-12-31"), USER_ID);

        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 12, 31));
    }

    @Test
    void update_clearDueDate_setsNull() {
        TaskList list = makeList(1L);
        Card card = makeCard(1L, 0, list);
        card.setDueDate(LocalDate.of(2025, 1, 1));
        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.update(1L, Map.of("dueDate", ""), USER_ID);

        assertThat(result.getDueDate()).isNull();
    }

    @Test
    void update_setPriority_storesPriority() {
        TaskList list = makeList(1L);
        Card card = makeCard(1L, 0, list);
        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.update(1L, Map.of("priority", "high"), USER_ID);

        assertThat(result.getPriority()).isEqualTo("high");
    }

    @Test
    void update_clearPriority_setsNull() {
        TaskList list = makeList(1L);
        Card card = makeCard(1L, 0, list);
        card.setPriority("high");
        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.update(1L, Map.of("priority", ""), USER_ID);

        assertThat(result.getPriority()).isNull();
    }

    @Test
    void update_blankTitle_fallsBackToDefault() {
        TaskList list = makeList(1L);
        Card card = makeCard(1L, 0, list);
        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Card result = cardService.update(1L, Map.of("title", "   "), USER_ID);

        assertThat(result.getTitle()).isEqualTo("無題のカード");
    }

    // ---- move() テスト ----

    @Test
    void move_sameList_cardMovedFromFirstToLast() {
        // [A(0), B(1), C(2)] → Aを位置2へ → [B(0), C(1), A(2)]
        TaskList list = makeList(10L);
        Card a = makeCard(1L, 0, list);
        Card b = makeCard(2L, 1, list);
        Card c = makeCard(3L, 2, list);

        when(cardRepository.findByIdAndList_Board_User_Id(1L, USER_ID)).thenReturn(Optional.of(a));
        when(listRepository.findByIdAndBoard_User_Id(10L, USER_ID)).thenReturn(Optional.of(list));
        when(cardRepository.findByListIdOrderByPosition(10L))
                .thenReturn(new ArrayList<>(List.of(a, b, c)));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.move(1L, 10L, 2, USER_ID);

        assertThat(b.getPosition()).isEqualTo(0);
        assertThat(c.getPosition()).isEqualTo(1);
        assertThat(a.getPosition()).isEqualTo(2);
    }

    @Test
    void move_sameList_cardMovedFromLastToFirst() {
        // [A(0), B(1), C(2)] → Cを位置0へ → [C(0), A(1), B(2)]
        TaskList list = makeList(10L);
        Card a = makeCard(1L, 0, list);
        Card b = makeCard(2L, 1, list);
        Card c = makeCard(3L, 2, list);

        when(cardRepository.findByIdAndList_Board_User_Id(3L, USER_ID)).thenReturn(Optional.of(c));
        when(listRepository.findByIdAndBoard_User_Id(10L, USER_ID)).thenReturn(Optional.of(list));
        when(cardRepository.findByListIdOrderByPosition(10L))
                .thenReturn(new ArrayList<>(List.of(a, b, c)));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.move(3L, 10L, 0, USER_ID);

        assertThat(c.getPosition()).isEqualTo(0);
        assertThat(a.getPosition()).isEqualTo(1);
        assertThat(b.getPosition()).isEqualTo(2);
    }

    @Test
    void move_differentList_fromListReindexed() {
        // fromList: [A(0), B(1), C(2)], toList: [D(0), E(1)]
        // Bをfromからtoのposition=1へ移動 → fromListは[A(0), C(1)]になること
        TaskList fromList = makeList(10L);
        TaskList toList = makeList(20L);
        Card a = makeCard(1L, 0, fromList);
        Card b = makeCard(2L, 1, fromList);
        Card c = makeCard(3L, 2, fromList);
        Card d = makeCard(4L, 0, toList);
        Card e = makeCard(5L, 1, toList);

        when(cardRepository.findByIdAndList_Board_User_Id(2L, USER_ID)).thenReturn(Optional.of(b));
        when(listRepository.findByIdAndBoard_User_Id(20L, USER_ID)).thenReturn(Optional.of(toList));
        when(cardRepository.findByListIdOrderByPosition(10L))
                .thenReturn(new ArrayList<>(List.of(a, b, c)));
        when(cardRepository.findByListIdOrderByPosition(20L))
                .thenReturn(new ArrayList<>(List.of(d, e)));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.move(2L, 20L, 1, USER_ID);

        assertThat(a.getPosition()).isEqualTo(0);
        assertThat(c.getPosition()).isEqualTo(1);
    }

    @Test
    void move_differentList_toListPositionsCorrect() {
        // fromList: [A(0), B(1), C(2)], toList: [D(0), E(1)]
        // Bをposition=1へ挿入 → toListは[D(0), B(1), E(2)]になること
        TaskList fromList = makeList(10L);
        TaskList toList = makeList(20L);
        Card a = makeCard(1L, 0, fromList);
        Card b = makeCard(2L, 1, fromList);
        Card c = makeCard(3L, 2, fromList);
        Card d = makeCard(4L, 0, toList);
        Card e = makeCard(5L, 1, toList);

        when(cardRepository.findByIdAndList_Board_User_Id(2L, USER_ID)).thenReturn(Optional.of(b));
        when(listRepository.findByIdAndBoard_User_Id(20L, USER_ID)).thenReturn(Optional.of(toList));
        when(cardRepository.findByListIdOrderByPosition(10L))
                .thenReturn(new ArrayList<>(List.of(a, b, c)));
        when(cardRepository.findByListIdOrderByPosition(20L))
                .thenReturn(new ArrayList<>(List.of(d, e)));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cardService.move(2L, 20L, 1, USER_ID);

        assertThat(d.getPosition()).isEqualTo(0);
        assertThat(b.getPosition()).isEqualTo(1);
        assertThat(e.getPosition()).isEqualTo(2);
        assertThat(b.getList()).isEqualTo(toList);
    }
}
