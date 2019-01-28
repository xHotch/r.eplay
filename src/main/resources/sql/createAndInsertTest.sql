
DROP TABLE IF EXISTS matchPlayer;
DROP TABLE IF EXISTS matchPlayerBoostPads;
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
  boostPerMinute double,
  boostPadAmount INT NOT NULL,
  timeLowBoost double,
  timeFullBoost double,
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

insert into player values(1,'Player 1', 123456, true);
insert into player values(2,'Player 2', 1234567, true);
insert into player values(3,'Player 3', 12345678, true);
insert into player values(4,'Player 4', 123456789, true);
insert into player values(5,'Player 5', 1234567890, false);
insert into player values(6,'Player 6', 1234561, false);

insert into team values(1, 'Team 1', 2);
insert into team values(2, 'Team 2', 2);

insert into teamPlayer values(1,1);
insert into teamPlayer values(1,2);
insert into teamPlayer values(2,3);
insert into teamPlayer values(2,4);

insert into match_ values(1, '2018-12-02 00:00:00', 2, '12345', 140.0, 150.0, 60, 40, 'match1.replay', 'match1.png', 350.0);

insert into matchPlayer values(1, 1, 'Player 1', 0, 100, 1, 0, 0, 2, 110.0, 195.0, 205.0, 95.0, 1300.0, 2400.0, 260.0, 50, 30.0, 50.0, '11.png');
insert into matchPlayer values(2, 1, 'Player 2', 0, 200, 1, 1, 0, 2, 115.0, 190.0, 200.0, 100.0, 1350.0, 2500.0,280.0, 55, 20.0, 60.0, '21.png');
insert into matchPlayer values(3, 1, 'Player 3', 1, 100, 0, 1, 0, 1, 112.0, 193.0, 200.0, 100.0, 1400.0, 2600.0,300.0, 60, 40.0, 70.0, '31.png');
insert into matchPlayer values(4, 1, 'Player 4', 1, 120, 1, 0, 1, 2, 110.0, 195.0, 190.0, 110.0, 1290.0, 2700.0, 310.0, 65, 35.0, 80.0, '41.png');

insert into matchPlayerBoostPads values(1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,3 ,3 ,3 , 3, 3, 3, 3);
insert into matchPlayerBoostPads values(2, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,3 ,3 ,3 , 3, 3, 3, 3);
insert into matchPlayerBoostPads values(3, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,3 ,3 ,3 , 3, 3, 3, 3);
insert into matchPlayerBoostPads values(4, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,3 ,3 ,3 , 3, 3, 3, 3);



insert into match_ values(2, '2018-12-03 00:00:00', 2, '123456', 142.0, 148.0, 58, 42, 'match2.replay', 'match2.png', 350.0);

insert into matchPlayer values(1, 2, 'Player 1', 0, 300, 3, 0, 0, 5, 108.0, 194.0, 205.0, 95.0, 1300.0, 2400.0,260.0, 50, 30.0, 50.0, '12.png');
insert into matchPlayer values(2, 2, 'Player 2', 0, 400, 1, 2, 0, 2, 112.0, 196.0, 200.0, 100.0, 1350.0, 2500.0,270.0, 60, 25.0, 70.0, '22.png');
insert into matchPlayer values(3, 2, 'Player 3', 1, 180, 0, 1, 0, 2, 116.0, 198.0, 200.0, 100.0, 1400.0, 2600.0,280.0, 70, 20.0, 55.0, '32.png');
insert into matchPlayer values(4, 2, 'Player 4', 1, 120, 1, 0, 1, 1, 111.0, 196.0, 190.0, 110.0, 1290.0, 2700.0,290.0, 80, 40.0, 56.0, '42.png');

insert into matchPlayerBoostPads values(1, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
insert into matchPlayerBoostPads values(2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
insert into matchPlayerBoostPads values(3, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
insert into matchPlayerBoostPads values(4, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
