package com.example.LAB5.framework.controller;

import com.example.LAB5.framework.service.WordleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/wordle")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WordleController {

    @Autowired
    private WordleService wordleService;

    private final ThreadLocal<WordleService.WordleGameState> gameStateHolder =
            new ThreadLocal<>();

    @PostMapping("/new-game")
    public ResponseEntity<WordleService.WordleGameState> newGame(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String userId = principal.getName(); // ← Используем username из JWT
        WordleService.WordleGameState state = wordleService.newGame(userId);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/guess")
    public ResponseEntity<WordleService.WordleGuessResult> guess(
            @RequestBody WordleGuessRequest request, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String userId = principal.getName();
        WordleService.WordleGuessResult result = wordleService.guess(userId, request.getWord());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/state")
    public ResponseEntity<WordleService.WordleGameState> getState(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String userId = principal.getName();
        WordleService.WordleGameState state = wordleService.getState(userId);
        return state != null ? ResponseEntity.ok(state) : ResponseEntity.noContent().build();
    }


    // Внутренний класс для запроса
    static class WordleGuessRequest {
        private String word;

        public String getWord() {
            return word != null ? word.toUpperCase() : null;
        }
        public void setWord(String word) { this.word = word; }
    }
}
