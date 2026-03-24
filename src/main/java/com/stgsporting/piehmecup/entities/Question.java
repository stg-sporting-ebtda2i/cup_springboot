package com.stgsporting.piehmecup.entities;

import com.stgsporting.piehmecup.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Question {

    private Long id;
    private String title;
    private String picture;
    private QuestionType type;
    private Long coins;
    private Long order;
    private List<Option> options;
    private Object answers;

    public Question() {
        options = new ArrayList<>();
    }

    public static Question fromJson(JSONObject questionJson) {
        Question question = new Question();
        question.setId((Long) questionJson.get("id"));
        question.setTitle((String) questionJson.get("title"));
        question.setPicture((String) questionJson.get("picture"));
        question.setCoins((Long) questionJson.get("points"));
        question.setAnswers(questionJson.get("answers"));
        question.setType(QuestionType.fromId(Math.toIntExact((Long) questionJson.get("type"))));

        if (questionJson.get("order") != null) {
            question.setOrder((Long) questionJson.get("order"));
        }

        JSONArray optionsArray = (JSONArray) questionJson.get("options");
        if (optionsArray != null) {
            for (Object optionObject : optionsArray) {
                JSONObject optionJson = (JSONObject) optionObject;
                Option option = Option.fromJson(optionJson);
                question.addOption(option);
            }
        }

        return question;
    }

    public void addOption(Option option) {
        if (options == null) {
            options = new ArrayList<>();
        }

        options.add(option);
    }
}
