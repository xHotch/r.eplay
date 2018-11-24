CREATE TABLE IF NOT EXISTS match
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dateTime TIMESTAMP NOT NULL,
  teamRedGoals INT NOT NULL,
  teamBlueGoals INT NOT NUll,
  teamSize INT NOT NULL
);

CREATE TABLE IF NOT EXISTS player
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name TEXT NOT NULL,
  team INT NOT NULL,
  score INT NOT NULL,
  goals INT NOT NULL,
  assists INT NOT NULL,
  saves INT NOT NULL,
  shots INT NOT NULL
);

CREATE TABLE IF NOT EXISTS playerInMatch
(
  playerid BIGINT,
  matchid  BIGINT,
  FOREIGN KEY (playerid) REFERENCES player(id),
  FOREIGN KEY (matchid) REFERENCES match(id),
  PRIMARY KEY (playerid,matchid)
);