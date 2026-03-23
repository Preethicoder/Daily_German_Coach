package com.dailygermancoach.model;

public class SentenceResponse {
    private String sentence;
    private String translation;

    public SentenceResponse() {}

    public SentenceResponse(String sentence, String translation) {
        this.sentence = sentence;
        this.translation = translation;
    }

    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }
}
