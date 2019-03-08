package com.survey.AddNewSurvey;

/**
 * Created by Ahmed Magdy on 4/27/2018.
 */

public class QuestionAdapter {
    private String question;
    private String format;
    private String from;
    private String to;
    private String label;

    public QuestionAdapter() {
    }

    public QuestionAdapter(String question, String format, String from, String to, String label) {
        this.question = question;
        this.format = format;
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
