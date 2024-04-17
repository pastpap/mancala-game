package com.bol.games.mancala.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MancalaGameState {
    private int[] board;
    private int turnPlayer;
    private boolean isFinished;
    private Integer winningPlayer;
    private Integer winningPlayerScore;
    private String message;
}
