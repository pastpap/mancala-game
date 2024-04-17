package com.bol.games.mancala.rules;

import com.bol.games.mancala.constants.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class MancalaGame {
    private int lastPlayer;
    private int[] board;

    @Value("${mancala.game.boardsize}")
    private Integer boardSize;
    @Value("${mancala.game.pebbles}")
    private Integer pebblesPerPit;


    public boolean playHand(int position) {
        if (!isFinished()) {
            validateHandStartPosition(position);
            int currentPosition = distributePebbles(position);
            if (isPlayerLastMoveAOnePebbleInOwnPit(currentPosition)) {
                tryCapturingEnemyPosition(currentPosition);
            }
            changeTurns(currentPosition);
        }
        return !isFinished();
    }

    public boolean isFinished() {
        return getPlayerOneLivePebbles() == 0 || getPlayerTwoLivePebbles() == 0;
    }

    private int getPlayerOneLivePebbles() {
        return IntStream
                .range(0, getBoardSize())
                .filter(i -> i >= 0 && i < getPlayerOneMancalaIndex())
                .map(i -> getBoard()[i])
                .sum();
    }

    private int getPlayerTwoLivePebbles() {
        return IntStream
                .range(0, getBoardSize())
                .filter(i -> i >= getPlayerTwoFirstPitIndex() && i < getPlayerTwoMancalaIndex())
                .map(i -> getBoard()[i])
                .sum();
    }

    private void validateHandStartPosition(int position) {
        checkBoardExtremities(position);
        checkHandStartingFromMancalaPits(position);
        checkHandStartingFromEmptyPit(position);
        checkHandStartingOpponentPits(position);

    }

    private void checkBoardExtremities(int position) {
        if (position < 0 || position > getPlayerTwoMancalaIndex()) {
            throw new RuntimeException("Cannot start hand from outside the board");
        }
    }

    private void checkHandStartingFromMancalaPits(int position) {
        if (position == getPlayerOneMancalaIndex() || position == getPlayerTwoMancalaIndex()) {
            throw new RuntimeException("Cannot start hand from a Mancala pit");
        }
    }

    private void checkHandStartingOpponentPits(int position) {
        if (!isPlayerOwnPit(position)) {
            throw new RuntimeException("Cannot start hand fom opponent's pits");
        }
    }

    private void checkHandStartingFromEmptyPit(int position) {
        if (getBoard()[position] == 0) {
            throw new RuntimeException("Cannot start hand from empty pit");
        }
    }

    private int distributePebbles(int position) {
        int startPebbles = board[position];
        board[position] = 0;
        int currentPosition = position;
        while (startPebbles > 0) {
            currentPosition = addOnePebbleToTheNextAvailablePit(currentPosition);
            startPebbles--;
        }
        return currentPosition;
    }

    private int addOnePebbleToTheNextAvailablePit(int currentPosition) {
        currentPosition = advanceCurrentPosition(currentPosition);
        board[currentPosition]++;
        return currentPosition;
    }

    private int advanceCurrentPosition(int currentPosition) {
        currentPosition = handleMovingToTheStartOfTheBoardWhenLastIndexReached(currentPosition);
        currentPosition = handleJumpingOverOpponentsMancala(currentPosition);
        return currentPosition;
    }

    private int handleMovingToTheStartOfTheBoardWhenLastIndexReached(int currentPosition) {
        if (currentPosition < getPlayerTwoMancalaIndex()) {
            currentPosition++;
        } else {
            currentPosition = 0;
        }
        return currentPosition;
    }

    private int handleJumpingOverOpponentsMancala(int currentPosition) {
        if (isPlayerOneHandAndNextPitIsThePlayerTwoMancala(currentPosition)) {
            currentPosition = 0;
        } else if (isPlayerTwoHandAndNextPitIsThePlayerOneMancala(currentPosition)) {
            currentPosition++;
        }
        return currentPosition;
    }

    private boolean isPlayerOneHandAndNextPitIsThePlayerTwoMancala(int currentPosition) {
        return isPlayerOne() && currentPosition == getPlayerTwoMancalaIndex();
    }

    private boolean isPlayerTwoHandAndNextPitIsThePlayerOneMancala(int currentPosition) {
        return isPlayerTwo() && currentPosition == getPlayerOneMancalaIndex();
    }

    private boolean isPlayerLastMoveAOnePebbleInOwnPit(int currentPosition) {
        return getBoard()[currentPosition] == 1 && isPlayerOwnPit(currentPosition);
    }

    private boolean isPlayerOwnPit(int currentPosition) {
        return isPlayerOneOwnPit(currentPosition) || isPlayerTwoOwnPit(currentPosition);
    }

    private boolean isPlayerOneOwnPit(int currentPosition) {
        return isPlayerOne() && currentPosition >= 0 && currentPosition < getPlayerOneMancalaIndex();
    }

    private boolean isPlayerTwoOwnPit(int currentPosition) {
        return isPlayerTwo() && currentPosition > getPlayerOneMancalaIndex() && currentPosition < getPlayerTwoMancalaIndex();
    }

    private void tryCapturingEnemyPosition(int currentPosition) {
        if (payerOneLastPebbleFellInEmptyOwnPit(currentPosition)) {
            captureFromPositionToMancala(currentPosition, getPlayerOneMancalaIndex());
        } else if (playerTwoLastPebbleFellIntoEmptyOwnPit(currentPosition)) {
            captureFromPositionToMancala(currentPosition, getPlayerTwoMancalaIndex());
        }
    }

    private void captureFromPositionToMancala(int currentPosition, int mancala) {
        getBoard()[mancala] += getBoard()[currentPosition] + getBoard()[getTotalLivePits() - currentPosition];
        getBoard()[currentPosition] = 0;
        getBoard()[getTotalLivePits() - currentPosition] = 0;
    }

    private boolean payerOneLastPebbleFellInEmptyOwnPit(int currentPosition) {
        return isPlayerOne() && 0 <= currentPosition && currentPosition < getPlayerOneMancalaIndex() && getBoard()[currentPosition] == 1;
    }

    private boolean playerTwoLastPebbleFellIntoEmptyOwnPit(int currentPosition) {
        return isPlayerTwo() && getPlayerTwoFirstPitIndex() <= currentPosition && currentPosition < getPlayerTwoMancalaIndex() && getBoard()[currentPosition] == 1;
    }

    private void changeTurns(int currentPosition) {
        if (isPlayerOneLatestPositionNotOwnMancala(currentPosition) || isPlayerTwoLatestPositionNotOwnMancala(currentPosition)) {
            setLastPlayer(nextPlayer());
        }
    }

    private boolean isPlayerTwoLatestPositionNotOwnMancala(int currentPosition) {
        return nextPlayer() == 1 && currentPosition != getPlayerTwoMancalaIndex();
    }

    private boolean isPlayerOneLatestPositionNotOwnMancala(int currentPosition) {
        return nextPlayer() == 0 && currentPosition != getPlayerOneMancalaIndex();
    }

    private boolean isPlayerOne() {
        return nextPlayer() == Constants.PLAYER_ONE_ID;
    }

    private boolean isPlayerTwo() {
        return !isPlayerOne();
    }

    public int nextPlayer() {
        if (getLastPlayer() == Constants.PLAYER_ONE_ID) {
            return Constants.PLAYER_TWO_ID;
        }
        return Constants.PLAYER_ONE_ID;
    }

    public int[] getBoard() {
        if (board.length <= 1) {
            board = new int[getBoardSize()];
        }
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public void resetBoard() {
        int[] resetBoard = new int[getBoardSize()];
        Arrays.fill(resetBoard, getPebblesPerPit());
        resetBoard[getPlayerOneMancalaIndex()] = 0;
        resetBoard[getPlayerTwoMancalaIndex()] = 0;
        setBoard(resetBoard);
        setLastPlayer(1);
    }

    public int getLastPlayer() {
        return lastPlayer;
    }

    public void setLastPlayer(int lastPlayer) {
        this.lastPlayer = lastPlayer;
    }

    public Integer getWinningPlayer() {
        if (isFinished()) {
            int compareResult = Integer.compare(getPlayerOneTotalPebbles(), getPlayerTwoTotalPebbles());
            return findWinningPlayerId(compareResult);
        }
        return null;
    }

    private Integer findWinningPlayerId(int compareResult) {
        if (compareResult > 0) {
            return Constants.PLAYER_ONE_ID;
        } else if (compareResult < 0) {
            return Constants.PLAYER_TWO_ID;
        }
        return null;
    }

    public Integer getWinningPlayerScore() {
        return Optional.ofNullable(getWinningPlayer())
                .map(id -> id == 0 ? getPlayerOneTotalPebbles() : getPlayerTwoTotalPebbles())
                .orElse(null);
    }

    public int getPlayerOneTotalPebbles() {
        return getPlayerOneLivePebbles() + getBoard()[getPlayerOneMancalaIndex()];
    }

    public int getPlayerTwoTotalPebbles() {
        return getPlayerTwoLivePebbles() + getBoard()[getPlayerTwoMancalaIndex()];
    }

    public int getBoardSize() {
        if (boardSize == null) {
            boardSize = 14;
        }
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getPebblesPerPit() {
        if (pebblesPerPit == null) {
            pebblesPerPit = 6;
        }
        return pebblesPerPit;
    }

    public void setPebblesPerPit(int pebblesPerPit) {
        this.pebblesPerPit = pebblesPerPit;
    }

    public int getTotalLivePits() {
        return getBoardSize() - 2;
    }

    public int getPlayerTwoFirstPitIndex() {
        return getBoardSize() / 2;
    }

    public int getPlayerOneMancalaIndex() {
        return getPlayerTwoFirstPitIndex() - 1;
    }

    public int getPlayerTwoMancalaIndex() {
        return getBoardSize() - 1;
    }
}
