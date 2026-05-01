package com.candle.mapper;

import com.candle.model.Candle;
import com.candle.model.HistoryResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryMapperTest {

    @Test
    void testToResponse_WithData() {
        Candle c1 = new Candle(100L, 1.0, 5.0, 0.5, 2.0, 10L);
        Candle c2 = new Candle(200L, 2.0, 6.0, 1.5, 3.0, 20L);

        HistoryResponse response = HistoryMapper.toResponse(Arrays.asList(c1, c2));

        assertEquals("ok", response.s());
        assertEquals(Arrays.asList(100L, 200L), response.t());
        assertEquals(Arrays.asList(1.0, 2.0), response.o());
        assertEquals(Arrays.asList(5.0, 6.0), response.h());
        assertEquals(Arrays.asList(0.5, 1.5), response.l());
        assertEquals(Arrays.asList(2.0, 3.0), response.c());
        assertEquals(Arrays.asList(10L, 20L), response.v());
    }

    @Test
    void testToResponse_EmptyList() {
        HistoryResponse response = HistoryMapper.toResponse(List.of());
        
        assertEquals("ok", response.s());
        assertEquals(0, response.t().size());
        assertEquals(0, response.o().size());
        assertEquals(0, response.h().size());
        assertEquals(0, response.l().size());
        assertEquals(0, response.c().size());
        assertEquals(0, response.v().size());
    }
}
