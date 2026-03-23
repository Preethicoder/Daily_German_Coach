package com.dailygermancoach.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Basic Word entity.
 */
@Entity
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String german;
    private String english;
    private String exampleSentence;

    public Word() {}

    public Word(Long id, String german, String english, String exampleSentence) {
        this.id = id;
        this.german = german;
        this.english = english;
        this.exampleSentence = exampleSentence;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGerman() { return german; }
    public void setGerman(String german) { this.german = german; }

    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }
}
