package org.example.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.api.models.Board;
import org.example.api.models.Card;
import org.example.api.services.BoardService;
import org.example.api.services.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrelloFlowTest {

    private static final Logger LOG = LogManager.getLogger(TrelloFlowTest.class);

    private final BoardService boardService = new BoardService();
    private final CardService cardService = new CardService();

    @Test
    @DisplayName("Trello: create board -> 2 cards -> update random -> delete cards -> delete board")
    void fullTrelloFlow() {
        Board board = boardService.createBoard("Testinium Case Board");
        assertNotNull(board.getId(), "Board id should not be null");

        String listId = boardService.getDefaultListId(board.getId());

        Card card1 = cardService.createCard(listId, "Card 1");
        Card card2 = cardService.createCard(listId, "Card 2");
        List<Card> cards = List.of(card1, card2);

        Card chosen = cards.get(ThreadLocalRandom.current().nextInt(cards.size()));
        LOG.info("Randomly chose card id={} to update", chosen.getId());
        String newName = "Güncellenmiş Kart";
        Card updated = cardService.updateCard(chosen.getId(), newName);
        assertEquals(newName, updated.getName(), "Updated card name should match");

        cardService.deleteCard(card1.getId());
        cardService.deleteCard(card2.getId());

        boardService.deleteBoard(board.getId());
    }
}
