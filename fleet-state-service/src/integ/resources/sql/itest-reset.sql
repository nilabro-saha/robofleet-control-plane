CREATE TABLE IF NOT EXISTS robot (
  robot_id TEXT PRIMARY KEY,
  display_name TEXT NULL
);

CREATE TABLE IF NOT EXISTS robot_state (
  robot_id TEXT PRIMARY KEY,
  x REAL NOT NULL,
  y REAL NOT NULL,
  battery REAL NOT NULL,
  status TEXT NOT NULL,
  timestamp TEXT NOT NULL,
  FOREIGN KEY (robot_id) REFERENCES robot(robot_id)
);

DELETE FROM robot_state;
DELETE FROM robot;