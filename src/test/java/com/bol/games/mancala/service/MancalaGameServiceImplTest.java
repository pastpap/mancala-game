package com.bol.games.mancala.service;

import com.bol.games.mancala.model.MancalaGameState;
import com.bol.games.mancala.rules.MancalaGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MancalaGameServiceImplTest {
    private MancalaGameServiceImpl mancalaGameService;

    @BeforeEach
    public void before() throws Exception {
        MancalaGame  mancalaGame = new MancalaGame();
        mancalaGameService = new MancalaGameServiceImpl(mancalaGame);
    }

    @Test
    public void whenAskingForNewBoard_thenTheCorrectGameStateIsReturned() {
        MancalaGameState returnedState = mancalaGameService.resetGame();
        assertArrayEquals(getNewBoardArray(), returnedState.getBoard());
        assertEquals(0, returnedState.getTurnPlayer());
        assertEquals(false, returnedState.isFinished());
        assertEquals(-1, returnedState.getWinningPlayer());
        assertEquals(0,returnedState.getWinningPlayerScore());
    }

    private int[] getNewBoardArray() {
        int[] newBoard = new int[14];
        Arrays.fill(newBoard, 6);
        newBoard[6] = 0;
        newBoard[13] = 0;
        return newBoard;
    }

    @Test
    public void whenPlayingLastHandOfPlayerTwoBeforeGameEnds_thenTheCorrectResultsAreFoundInTheGameState() {
        MancalaGameState outState = mancalaGameService.playHand(12, generateStateOneHandOfPlayerTwoBeforeGameEnd());
        assertEquals(1, outState.getTurnPlayer());
        assertEquals(true, outState.isFinished());
        assertEquals(0, outState.getWinningPlayer());
        assertEquals(36, outState.getWinningPlayerScore());
    }

    private MancalaGameState generateStateOneHandOfPlayerTwoBeforeGameEnd() {
        return MancalaGameState.builder()
                .board(getBoardForLastHandOfPlayerTwo())
                .turnPlayer(1)
                .isFinished(false)
                .winningPlayer(-1)
                .winningPlayerScore(0)
                .build();
    }

    private int[] getBoardForLastHandOfPlayerTwo() {
        int[] newBoard = getNewBoardArray();
        for (int i = 7; i < 13; i++) {
            newBoard[i] = 0;
        }
        newBoard[12] = 1;
        return newBoard;
    }
}