package com.bol.games.mancala.controller;

import com.bol.games.mancala.model.MancalaGameState;
import com.bol.games.mancala.service.interfaces.MancalaGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/v1")
public class MancalaGameController {

    private MancalaGameService mancalaGameService;

    public MancalaGameController(MancalaGameService mancalaGameService) {
        this.mancalaGameService = mancalaGameService;
    }

    @PostMapping(value = "/play", produces = "application/json")
    @Operation(summary = "Action for playing the next hand.",
            description = "Any user can play the next hand by providing the start position.")
    public ResponseEntity<?> playHand(@Parameter(description = "Starting position for next hand", required = true) @RequestParam int position
            , @RequestBody MancalaGameState gameState) {
        return new ResponseEntity<>(mancalaGameService.playHand(position, gameState), HttpStatus.OK);
    }

    @GetMapping(value = "/reset", produces = "application/json")
    @Operation(summary = "Resets the game.",
            description = "Resets the game to the original configuration.")
    public ResponseEntity<?> resetBoard() {
        return new ResponseEntity<>(mancalaGameService.resetGame(), HttpStatus.OK);
    }

}
