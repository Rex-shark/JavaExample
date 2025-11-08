package com.rex.linebotgame1.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class DrawGameModel {
    private String Id;
    private String answer;
    private List<String> prompts;
    private  Integer level;
}
