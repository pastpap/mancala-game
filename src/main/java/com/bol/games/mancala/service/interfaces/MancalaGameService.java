package com.bol.games.mancala.service.interfaces;

import com.bol.games.mancala.model.MancalaGameState;

public interface MancalaGameService {
    MancalaGameState playHand(int position, MancalaGameState gameState);
    MancalaGameState resetGame();
}
