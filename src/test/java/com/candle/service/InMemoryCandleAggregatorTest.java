package com.candle.service;

import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.model.Interval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryCandleAggregatorTest {

    private InMemoryCandleAggregator aggregator;

    @BeforeEach
    public void setup() {
        aggregator = new InMemoryCandleAggregator();
    }

    @Test
    public void testAggregation() {
        long baseTimeMs = 1620000000000L; // 1620000000 seconds
        
        // Event 1
        aggregator.processEvent(new BidAskEvent("BTC-USD", 100.0, 102.0, baseTimeMs)); // mid: 101.0
        // Event 2 (10 seconds later)
        aggregator.processEvent(new BidAskEvent("BTC-USD", 105.0, 107.0, baseTimeMs + 10000)); // mid: 106.0
        // Event 3 (20 seconds later)
        aggregator.processEvent(new BidAskEvent("BTC-USD", 90.0, 92.0, baseTimeMs + 20000)); // mid: 91.0
        // Event 4 (70 seconds later -> next 1m candle)
        aggregator.processEvent(new BidAskEvent("BTC-USD", 110.0, 112.0, baseTimeMs + 70000)); // mid: 111.0

        // Get 1m history
        List<Candle> history1m = aggregator.getHistory("BTC-USD", Interval.ONE_MIN, 1620000000L, 1620000070L);
        
        assertEquals(2, history1m.size());
        
        Candle firstCandle = history1m.get(0);
        assertEquals(1620000000L, firstCandle.time());
        assertEquals(101.0, firstCandle.open());
        assertEquals(106.0, firstCandle.high());
        assertEquals(91.0, firstCandle.low());
        assertEquals(91.0, firstCandle.close()); // last event in that minute
        assertEquals(3, firstCandle.volume());

        Candle secondCandle = history1m.get(1);
        assertEquals(1620000060L, secondCandle.time());
        assertEquals(111.0, secondCandle.open());
        assertEquals(111.0, secondCandle.high());
        assertEquals(111.0, secondCandle.low());
        assertEquals(111.0, secondCandle.close());
        assertEquals(1, secondCandle.volume());
    }
}
