package com.bol.games.mancala.controller;

import com.bol.games.mancala.model.MancalaGameState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MancalaGameControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenCallingResetEndpoint_thenAFreshBoardIsPresented() throws NullPointerException {
        ResponseEntity<MancalaGameState> entity = this.restTemplate.exchange(
                RequestEntity.get(uri("/v1/reset/")).header(HttpHeaders.ORIGIN, "http://localhost:8888").build(),
                MancalaGameState.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        MancalaGameState gameState = entity.getBody();
        assertNotNull(gameState);
        assertEquals(0, gameState.getTurnPlayer());
    }

    private URI uri(String path) {
        return restTemplate.getRestTemplate().getUriTemplateHandler().expand(path);
    }
}