package com.candle.service;

import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.model.Interval;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * In-memory thread-safe implementation of the CandleAggregator.
 * 
 * Uses a flattened ConcurrentHashMap to store symbols/intervals, and an inner 
 * ConcurrentSkipListMap to maintain time-ordered, highly concurrent access to the candles.
 */
@Service
public class InMemoryCandleAggregator implements CandleAggregator {

    // outer map key: symbol + "_" + interval code
    // inner map key: candle timestamp (unix seconds)
    private final ConcurrentHashMap<String, ConcurrentSkipListMap<Long, Candle>> store = new ConcurrentHashMap<>();

    @Override
    public void processEvent(BidAskEvent event) {
        long timeSec = event.timestamp() / 1000;
        double price = (event.bid() + event.ask()) / 2.0; // Mid price

        for (Interval interval : Interval.values()) {
            long bucketSec = (timeSec / interval.getSeconds()) * interval.getSeconds();
            String storeKey = getStoreKey(event.symbol(), interval);

            ConcurrentSkipListMap<Long, Candle> intervalMap = store.computeIfAbsent(storeKey, k -> new ConcurrentSkipListMap<>());

            intervalMap.compute(bucketSec, (k, existing) -> {
                if (existing == null) {
                    return new Candle(bucketSec, price, price, price, price, 1);
                } else {
                    return new Candle(
                            bucketSec,
                            existing.open(),
                            Math.max(existing.high(), price),
                            Math.min(existing.low(), price),
                            price, // close is the latest price
                            existing.volume() + 1
                    );
                }
            });
        }
    }

    @Override
    public List<Candle> getHistory(String symbol, Interval interval, long fromSec, long toSec) {
        String storeKey = getStoreKey(symbol, interval);
        ConcurrentSkipListMap<Long, Candle> intervalMap = store.get(storeKey);
        
        if (intervalMap == null) {
            return Collections.emptyList();
        }

        // Include fromSec, include toSec
        Map<Long, Candle> subMap = intervalMap.subMap(fromSec, true, toSec, true);
        return new ArrayList<>(subMap.values());
    }

    private String getStoreKey(String symbol, Interval interval) {
        return symbol + "_" + interval.getCode();
    }
}
