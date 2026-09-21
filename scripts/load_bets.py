#!/usr/bin/env python3
"""Loads the f1-bets stack with sample data for manual/exploratory testing.

For every run it generates brand-new, random identifiers (event IDs, market
IDs, runner/outcome IDs, user IDs) - nothing is hardcoded - so the script can
be re-run repeatedly to keep piling fresh data onto a running stack.

What it does, in order:
  1. Creates N events (races), each with its own market and a pool of
     candidate "runners" (possible outcome/winner IDs).
  2. Registers `--bets-per-event` bets per event against
     `event-matcher-worker` (PUT /api/v1/bet). Roughly `--loss-rate` of the
     bets back a losing runner, the remainder back the runner that will be
     declared the winner.
  3. Publishes one event outcome per event against `event-outcome-producer`
     (POST /api/v1/event-outcome), revealing the winning runner chosen for
     that event in step 1.

Usage:
    python3 scripts/load_bets.py
    python3 scripts/load_bets.py --events 3 --bets-per-event 1000 --loss-rate 0.7
    python3 scripts/load_bets.py --matcher-url http://localhost:8082 \
                                  --producer-url http://localhost:8081

No third-party dependencies required (stdlib only).
"""

from __future__ import annotations

import argparse
import concurrent.futures
import json
import random
import sys
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass, field

DEFAULT_MATCHER_URL = "http://localhost:8082"
DEFAULT_PRODUCER_URL = "http://localhost:8081"

RACE_NAMES = [
    "Monaco Grand Prix",
    "British Grand Prix",
    "Italian Grand Prix",
    "Japanese Grand Prix",
    "Brazilian Grand Prix",
    "Belgian Grand Prix",
    "Singapore Grand Prix",
    "Abu Dhabi Grand Prix",
    "Australian Grand Prix",
    "Spanish Grand Prix",
]


@dataclass
class Event:
    """One generated race: its identifiers plus the runner pool bets can back."""

    event_id: str
    event_name: str
    event_market_id: str
    runner_ids: list[str]
    winner_id: str = field(init=False)

    def __post_init__(self) -> None:
        self.winner_id = random.choice(self.runner_ids)

    def loser_ids(self) -> list[str]:
        return [r for r in self.runner_ids if r != self.winner_id]


def new_uuid() -> str:
    return str(uuid.uuid4())


def generate_events(count: int, runners_per_event: int) -> list[Event]:
    names = random.sample(RACE_NAMES, k=min(count, len(RACE_NAMES)))
    while len(names) < count:
        names.append(f"Race #{new_uuid()[:8]}")

    events = []
    for name in names:
        runner_ids = [new_uuid() for _ in range(runners_per_event)]
        events.append(
            Event(
                event_id=new_uuid(),
                event_name=name,
                event_market_id=new_uuid(),
                runner_ids=runner_ids,
            )
        )
    return events


def random_amount() -> float:
    return round(random.uniform(1.0, 250.0), 2)


def build_bet_payload(event: Event, loss_rate: float) -> dict:
    is_losing_bet = random.random() < loss_rate
    picked_runner = random.choice(event.loser_ids()) if is_losing_bet else event.winner_id
    return {
        "userId": new_uuid(),
        "eventId": event.event_id,
        "eventMarketId": event.event_market_id,
        "eventWinnerId": picked_runner,
        "amount": random_amount(),
        "status": "PENDING"
    }


def post_json(url: str, payload: dict, method: str = "POST") -> tuple[int, str]:
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        url, data=data, method=method, headers={"Content-Type": "application/json"}
    )
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return response.status, response.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as exc:
        return exc.code, exc.read().decode("utf-8", errors="replace")
    except urllib.error.URLError as exc:
        return -1, str(exc.reason)


