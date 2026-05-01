package com.candle.mapper;

import com.candle.model.Candle;
import com.candle.model.HistoryResponse;

import java.util.ArrayList;
import java.util.List;

public class HistoryMapper {

    private HistoryMapper() {
        // Utility class, do not instantiate
    }

    /**
     * Converts a list of standard Candles into the highly compressed
     * array-based HistoryResponse format required by the REST API.
     */
    public static HistoryResponse toResponse(List<Candle> candles) {
        List<Long> t = new ArrayList<>(candles.size());
        List<Double> o = new ArrayList<>(candles.size());
        List<Double> h = new ArrayList<>(candles.size());
        List<Double> l = new ArrayList<>(candles.size());
        List<Double> c = new ArrayList<>(candles.size());
        List<Long> v = new ArrayList<>(candles.size());

        for (Candle candle : candles) {
            t.add(candle.time());
            o.add(candle.open());
            h.add(candle.high());
            l.add(candle.low());
            c.add(candle.close());
            v.add(candle.volume());
        }

        return new HistoryResponse("ok", t, o, h, l, c, v);
    }
}
