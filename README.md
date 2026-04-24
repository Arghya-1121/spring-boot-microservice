# Backend Engineering Assignment – Core API & Redis Guardrails

## Overview

This project is a Spring Boot microservice that acts as a central API layer with Redis-based guardrails to control concurrency, enforce limits, and compute real-time metrics.

The system ensures:

* Safe handling of concurrent bot interactions
* Real-time virality scoring using Redis
* Strict enforcement of constraints (caps & cooldowns)

---

## Tech Stack

* Java 17
* Spring Boot 3.x
* PostgreSQL (persistent storage)
* Redis (in-memory state & guardrails)

---

## System Design

### Architecture Flow

```
Client Request → Service Layer → Redis (Guardrails) → PostgreSQL
```

* **PostgreSQL** → source of truth (posts, comments, users)
* **Redis** → handles:

  * concurrency control
  * rate limiting
  * real-time scoring

---

## Features Implemented

### Phase 1: Core API

* Create Post → `POST /api/posts`
* Add Comment → `POST /api/posts/{postId}/comments`
* Like Post → `POST /api/posts/{postId}/like`

---

### Phase 2: Redis Virality Engine & Guardrails

#### 1. Virality Score (Real-Time)

Each interaction updates a Redis counter:

| Action        | Score |
| ------------- | ----- |
| Bot Reply     | +1    |
| Human Like    | +20   |
| Human Comment | +50   |

**Redis Key:**

```
post:{id}:virality_score
```

---

#### 2. Horizontal Cap (Concurrency Safe)

Limits bot replies per post to **100 max**

**Redis Key:**

```
post:{id}:bot_count
```

**Logic:**

* Uses atomic `INCR`
* Rejects requests if count > 100
* Prevents race conditions during concurrent requests

---

#### 3. Vertical Cap

Limits comment thread depth:

```
depth_level <= 20
```

---

#### 4. Cooldown Cap (Rate Limiting)

Prevents repeated bot interaction with same user within 10 minutes

**Redis Key:**

```
cooldown:bot_{botId}:human_{userId}
```

**Implementation:**

* Uses Redis TTL (10 minutes)
* Blocks request if key exists

---

## Approach & Thread Safety (Phase 2 – Atomic Locks)

### Design Philosophy

The system is designed to be **fully stateless**, where:

* PostgreSQL stores persistent data (posts, comments)
* Redis acts as a **real-time gatekeeper** for enforcing constraints

All guardrails are enforced **before committing to the database**, ensuring invalid operations never reach persistent storage.

---

### Ensuring Thread Safety with Redis

To handle concurrent requests safely (e.g., 200 bots commenting at the same time), Redis atomic operations were used.

---

### 1. Horizontal Cap (Max 100 Bot Replies)

**Requirement:**
A post must not have more than 100 bot replies.

**Solution:**

* Used Redis `INCR` operation on:

  ```
  post:{id}:bot_count
  ```
* `INCR` is atomic → multiple concurrent requests cannot corrupt the counter

**Implementation Logic:**

1. Increment counter using `INCR`
2. If value exceeds 100:

   * Immediately decrement (`DECR`)
   * Reject request (HTTP 429)

**Why this is thread-safe:**

* `INCR` guarantees atomicity
* No race condition even under high concurrency
* Ensures strict enforcement of the limit

---

### 2. Cooldown Cap (Bot ↔ User Interaction)

**Requirement:**
A bot cannot interact with the same user more than once within 10 minutes.

**Solution:**

* Used Redis key with TTL:

  ```
  cooldown:bot_{botId}:human_{userId}
  ```

**Implementation Logic:**

1. Check if key exists
2. If exists → reject request
3. If not:

   * Allow interaction
   * Set key with 10-minute TTL

**Why this is thread-safe:**

* Redis key existence + TTL is atomic at the operation level
* Prevents duplicate interactions within cooldown window

---

### 3. Virality Score (Real-Time Updates)

**Requirement:**
Update score instantly based on interactions.

**Solution:**

* Used atomic `INCRBY`:

  ```
  post:{id}:virality_score
  ```

**Why this is safe:**

* Atomic increment ensures accurate scoring under concurrent updates

---

### 4. Vertical Cap (Depth Limit)

**Requirement:**
Comment depth must not exceed 20.

**Solution:**

* Simple validation before processing request

---

### Key Principles Followed

* All critical counters handled via Redis (not Java memory)
* No shared in-memory state → fully stateless design
* Database writes happen only after Redis validation
* Atomic operations (`INCR`, TTL) prevent race conditions

---

### Result

The system guarantees:

* Strict enforcement of limits under high concurrency
* No race conditions
* Consistent and reliable behavior even during simultaneous requests

---

This approach ensures that even under the “200 concurrent bot requests” test, the system will never exceed the defined limits.

---

## Database vs Redis Responsibilities

| Component  | Responsibility                           |
| ---------- | ---------------------------------------- |
| PostgreSQL | Persistent data (posts, comments, users) |
| Redis      | Counters, limits, cooldowns              |

---

## Running the Project

### 1. Start Services (Docker)

```bash
docker-compose up -d
```

---

### 2. Run Spring Boot App

```bash
./mvnw spring-boot:run
```

---

## API Usage

### Create Post

```
POST /api/posts
```

---

### Add Comment

```
POST /api/posts/{postId}/comments
```

---

### Like Post

```
POST /api/posts/{postId}/like
```

---

## Testing Suggestions

* Send multiple bot comment requests concurrently
* Verify bot replies stop at exactly **100**
* Test cooldown by repeating bot interaction
* Validate virality score updates in Redis

---

## Notes

* Application is fully stateless (no in-memory storage)
* Redis acts as a real-time gatekeeper
* Database writes happen only after passing Redis checks

---

## Deliverables Included

* Spring Boot source code
* Docker setup for PostgreSQL & Redis
* REST endpoints for testing

---

## Summary

This project demonstrates:

* Use of Redis for concurrency control
* Real-time event scoring
* Rate limiting using TTL
* Stateless backend design

