package com.candle.model;

public record BidAskEvent(String symbol, double bid, double ask, long timestamp) {
}
