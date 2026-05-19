CREATE TABLE IF NOT EXISTS robot_state (
  robot_id TEXT PRIMARY KEY,
  x REAL NOT NULL,
  y REAL NOT NULL,
  battery REAL NOT NULL,
  status TEXT NOT NULL,
  timestamp TEXT NOT NULL
);

DELETE FROM robot_state;

INSERT INTO robot_state (robot_id, x, y, battery, status, timestamp)
VALUES ('robot-1', 12.34, 56.78, 87.1, 'MOVING', '2026-05-19 16:40:03.000');

INSERT INTO robot_state (robot_id, x, y, battery, status, timestamp)
VALUES ('robot-2', 88.00, 10.00, 65.5, 'IDLE', '2026-05-19 16:41:03.000');