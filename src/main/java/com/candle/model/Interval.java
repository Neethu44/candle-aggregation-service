package com.candle.model;

public enum Interval {
    ONE_SEC("1s", 1),
    FIVE_SEC("5s", 5),
    ONE_MIN("1m", 60),
    FIFTEEN_MIN("15m", 900),
    ONE_HOUR("1h", 3600);

    private final String code;
    private final long seconds;

    Interval(String code, long seconds) {
        this.code = code;
        this.seconds = seconds;
    }

    public String getCode() {
        return code;
    }

    public long getSeconds() {
        return seconds;
    }

    public static Interval fromCode(String code) {
        for (Interval i : values()) {
            if (i.code.equals(code)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown interval: " + code);
    }
}
