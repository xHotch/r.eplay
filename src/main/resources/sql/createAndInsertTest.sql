DROP TABLE IF EXISTS matchPlayer;
DROP TABLE IF EXISTS teamPlayer;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS match_;
DROP TABLE IF EXISTS team;

CREATE TABLE IF NOT EXISTS match_
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dateTime TIMESTAMP NOT NULL,
  teamSize INT NOT NULL,
  readId VARCHAR(50) NOT NULL UNIQUE,
  timeBallInBlueSide double,
  timeBallInRedSide double,
  possessionBlue int,
  possessionRed int,
  ballHeatmapFilename VARCHAR(80) NOT NULL
);

CREATE TABLE IF NOT EXISTS player
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  plattformid BIGINT NOT NUll,
  shown BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS matchPlayer
(
  playerid BIGINT,
  matchid  BIGINT,
  name VARCHAR(50) NOT NULL,
  team INT NOT NULL,
  score INT NOT NULL,
  goals INT NOT NULL,
  assists INT NOT NULL,
  saves INT NOT NULL,
  shots INT NOT NULL,
  airTime double,
  groundTime double,
  homeSideTime double,
  enemySideTime double,
  averageSpeed double,
  averageDistanceToBall double,
  heatmapFilename VARCHAR(80) NOT NULL,
  FOREIGN KEY (playerid) REFERENCES player(id),
  FOREIGN KEY (matchid) REFERENCES match_(id) on delete cascade,
  PRIMARY KEY (playerid,matchid)
);

CREATE TABLE IF NOT EXISTS team
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  teamSize INT NOT NULL
);

CREATE TABLE IF NOT EXISTS teamPlayer
(
  teamId BIGINT,
  playerId BIGINT,
  FOREIGN KEY (teamId) REFERENCES team(id) on delete cascade,
  FOREIGN KEY (playerId) REFERENCES player(id),
  PRIMARY KEY (teamId, playerId)
);