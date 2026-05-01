# Candle Aggregation Service

A high-performance backend service built with Spring Boot that ingests a continuous stream of bid/ask market data, aggregates it into candlestick (OHLCV) format, and exposes a REST API for historical retrieval.

The system is designed to simulate real-world trading workloads with concurrent ingestion, low-latency processing, and efficient in-memory storage.

## Architecture Overview
```text
Market Data Generator
        ↓
Async Event Processing (Thread Pool)
        ↓
Candle Aggregator (OHLCV Computation)
        ↓
In-Memory Store (Concurrent Maps)
        ↓
REST API (/history)
```

## Tech Stack
- **Spring Boot** – REST API and application framework
- **ConcurrentHashMap** – Top-level structure for symbol + interval storage
- **ConcurrentSkipListMap** – Time-ordered candle storage with efficient range queries
- **CompletableFuture + ThreadPoolTaskExecutor** – Asynchronous event processing
- **Scheduled Executor** – Simulated high-frequency market data generator
- **ShedLock + H2** – Prevents duplicate scheduled execution across instances

## Core Design

### 1. Data Ingestion
- A scheduled background task generates bid/ask events at high frequency.
- Each event is processed asynchronously using a bounded thread pool to simulate real-world streaming ingestion.

### 2. Candle Aggregation Logic
Each incoming event contains:
- Symbol
- Bid price
- Ask price
- Timestamp (milliseconds)

The mid-price is calculated as: `(bid + ask) / 2`

Events are grouped into fixed time intervals (e.g., 1 minute):
- Timestamp is normalized to the interval start
- Each interval maintains:
  - **Open** → first price
  - **High** → max price
  - **Low** → min price
  - **Close** → last price
  - **Volume** → number of events in interval

### 3. In-Memory Storage Model
`ConcurrentHashMap<String, ConcurrentSkipListMap<Long, Candle>>`
- **Outer map** → Concatenated Symbol + Interval string (e.g. "BTC-USD_1m")
- **Inner map** → Sorted candles by timestamp

Enables:
- Thread-safe writes
- Efficient time-range queries (`subMap()`)

### 4. Concurrency Strategy
- Uses `ConcurrentSkipListMap.compute()` for atomic updates per candle
- Avoids coarse-grained locking
- Ensures high throughput under concurrent writes
- Bounded thread pool prevents resource exhaustion under load

## API

### Get Historical Candles
`GET /history`

**Query Parameters**
| Param | Description |
|-------|-------------|
| `symbol` | Trading pair (e.g., BTC-USD) |
| `interval` | Candle interval (e.g., 1m) |
| `from` | Start timestamp (epoch seconds) |
| `to` | End timestamp (epoch seconds) |

**Example Request**
```http
GET /history?symbol=BTC-USD&interval=1m&from=1620000000&to=1620000600
```

**Example Response**
```json
{
  "s": "ok",
  "t": [1620000000, 1620000060],
  "o": [29500.5, 29501.0],
  "h": [29510.0, 29505.0],
  "l": [29490.0, 29500.0],
  "c": [29505.0, 29502.0],
  "v": [10, 8]
}
```

**Response Fields**
- `t` → timestamps (start of interval)
- `o` → open prices
- `h` → high prices
- `l` → low prices
- `c` → close prices
- `v` → volume (number of events per interval)

## Running the Application

**Prerequisites**
- Java 17+
- Maven

**Build**
```bash
mvn clean install
```

**Run**
```bash
mvn spring-boot:run
```

Application starts at: `http://localhost:8080`

A background generator will begin simulating market data automatically.

## Running Tests
```bash
mvn test
```
Includes unit tests validating core aggregation logic.

## Bonus Features

### 1. Distributed Scheduler Safety
- ShedLock ensures only one instance runs the scheduled generator
- Backed by an H2 in-memory database

### 2. Asynchronous Processing
- Custom `ThreadPoolTaskExecutor`
- Max threads: 500
- Queue capacity: 750
- Uses `CompletableFuture.runAsync()` for event handling
- Simulates message-driven ingestion with backpressure control

### 3. Extensibility for Production
The system is designed to evolve into a scalable architecture:
- Replace in-memory store with:
  - TimescaleDB (time-series DB)
  - Redis (low-latency caching)
- Introduce:
  - Kafka / message broker for ingestion
  - Horizontal scaling by symbol partitioning
  - Periodic persistence using optimistic locking

## Assumptions & Trade-offs
- **In-Memory Storage**: Fast but not persistent. Suitable for simulation, not production-scale history.
- **Timestamp Precision**: Input: milliseconds. Output: normalized to seconds (interval-based).
- **Mid-Price Aggregation**: Uses `(bid + ask) / 2`. Simplifies candle generation vs separate bid/ask candles.

## Edge Cases Considered
- Concurrent updates to same candle interval
- High-frequency event bursts
- Empty intervals (not returned in response)
- Thread pool backpressure handling

## Limitations
- No long-term persistence
- No eviction strategy (memory grows over time)
- No handling for out-of-order or late-arriving events
- Single-node in-memory design (not horizontally partitioned)

## Future Improvements
- Add Kafka for real-time ingestion
- Implement Redis caching layer
- Persist completed candles to database
- Handle out-of-order events with watermarking
- Introduce retention and eviction policies

## Summary
This project demonstrates:
- Concurrent data processing in Java
- Efficient in-memory time-series aggregation
- Scalable system design thinking
- Production-aware trade-offs and extensibility
