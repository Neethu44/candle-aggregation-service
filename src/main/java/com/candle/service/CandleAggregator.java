package com.candle.service;

import com.candle.model.BidAskEvent;
import com.candle.model.Candle;
import com.candle.model.Interval;

import java.util.List;

/**
 * Defines the core contract for aggregating raw market data events
 * into time-bucketed Candlestick (OHLCV) structures.
 */
public interface CandleAggregator {

    /**
     * Processes a single bid/ask event, updating the relevant candle buckets
     * across all supported time intervals asynchronously.
     *
     * @param event The raw market data event containing symbol, bid, ask, and timestamp.
     */
    void processEvent(BidAskEvent event);

    /**
     * Retrieves historical candle data for a given symbol and interval within a time range.
     *
     * @param symbol   The trading pair symbol (e.g., "BTC-USD").
     * @param interval The timeframe interval (e.g., 1m, 1h).
     * @param fromSec  The inclusive start timestamp in UNIX seconds.
     * @param toSec    The inclusive end timestamp in UNIX seconds.
     * @return A time-ordered list of candles falling within the specified range.
     */
    List<Candle> getHistory(String symbol, Interval interval, long fromSec, long toSec);
}
