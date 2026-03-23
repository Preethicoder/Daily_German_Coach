package com.dailygermancoach.controller;

import com.dailygermancoach.service.GermanService;
import com.dailygermancoach.model.Word;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Daily German Coach API - Simplified to 4 essential endpoints only.
 */
@RestController
@RequestMapping("/api/german")
public class GermanController {
    private final GermanService germanService;

    public GermanController(GermanService germanService) {
        this.germanService = germanService;
    }

    /**
     * 1. Save a German word with AI-generated translation and example sentence.
     * Expects JSON: {"german":"Hund"}
     * Returns: Word object with id, german, english, exampleSentence
     */
    @PostMapping("/enrich")
    public ResponseEntity<Word> enrichWord(@RequestBody Map<String, String> body) {
        String german = body.get("german");
        if (german == null || german.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Word saved = germanService.enrichAndSaveWord(german);
        return ResponseEntity.ok(saved);
    }

    /**
     * 2. Get all saved German words with their translations and example sentences.
     * Returns: List of all Word objects
     */
    @GetMapping("/words")
    public ResponseEntity<java.util.List<Word>> listWords() {
        return ResponseEntity.ok(germanService.listWordsAll());
    }

    /**
     * 3. Get a random word from database for practice.
     * Returns: One random Word object, or 204 No Content if database is empty
     */
    @GetMapping("/random")
    public ResponseEntity<Word> randomWord() {
        Word w = germanService.getRandomWord();
        if (w == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(w);
    }

    /**
     * 4. Check if user's German sentence is correct and uses the word properly.
     * Expects JSON: {"german":"Hund", "userSentence":"Der Hund spielt im Park"}
     * Returns: AI feedback in JSON format with correct/feedback/suggestion fields
     */
    @PostMapping("/check-sentence")
    public ResponseEntity<String> checkUserSentence(@RequestBody Map<String, String> body) {
        String german = body.get("german");
        String userSentence = body.get("userSentence");
        
        if (german == null || german.isBlank() || userSentence == null || userSentence.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Both 'german' and 'userSentence' are required\"}");
        }
        
        String feedback = germanService.checkUserSentence(german, userSentence);
        return ResponseEntity.ok(feedback);
    }
}
