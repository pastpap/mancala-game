package com.bol.games.mancala.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MancalaGameTestSpec {

    private MancalaGame mancalaGame;

    @BeforeEach
    public void before() throws Exception {
        mancalaGame = new MancalaGame();
    }

    @Test
    public void whenStartingOutsideOfTheBoard_thenRuntimeException() {
        givenResetBoard();
        assertThrows(RuntimeException.class, () -> {
            mancalaGame.playHand(14);
        });
    }

    @Test
    public void whenStartingFromPlayerOneMancalaPit_thenRuntimeException() {
        givenResetBoard();
        assertThrows(RuntimeException.class, () -> {
            mancalaGame.playHand(6);
        });
    }

    @Test
    public void whenStartingFromPlayerTwoMancalaPit_thenRuntimeException() {
        givenResetBoard();
        assertThrows(RuntimeException.class, () -> {
            mancalaGame.playHand(13);
        });
    }

    @Test
    public void whenStartingFromOppositePlayerPit_thenRuntimeException() {
        givenResetBoard();
        assertThrows(RuntimeException.class, () -> {
            mancalaGame.playHand(12);
        });
    }

    @Test
    public void givenFirstTurnWhenNextPlayer_thenPlayerOne() {
        givenResetBoard();
        assertEquals(0, mancalaGame.nextPlayer());
    }

    @Test
    public void givenPlayerOneMoveEndsInPlayerTwoPit_whenNextPlayer_thenPlayerTwo() {
        givenEmptyBoard();
        addPebblesInPit(3, 5);
        addPebblesInPit(1, 7);
        mancalaGame.playHand(5);
        assertEquals(1, mancalaGame.nextPlayer());
    }

    private void givenResetBoard() {
        mancalaGame.resetBoard();
        mancalaGame.setLastPlayer(1);
    }

    @Test
    public void givenLiveBoard_whenPlayingHandFromEmptyPit_thenRuntimeException() {
        givenEmptyBoard();
        addPebblesInPit(1, 0);
        addPebblesInPit(1, 7);
        assertThrows(RuntimeException.class, () -> {
            mancalaGame.playHand(1);
        });
    }

    private void givenEmptyBoard() {
        mancalaGame.setBoard(new int[14]);
        mancalaGame.setLastPlayer(1);
    }

    @Test
    public void whenEndingInOneOfTheOpponentsPits_thenOpponentsTurn() {
        givenResetBoard();
        mancalaGame.playHand(1);
        assertEquals(7, mancalaGame.getBoard()[7]);
        assertEquals(1, mancalaGame.nextPlayer());
    }

    @Test
    public void givenPlayerOne_whenPassingTheOpponentsMancala_thenOpponentsMancalaIsSkipped() {
        givenEmptyBoard();
        addPebblesInPit(8, 5);
        addPebblesInPit(1, 0);
        addPebblesInPit(1, 7);
        mancalaGame.playHand(5);
        assertEquals(2, mancalaGame.getBoard()[0]);
        assertEquals(0, mancalaGame.getBoard()[13]);
    }

    private void addPebblesInPit(int pebbles, int pit) {
        int[] changedBoard = mancalaGame.getBoard();
        changedBoard[pit] = pebbles;
        mancalaGame.setBoard(changedBoard);
    }

    @Test
    public void givenPlayerTwo_whenPassingTheOpponentsMancala_thenOpponentsMancalaIsSkipped() {
        givenEmptyBoard();
        addPebblesInPit(2, 5);
        addPebblesInPit(1, 0);
        addPebblesInPit(8, 12);
        mancalaGame.playHand(5);
        mancalaGame.playHand(12);
        assertEquals(2, mancalaGame.getBoard()[7]);
    }

    @Test
    public void givenPlayerOne_whenLastPebbleInPlayerOneMancala_thenPlayerOneAgain() {
        givenEmptyBoard();
        addPebblesInPit(1, 5);
        mancalaGame.playHand(5);
        assertEquals(0, mancalaGame.nextPlayer());
    }

    @Test
    public void givenPlayerTwo_whenLastPebbleInPlayerTwoMancala_thenPlayerTwoAgain() {
        givenEmptyBoard();
        addPebblesInPit(2, 5);
        addPebblesInPit(1, 12);
        mancalaGame.playHand(5);
        mancalaGame.playHand(12);
        assertEquals(1, mancalaGame.nextPlayer());
    }

    @Test
    public void whenPlayerOneLastPebbleInEmptyPlayerOnePit_thenPlayerOneCapturesOppositePlayerTwoPosition() {
        givenEmptyBoard();
        addPebblesInPit(1, 0);
        addPebblesInPit(1, 11);
        mancalaGame.playHand(0);
        assertEquals(2, mancalaGame.getBoard()[6]);
    }

    @Test
    public void whenPlayerTwoLastPebbleInEmptyPlayerTwoPit_thenPlayerTwoCapturesOppositePlayerOnePosition() {
        givenEmptyBoard();
        addPebblesInPit(1, 0);
        addPebblesInPit(1, 1);
        addPebblesInPit(1, 10);
        mancalaGame.playHand(0);
        mancalaGame.playHand(10);
        assertEquals(3, mancalaGame.getBoard()[13]);
    }

    @Test
    public void givenOnePlayerHasNoPebblesInPits_whenIsFinished_thenTrue() {
        givenEmptyBoard();
        addPebblesInPit(1, 0);
        assertTrue(mancalaGame.isFinished());
    }

    @Test
    public void givenOnePlayerHasNoPebblesInPits_whenPlayingAHand_thenHandIsNotPlayed() {
        givenEmptyBoard();
        addPebblesInPit(1, 0);
        assertFalse(mancalaGame.playHand(1));
    }

}
