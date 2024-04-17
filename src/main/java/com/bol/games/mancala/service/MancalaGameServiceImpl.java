package com.bol.games.mancala.service;

import com.bol.games.mancala.constants.Constants;
import com.bol.games.mancala.model.MancalaGameState;
import com.bol.games.mancala.rules.MancalaGame;
import com.bol.games.mancala.service.interfaces.MancalaGameService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MancalaGameServiceImpl implements MancalaGameService {

    private MancalaGame mancalaGame;

    public MancalaGameServiceImpl(MancalaGame mancalaGame) {
        this.mancalaGame = mancalaGame;
    }

    @Override
    public MancalaGameState playHand(int position, MancalaGameState state) {
        mancalaGame.setBoard(state.getBoard());
        mancalaGame.setLastPlayer(calculateLastPlayer(state.getTurnPlayer()));
        try {
            mancalaGame.playHand(position);
        } catch (RuntimeException ex) {
            state.setMessage(ex.getMessage());
            return state;
        }
        MancalaGameState newState = mapMancalaGameToState(mancalaGame);
        if (mancalaGame.isFinished()) {
            newState.setMessage(getMessageAboutWinnerAndScore(newState));
        }
        return newState;
    }

    private String getMessageAboutWinnerAndScore(MancalaGameState newState) {
        return (newState.getWinningPlayer() == Constants.PLAYER_ONE_ID ? "Player One" : "Player Two")
                + " won with "
                + newState.getWinningPlayerScore()
                + " pebbles";
    }

    private int calculateLastPlayer(int turnPlayer) {
        return turnPlayer == Constants.PLAYER_ONE_ID ? Constants.PLAYER_TWO_ID : Constants.PLAYER_ONE_ID;
    }

    @Override
    public MancalaGameState resetGame() {
        mancalaGame.resetBoard();

        return mapMancalaGameToState(mancalaGame);
    }

    private MancalaGameState mapMancalaGameToState(MancalaGame mancalaGame) {
        Integer winnerId = mancalaGame.getWinningPlayer();
        return MancalaGameState.builder()
                .board(mancalaGame.getBoard())
                .turnPlayer(mancalaGame.nextPlayer())
                .isFinished(mancalaGame.isFinished())
                .winningPlayer(Optional.ofNullable(winnerId).orElse(-1))
                .winningPlayerScore(Optional.ofNullable(winnerId)
                        .map(id -> id == Constants.PLAYER_ONE_ID ? mancalaGame.getPlayerOneTotalPebbles() : mancalaGame.getPlayerTwoTotalPebbles())
                        .orElse(0))
                .message("")
                .build();
    }
}
