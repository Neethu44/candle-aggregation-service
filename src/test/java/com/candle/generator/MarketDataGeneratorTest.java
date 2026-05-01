package com.candle.generator;

import com.candle.model.BidAskEvent;
import com.candle.service.CandleAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class MarketDataGeneratorTest {

    private CandleAggregator candleAggregator;
    private MarketDataGenerator generator;

    @BeforeEach
    void setUp() {
        candleAggregator = mock(CandleAggregator.class);
        // Use a synchronous executor so the async tasks run immediately in the test thread
        Executor syncExecutor = Runnable::run;
        generator = new MarketDataGenerator(candleAggregator, syncExecutor);
    }

    @Test
    void testGenerateData() {
        // Trigger the scheduled task manually
        generator.generateData();

        // Capture the events sent to the aggregator
        ArgumentCaptor<BidAskEvent> eventCaptor = ArgumentCaptor.forClass(BidAskEvent.class);
        verify(candleAggregator, times(2)).processEvent(eventCaptor.capture());

        boolean hasBtc = false;
        boolean hasEth = false;

        for (BidAskEvent event : eventCaptor.getAllValues()) {
            if ("BTC-USD".equals(event.symbol())) {
                hasBtc = true;
                assertTrue(event.bid() > 0);
            } else if ("ETH-USD".equals(event.symbol())) {
                hasEth = true;
                assertTrue(event.ask() > 0);
            }
        }

        assertTrue(hasBtc);
        assertTrue(hasEth);
    }
}
