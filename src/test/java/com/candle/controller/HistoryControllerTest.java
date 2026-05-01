package com.candle.controller;

import com.candle.model.Candle;
import com.candle.model.Interval;
import com.candle.service.CandleAggregator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CandleAggregator candleAggregator;

    @Test
    void testGetHistory_Success() throws Exception {
        when(candleAggregator.getHistory("BTC-USD", Interval.ONE_MIN, 100L, 200L))
                .thenReturn(List.of(new Candle(100L, 1.0, 2.0, 0.5, 1.5, 10L)));

        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "1m")
                .param("from", "100")
                .param("to", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s").value("ok"))
                .andExpect(jsonPath("$.t[0]").value(100))
                .andExpect(jsonPath("$.o[0]").value(1.0))
                .andExpect(jsonPath("$.h[0]").value(2.0))
                .andExpect(jsonPath("$.l[0]").value(0.5))
                .andExpect(jsonPath("$.c[0]").value(1.5))
                .andExpect(jsonPath("$.v[0]").value(10));
    }

    @Test
    void testGetHistory_InvalidInterval() throws Exception {
        mockMvc.perform(get("/history")
                .param("symbol", "BTC-USD")
                .param("interval", "invalid_interval_code")
                .param("from", "100")
                .param("to", "200"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.s").value("error_invalid_interval"));
    }
}
