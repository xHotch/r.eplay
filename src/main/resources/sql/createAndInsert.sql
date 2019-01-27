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
  fileName VARCHAR(100) NOT NULL UNIQUE,
  ballHeatmapFilename VARCHAR(80) NOT NULL,
  matchTime double
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

CREATE TABLE IF NOT EXISTS matchPlayerBoostPads
(
  matchPlayerid BIGINT,
  matchid BIGINT,
  boostpad0 BIGINT, boostpad1 BIGINT, boostpad2 BIGINT, boostpad3 BIGINT, boostpad4 BIGINT, boostpad5 BIGINT,
  boostpad6 BIGINT, boostpad7 BIGINT, boostpad8 BIGINT, boostpad9 BIGINT, boostpad10 BIGINT, boostpad11 BIGINT,
  boostpad12 BIGINT, boostpad13 BIGINT, boostpad14 BIGINT, boostpad15 BIGINT, boostpad16 BIGINT, boostpad17 BIGINT,
  boostpad18 BIGINT, boostpad19 BIGINT, boostpad20 BIGINT, boostpad21 BIGINT, boostpad22 BIGINT, boostpad23 BIGINT,
  boostpad24 BIGINT, boostpad25 BIGINT, boostpad26 BIGINT, boostpad27 BIGINT, boostpad28 BIGINT, boostpad29 BIGINT,
  boostpad30 BIGINT, boostpad31 BIGINT, boostpad32 BIGINT, boostpad33 BIGINT,
  FOREIGN KEY (matchPlayerid) REFERENCES matchPlayer(playerid) on delete cascade,
  FOREIGN KEY (matchid) REFERENCES matchPlayer(matchid) on delete cascade,
  PRIMARY KEY (matchPlayerid, matchid)
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
