package com.rex.linebotgame1.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
public class DrawGameModel {
    private String id;
    private String answer;
    private List<String> prompts;
    private  Integer level;
}
