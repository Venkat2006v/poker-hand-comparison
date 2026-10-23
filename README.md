# Poker Hand Comparison API

A Spring Boot REST service that compares 2–4 poker hands using four hand categories: Straight, Three of a Kind, Two of a Kind (Pair), and High Card. Each hand has 5 or 7 cards; for 7-card hands it finds the best five-card hand.

## Requirements

- JDK 21 or later
- Maven 3.9 or later

## Build and test

```bash
mvn clean test
mvn package
```

## Run

```bash
mvn spring-boot:run
```

The service listens on `http://localhost:8080`.

## Compare hands

Send a `POST` request to `/api/poker/compare`. Cards use rank followed by suit: ranks `2`–`10`, `J`, `Q`, `K`, `A`; suits `C` (clubs), `D` (diamonds), `H` (hearts), `S` (spades). Card strings are case-insensitive.

```bash
curl -X POST http://localhost:8080/api/poker/compare \
  -H 'Content-Type: application/json' \
  -d '{
    "hands": [
      {"player": "A", "cards": ["5H", "6D", "7S", "8C", "9H"]},
      {"player": "B", "cards": ["KC", "KD", "KH", "2S", "3C"]}
    ]
  }'
```

The response includes all winner names (including ties) and each player's category and best five cards:

```json
{
  "winners": ["A"],
  "results": [
    {"player": "A", "category": "Straight", "bestFive": ["9H", "8C", "7S", "6D", "5H"]},
    {"player": "B", "category": "Three of a Kind", "bestFive": ["KC", "KD", "KH", "3C", "2S"]}
  ]
}
```

Invalid requests (wrong number of hands, hand sizes other than 5 or 7, malformed cards, duplicate cards within a hand, blank or duplicate player names, malformed JSON) return HTTP 400 with an `error` message.

## API documentation

While the service is running:

- Interactive Swagger UI: http://localhost:8080/swagger-ui.html. Open **POST /api/poker/compare**, click **Try it out**, pick a scenario from the **Examples** dropdown (winners, ties, kickers, 7-card hands, and invalid requests), and click **Execute**.
- OpenAPI document (JSON): http://localhost:8080/v3/api-docs

## How hands are evaluated

A request is handled in three steps:

1. **Check the request** (`PokerService`)
   - 2 to 4 hands, each with 5 or 7 valid cards, no repeated card within a hand, and a unique player name.
   - Anything invalid is rejected with HTTP 400 and an error message.
2. **Score each hand** (`HandEvaluator`)
   - Sort the cards into groups by rank (all the Sevens together, all the Kings together, and so on).
   - Pick the best five cards by checking each category from strongest to weakest and stopping at the first match:
     - **Straight**: five ranks in a row. If there is more than one, take the highest.
     - **Three of a Kind**: the highest rank with three cards.
     - **Two of a Kind (Pair)**: the highest rank with two cards.
     - **High Card**: none of the above.
   - Fill the remaining spots with the highest leftover cards (the "kickers").
   - This reads each card once (O(n)) instead of trying all 21 five-card combinations of a 7-card hand.
   - `ReferenceEvaluator` (test-only code) is there to prove the faster logic in `HandEvaluator`, the code the API actually runs, is right. It does it the slow way, trying every five-card combination (all 21 for a 7-card hand). On every build, a test scores 20,000 random 5- and 7-card hands both ways and fails if the results ever differ.
3. **Pick the winners** (`PokerService`)
   - Compare hands by category first. If the categories match, compare the deciding ranks from highest to lowest, for example the pair rank and then each kicker.
   - Every hand that ties for best is a winner.

## Assumptions and design notes

**Ranking rules**

- **Suits never decide a winner.** Two hands with the same ranks tie, whatever their suits.
- **Ace is high, except in `A-2-3-4-5`**, which counts as a five-high straight. Wrap-arounds like `Q-K-A-2-3` are not straights.
- **Hands outside the four categories score as the best of the four they qualify for:**
  - Four of a kind and full house → Three of a Kind
  - Two pair → a Pair of the higher rank, with the lower pair as kickers

**Input**

- **Cards** are written as rank then suit, for example `AS` or `10H`.
  - Ten is `10` (not `T`).
  - Upper or lower case both work, and extra spaces are ignored.
- **A card can't appear twice in the same hand**, but it can appear in different players' hands. Each hand is scored on its own, so callers can compare hypothetical hands.
- **Player names** must be non-empty and unique. They are trimmed and case-sensitive (`"a"` and `"A"` are different players).

**Responses and errors**

- **`results` lists every hand**, not just the winners, so the caller can see why each hand won or lost. Order matches the request.
- **For 7-card hands, only the best five are returned** in `bestFive`. The two unused cards don't affect the result, and the caller already has them.
- **The best five cards are always shown the same way.** If two cards of the same rank could fill a spot, the lower suit is used (C, D, H, S).
- **Every invalid request returns HTTP 400** with one `error` message for the first problem found. 400 fits because each case is a mistake in the request itself, and a single status keeps error handling simple.

## Improvements with more time

- Replace `IllegalArgumentException` with a dedicated validation exception, and add a catch-all handler so unexpected errors return a JSON 500 rather than leaking as 400s.
- Return all validation errors at once, and refer to hands by player name rather than "hand at index 0".
- Add the remaining poker categories (flush, full house, four of a kind, straight flush). The category and tiebreak-ranks model extends to them directly.
- Return a stable category code (e.g. `PAIR`) alongside the display label, so clients don't depend on label text.
- Return a human-readable description of each hand (e.g. "Pair of Queens, kickers 9, 4, 2") alongside the category.
- Optionally reject the same card appearing across different hands, if the API should model a single shared deck.
- Add request tracing and logging: an `X-Request-Id` header carried through the SLF4J MDC, with rejected requests logged at WARN.
- Add a Maven wrapper so the project builds without a local Maven install.
