package com.rex.linebotgame1.enums;

public enum SocketMessageType {
    SYSTEM,
    USER,
    UNKNOWN;

    public static SocketMessageType from(String value) {
        if (value == null) return UNKNOWN;
        try {
            return SocketMessageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
