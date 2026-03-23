package com.dailygermancoach.model;

/**
 * Basic request model for sentence checking/generation.
 */
public class SentenceRequest {
    private String sentence;
    private String german;

    public SentenceRequest() {}

    public SentenceRequest(String sentence) { this.sentence = sentence; }

    public SentenceRequest(String sentence, String german) {
        this.sentence = sentence;
        this.german = german;
    }

    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }

    public String getGerman() { return german; }
    public void setGerman(String german) { this.german = german; }
}
