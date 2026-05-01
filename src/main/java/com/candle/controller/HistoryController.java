package com.candle.controller;

import com.candle.mapper.HistoryMapper;
import com.candle.model.Candle;
import com.candle.model.HistoryResponse;
import com.candle.model.Interval;
import com.candle.service.CandleAggregator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/history")
@Validated
public class HistoryController {

    private static final Logger log = LoggerFactory.getLogger(HistoryController.class);

    private final CandleAggregator candleAggregator;

    public HistoryController(CandleAggregator candleAggregator) {
        this.candleAggregator = candleAggregator;
    }

    /**
     * Retrieves historical OHLCV data in a highly compressed JSON array format
     * optimized for frontend charting libraries (like TradingView Lightweight Charts).
     *
     * @param symbol   The trading pair (e.g., "BTC-USD")
     * @param interval The timeframe (e.g., "1s", "1m", "1h")
     * @param from     The start timestamp in UNIX seconds
     * @param to       The end timestamp in UNIX seconds
     * @return ResponseEntity with HistoryResponse containing parallel arrays of time, open, high, low, close, and volume.
     */
    @GetMapping
    public ResponseEntity<HistoryResponse> getHistory(
            @RequestParam @NotBlank String symbol,
            @RequestParam @NotBlank String interval,
            @RequestParam @PositiveOrZero long from,
            @RequestParam @PositiveOrZero long to) {

        Interval enumInterval;
        try {
            enumInterval = Interval.fromCode(interval);
        } catch (IllegalArgumentException e) {
            log.warn("Rejected request: Invalid interval provided '{}' for symbol '{}'", interval, symbol);
            return ResponseEntity.badRequest().body(
                    new HistoryResponse("error_invalid_interval", List.of(), List.of(), List.of(), List.of(), List.of(), List.of())
            );
        }

        List<Candle> candles = candleAggregator.getHistory(symbol, enumInterval, from, to);

        return ResponseEntity.ok(HistoryMapper.toResponse(candles));
    }
}
