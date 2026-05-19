import json
import random
import signal
import sys
import time
from dataclasses import dataclass, asdict
from datetime import datetime, timezone

from kafka import KafkaProducer


KAFKA_BOOTSTRAP_SERVERS = ["localhost:9092"]
TELEMETRY_TOPIC = "robot.telemetry"
PUBLISH_INTERVAL_SECONDS = 1

STATUSES = ["IDLE", "MOVING", "CHARGING", "ERROR"]


@dataclass
class RobotState:
    robotId: str
    x: float
    y: float
    battery: float
    status: str
    timestamp: str


class RobotSimulator:
    def __init__(self, robot_id: str):
        self.robot_id = robot_id
        self.x = random.uniform(0, 100)
        self.y = random.uniform(0, 100)
        self.battery = random.uniform(60, 100)
        self.status = random.choice(["IDLE", "MOVING"])

    def next_state(self) -> RobotState:
        if self.status == "MOVING":
            self.x = max(0.0, min(100.0, self.x + random.uniform(-3, 3)))
            self.y = max(0.0, min(100.0, self.y + random.uniform(-3, 3)))
            self.battery = max(0.0, self.battery - random.uniform(0.3, 1.5))
        elif self.status == "CHARGING":
            self.battery = min(100.0, self.battery + random.uniform(1.0, 3.0))
        else:
            self.battery = max(0.0, self.battery - random.uniform(0.05, 0.2))

        if self.battery < 15:
            self.status = "CHARGING"
        elif self.battery > 90 and self.status == "CHARGING":
            self.status = "IDLE"
        elif random.random() < 0.07:
            self.status = random.choice(STATUSES)

        return RobotState(
            robotId=self.robot_id,
            x=round(self.x, 2),
            y=round(self.y, 2),
            battery=round(self.battery, 2),
            status=self.status,
            timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        )


running = True


def shutdown_handler(signum, frame):
    global running
    print("\nShutdown signal received. Stopping simulator...")
    running = False


def main():
    signal.signal(signal.SIGINT, shutdown_handler)
    signal.signal(signal.SIGTERM, shutdown_handler)

    print(f"Connecting to Kafka at {KAFKA_BOOTSTRAP_SERVERS}...")
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        linger_ms=10,
    )

    robots = [RobotSimulator(f"robot-{i}") for i in range(1, 4)]
    print("Started simulator for robots: robot-1, robot-2, robot-3")
    print(f"Publishing telemetry to topic '{TELEMETRY_TOPIC}' every {PUBLISH_INTERVAL_SECONDS}s")

    try:
        while running:
            for robot in robots:
                telemetry = asdict(robot.next_state())
                producer.send(TELEMETRY_TOPIC, telemetry)
                print(f"Published: {telemetry}")

            producer.flush()
            time.sleep(PUBLISH_INTERVAL_SECONDS)
    except Exception as ex:
        print(f"Simulator failed: {ex}")
        sys.exit(1)
    finally:
        producer.close()
        print("Simulator stopped.")


if __name__ == "__main__":
    main()
