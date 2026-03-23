package com.dailygermancoach.service;

import com.dailygermancoach.model.SentenceResponse;
import com.dailygermancoach.model.Word;
import com.dailygermancoach.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * German Service - Simplified to support only 4 essential operations.
 */
@Service
public class GermanService {
    private final AiService aiService;
    private final WordRepository wordRepository;

    public GermanService(AiService aiService, WordRepository wordRepository) {
        this.aiService = aiService;
        this.wordRepository = wordRepository;
    }

    /**
     * Get a random word from the database for practice.
     */
    public Word getRandomWord() {
        List<Word> words = wordRepository.findAll();
        if (words == null || words.isEmpty()) return null;
        return words.get(new Random().nextInt(words.size()));
    }

    /**
     * Get all saved words with their translations and example sentences.
     */
    public List<Word> listWordsAll() {
        return wordRepository.findAll();
    }

    /**
     * Use AI to translate a German word and generate an example sentence, then save to database.
     */
    public Word enrichAndSaveWord(String german) {
        SentenceResponse resp = aiService.translateAndExample(german);
        Word w = new Word();
        w.setGerman(german);
        w.setEnglish(resp.getTranslation() == null ? "" : resp.getTranslation());
        w.setExampleSentence(resp.getSentence() == null ? "" : resp.getSentence());
        return wordRepository.save(w);
    }

    /**
     * Check if user's sentence is grammatically correct and uses the German word properly.
     */
    public String checkUserSentence(String german, String userSentence) {
        return aiService.checkUserSentence(german, userSentence);
    }
}