def submit_bets(matcher_url: str, events: list[Event], bets_per_event: int, loss_rate: float,
                 concurrency: int) -> tuple[int, int]:
    bet_endpoint = f"{matcher_url.rstrip('/')}/api/v1/bet"
    payloads = [
        build_bet_payload(event, loss_rate)
        for event in events
        for _ in range(bets_per_event)
    ]
    random.shuffle(payloads)

    succeeded = 0
    failed = 0
    total = len(payloads)

    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        futures = [pool.submit(post_json, bet_endpoint, payload, "PUT") for payload in payloads]
        for i, future in enumerate(concurrent.futures.as_completed(futures), start=1):
            status, body = future.result()
            if 200 <= status < 300:
                succeeded += 1
            else:
                failed += 1
                if failed <= 5:
                    print(f"  [bet failed] status={status} body={body[:200]!r}", file=sys.stderr)
            if i % 500 == 0 or i == total:
                print(f"  ...submitted {i}/{total} bets ({succeeded} ok, {failed} failed)")

    return succeeded, failed


def publish_outcomes(producer_url: str, events: list[Event]) -> tuple[int, int]:
    outcome_endpoint = f"{producer_url.rstrip('/')}/api/v1/event-outcome"
    succeeded = 0
    failed = 0
    for event in events:
        payload = {
            "eventId": event.event_id,
            "eventName": event.event_name,
            "eventWinnerId": event.winner_id,
        }
        status, body = post_json(outcome_endpoint, payload, "POST")
        if 200 <= status < 300:
            succeeded += 1
            print(f"  [outcome ok] event='{event.event_name}' id={event.event_id} winner={event.winner_id}")
        else:
            failed += 1
            print(f"  [outcome failed] event='{event.event_name}' status={status} body={body[:200]!r}",
                  file=sys.stderr)
    return succeeded, failed


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--matcher-url", default=DEFAULT_MATCHER_URL,
                         help=f"Base URL of event-matcher-worker (default: {DEFAULT_MATCHER_URL})")
    parser.add_argument("--producer-url", default=DEFAULT_PRODUCER_URL,
                         help=f"Base URL of event-outcome-producer (default: {DEFAULT_PRODUCER_URL})")
    parser.add_argument("--events", type=int, default=3, help="Number of events/races to generate (default: 3)")
    parser.add_argument("--bets-per-event", type=int, default=1000,
                         help="Number of bets to generate per event (default: 1000)")
    parser.add_argument("--loss-rate", type=float, default=0.7,
                         help="Fraction of bets that back a losing runner, 0-1 (default: 0.7)")
    parser.add_argument("--runners-per-event", type=int, default=6,
                         help="Number of candidate outcome/runner IDs per event (default: 6)")
    parser.add_argument("--concurrency", type=int, default=20,
                         help="Number of parallel HTTP requests when submitting bets (default: 20)")
    parser.add_argument("--seed", type=int, default=None, help="Optional random seed for reproducible runs")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.seed is not None:
        random.seed(args.seed)

    if not 0 <= args.loss_rate <= 1:
        sys.exit("--loss-rate must be between 0 and 1")
    if args.runners_per_event < 2:
        sys.exit("--runners-per-event must be at least 2 (one winner, one loser)")

    events = generate_events(args.events, args.runners_per_event)

    print(f"Generated {len(events)} event(s):")
    for event in events:
        print(f"  - {event.event_name} (id={event.event_id}, market={event.event_market_id}, "
              f"{len(event.runner_ids)} runners, secret winner={event.winner_id})")

    total_bets = args.events * args.bets_per_event
    print(f"\nSubmitting {total_bets} bets to {args.matcher_url} "
          f"(~{args.loss_rate:.0%} losing / {1 - args.loss_rate:.0%} winning)...")
    bets_ok, bets_failed = submit_bets(
        args.matcher_url, events, args.bets_per_event, args.loss_rate, args.concurrency
    )
    print(f"Bets done: {bets_ok} succeeded, {bets_failed} failed.")

    print(f"\nPublishing {len(events)} event outcome(s) to {args.producer_url}...")
    outcomes_ok, outcomes_failed = publish_outcomes(args.producer_url, events)
    print(f"Outcomes done: {outcomes_ok} succeeded, {outcomes_failed} failed.")

    if bets_failed or outcomes_failed:
        sys.exit(1)


if __name__ == "__main__":
    main()
