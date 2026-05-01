package com.candle.generator;

import com.candle.model.BidAskEvent;
import com.candle.service.CandleAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class MarketDataGenerator {

    private final CandleAggregator candleAggregator;
    private final Executor executor;

    private double currentBtcPrice = 60000.0;
    private double currentEthPrice = 3000.0;

    @Autowired
    public MarketDataGenerator(CandleAggregator candleAggregator, @Qualifier("boundedTaskExecutor") Executor executor) {
        this.candleAggregator = candleAggregator;
        this.executor = executor;
    }

    /**
     * Simulates high-frequency market data ingestion.
     * Scheduled to run every 100ms. ShedLock guarantees this only runs on a single
     * node in a multi-instance deployment. Events are dispatched asynchronously 
     * to prevent blocking the scheduler thread.
     */
    @Scheduled(fixedRate = 100) // Every 100ms
    @SchedulerLock(name = "generateDataLock", lockAtLeastFor = "50ms", lockAtMostFor = "200ms")
    public void generateData() {
        long timestamp = System.currentTimeMillis();

        CompletableFuture.runAsync(() -> {
            // Random walk for BTC
            currentBtcPrice = currentBtcPrice + (ThreadLocalRandom.current().nextDouble() - 0.5) * 10;
            double bid = currentBtcPrice - 1.0;
            double ask = currentBtcPrice + 1.0;
            candleAggregator.processEvent(new BidAskEvent("BTC-USD", bid, ask, timestamp));
        }, executor);

        CompletableFuture.runAsync(() -> {
            // Random walk for ETH
            currentEthPrice = currentEthPrice + (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;
            double bid = currentEthPrice - 0.5;
            double ask = currentEthPrice + 0.5;
            candleAggregator.processEvent(new BidAskEvent("ETH-USD", bid, ask, timestamp));
        }, executor);
    }
}
