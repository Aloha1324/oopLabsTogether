package com.example.LAB5.framework.service;

import io.jsonwebtoken.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WordleService {

    // ✅ Карта: userId -> GameState
    private final Map<String, GameSession> userGames = new ConcurrentHashMap<>();

    private static final List<String> WORDS;

    static {
        try {
            ClassPathResource resource = new ClassPathResource("russian_nouns_5word.txt");
            WORDS = Files.readAllLines(resource.getFile().toPath())
                    .stream()
                    .map(String::trim)
                    .filter(word -> word.length() == 5) // на всякий случай
                    .map(String::toUpperCase)
                    .toList(); // или .collect(Collectors.toList()) для Java <16
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить словарь: russian_nouns_5word.txt", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        if (WORDS.isEmpty()) {
            throw new RuntimeException("Словарь пуст!");
        }
    }

    private final Random random = new Random();

    public WordleGameState newGame(String userId) {
        String targetWord = WORDS.get(random.nextInt(WORDS.size()));
        GameSession session = new GameSession(targetWord);
        userGames.put(userId, session);
        return session.getState();
    }

    public WordleGuessResult guess(String userId, String guessWord) {
        GameSession session = userGames.get(userId);
        if (session == null) {
            return new WordleGuessResult("Сессия не найдена!", false, null);
        }

        return session.makeGuess(guessWord);
    }

    public WordleGameState getState(String userId) {
        GameSession session = userGames.get(userId);
        return session != null ? session.getState() : null;
    }

    // ✅ Внутренний класс сессии
    private static class GameSession {
        private final String targetWord;
        private int attemptsLeft = 6;

        public GameSession(String targetWord) {
            this.targetWord = targetWord;
        }

        public WordleGuessResult makeGuess(String guessWord) {
            if (attemptsLeft <= 0) {
                return new WordleGuessResult("Игра окончена!", false, null);
            }

            if (!WORDS.contains(guessWord)) {
                return new WordleGuessResult("Слово не в словаре!", false, null);
            }

            if (guessWord.length() != 5) {
                return new WordleGuessResult("Введите 5 букв!", false, null);
            }

            String[] status = getGuessStatus(guessWord);
            attemptsLeft--;
            boolean won = Arrays.stream(status).allMatch(s -> "green".equals(s));

            String message = won ? "🎉 Победа!" :
                    attemptsLeft == 0 ? "Игра окончена! Слово: " + targetWord : "";

            return new WordleGuessResult(message, won, status);
        }

        public WordleGameState getState() {
            return new WordleGameState(targetWord, attemptsLeft);
        }

        private String[] getGuessStatus(String guess) {
            String[] status = new String[5];
            Arrays.fill(status, "gray");

            for (int i = 0; i < 5; i++) {
                if (guess.charAt(i) == targetWord.charAt(i)) {
                    status[i] = "green";
                }
            }

            for (int i = 0; i < 5; i++) {
                if ("green".equals(status[i])) continue;

                for (int j = 0; j < 5; j++) {
                    if (targetWord.charAt(j) == guess.charAt(i) && !"green".equals(status[j])) {
                        status[i] = "yellow";
                        break;
                    }
                }
            }
            return status;
        }
    }

    // DTO классы (остаются те же)
    public static class WordleGameState {
        private final String targetWord;
        private final int attemptsLeft;

        public WordleGameState(String targetWord, int attemptsLeft) {
            this.targetWord = targetWord;
            this.attemptsLeft = attemptsLeft;
        }

        public String getTargetWord() { return targetWord; }
        public int getAttemptsLeft() { return attemptsLeft; }
    }

    public static class WordleGuessResult {
        private final String message;
        private final boolean won;
        private final String[] status;

        public WordleGuessResult(String message, boolean won, String[] status) {
            this.message = message;
            this.won = won;
            this.status = status;
        }

        public String getMessage() { return message; }
        public boolean isWon() { return won; }
        public String[] getStatus() { return status; }
    }
}
