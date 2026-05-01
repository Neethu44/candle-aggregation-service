package com.candle.model;

import java.util.List;

public record HistoryResponse(
    String s,
    List<Long> t,
    List<Double> o,
    List<Double> h,
    List<Double> l,
    List<Double> c,
    List<Long> v
) {
    /**
     * Creates an error response with empty arrays to maintain schema consistency.
     */
    public static HistoryResponse error(String status) {
        return new HistoryResponse(status, List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
