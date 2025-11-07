package com.rex.linebotgame1.enums;


public enum GameMessageType {
    MOVE,
    MESSAGE,
    JOIN,
    GUESS,
    UNKNOWN;

    public static GameMessageType from(String value) {
        if (value == null) return UNKNOWN;
        try {
            return GameMessageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}