DROP TABLE IF EXISTS matchPlayer;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS match_;
--TODO DELETE ABOVE BEFORE RELEASE

CREATE TABLE IF NOT EXISTS match_
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dateTime TIMESTAMP NOT NULL,
  teamSize INT NOT NULL
);

CREATE TABLE IF NOT EXISTS player
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  plattformid BIGINT NOT NUll,
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
  FOREIGN KEY (playerid) REFERENCES player(id),
  FOREIGN KEY (matchid) REFERENCES match_(id),
  PRIMARY KEY (playerid,matchid)
);

