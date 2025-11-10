package com.rex.linebotgame1.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class WhoAmIGameModel {
    private String id;
    private String displayName;
    private List<String> answers;
    private List<String> prompts;
    private Integer level;

    private String questionImageUrl;
    private String promptImageUrl;
    private String answerImageUrl;
}
