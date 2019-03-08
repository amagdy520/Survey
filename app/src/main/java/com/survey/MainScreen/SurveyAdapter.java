package com.survey.MainScreen;

/**
 * Created by Ahmed Magdy on 4/28/2018.
 */

public class SurveyAdapter {
    private String name;
    private String category;
    private String points;

    public SurveyAdapter() {
    }

    public SurveyAdapter(String name, String category, String points) {
        this.name = name;
        this.category = category;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
