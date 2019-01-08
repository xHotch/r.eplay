DROP TABLE IF EXISTS matchPlayer;
DROP TABLE IF EXISTS matchPlayerBoostPads;
DROP TABLE IF EXISTS teamPlayer;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS match_;
DROP TABLE IF EXISTS team;
--TODO DELETE ABOVE BEFORE RELEASE

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

CREATE TABLE IF NOT EXISTS matchPlayerBoostPads
(
  matchPlayerid BIGINT,
  matchid BIGINT,
  boostpad67 BIGINT, boostpad12 BIGINT, boostpad43 BIGINT, boostpad13 BIGINT, boostpad66 BIGINT, boostpad18 BIGINT,
  boostpad11 BIGINT, boostpad17 BIGINT, boostpad5 BIGINT, boostpad14 BIGINT, boostpad4 BIGINT, boostpad10 BIGINT,
  boostpad7 BIGINT, boostpad41 BIGINT, boostpad3 BIGINT, boostpad64 BIGINT, boostpad40 BIGINT, boostpad42 BIGINT,
  boostpad63 BIGINT, boostpad23 BIGINT, boostpad19 BIGINT, boostpad20 BIGINT, boostpad31 BIGINT, boostpad28 BIGINT,
  boostpad21 BIGINT, boostpad36 BIGINT, boostpad68 BIGINT, boostpad32 BIGINT, boostpad38 BIGINT, boostpad34 BIGINT,
  boostpad35 BIGINT, boostpad33 BIGINT, boostpad65 BIGINT, boostpad39 BIGINT,
  FOREIGN KEY (matchPlayerid) REFERENCES matchPlayer(playerid),
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

--insert test data


INSERT INTO match_(ID, DATETIME, TEAMSIZE, READID, TIMEBALLINBLUESIDE, TIMEBALLINREDSIDE, POSSESSIONBLUE, POSSESSIONRED, FILENAME, BALLHEATMAPFILENAME)
VALUES
(1,'2018-12-17 08:26:22',	2,	'D79D9E4846D829DDE58A55B387AFABB7',	146.46225040000002,	143.352579,	45,	55,	'WbULTRke_MonJan07214516CET2019_1546893916905.replay',	'D79D9E4846D829DDE58A55B387AFABB7_ball.png'),
(2,'2019-01-07 17:50:39',	1,	'B794BEF742404B561B5F499D6ED3CC4E',	75.32280860000003	,54.453483899999995	,43	,57,'ZPeVmmY4_MonJan07214558CET2019_1546893958190.replay',	'B794BEF742404B561B5F499D6ED3CC4E_ball.png'),
(3,'2018-12-18 01:30:09',	3,	'39AEB8954363EA02F9C8268FE3BACEC4',	121.68456459999994,	182.78150900000009,	47,	53,'TGdwFFsr_MonJan07215108CET2019_1546894268435.replay'	,'39AEB8954363EA02F9C8268FE3BACEC4_ball.png');

INSERT INTO player(ID,  	NAME,  	PLATTFORMID,  	SHOWN) VALUES
(1	,'M',	76561198061305670,	FALSE),
(2	,'Almanac',	76561198035782701,	FALSE),
(3	,'Anaconda',	76561197967049891,	FALSE),
(4	,'TexxyPoo',	76561198053694777,	FALSE),
(5	,'maiX',	76561198035374894,	FALSE),
(6	,'User45'	,76561198082530072,	FALSE),
(7	,'Twitchy',	76561198426628326,	TRUE),
(8	,'Comet',	76561198023180994,	TRUE),
(9	,'Halalbae',	76561198150156099	,TRUE),
(10	,'housei'	,76561198237829684	,TRUE),
(11	,'Mythologia''s End',	76561198077270070,	TRUE),
(12	,'Jube',	76561197991508698,	TRUE);

INSERT INTO team(ID,  	NAME,  	TEAMSIZE) VALUES
(1,	'Team 1',1),
(2,	'Team 2',2),
(3,	'Team 3',3);

INSERT INTO teamPlayer(TEAMID, PLAYERID) VALUES
(1,	7),
(2,	8),
(2,	9),
(3,	10),
(3,	11),
(3,	12);

INSERT INTO matchPlayer(PLAYERID, MATCHID, NAME, TEAM, SCORE, GOALS, ASSISTS, SAVES, SHOTS, AIRTIME, GROUNDTIME, HOMESIDETIME, ENEMYSIDETIME, AVERAGESPEED, AVERAGEDISTANCETOBALL, HEATMAPFILENAME) VALUES
(1	,1,	'M',                1,	590,  2,  0,  1,	5,  142.44376660000006, 155.1890804,	    190.46649960000008,	107.16634739999999,	1523.2280819004848,	2786.180183204073,	'D79D9E4846D829DDE58A55B387AFABB7_M.png'),
(2	,1,	'Almanac',        	0,	526,  2,  0,  2,	3,	107.85309519999997,	198.33993480000004,	185.48442620000003,	120.70860379999998,	1473.2755433112945,	2794.5345653016666,	'D79D9E4846D829DDE58A55B387AFABB7_Almanac.png'),
(3	,1,	'Anaconda',     	0,  404,  1,  1,  1,    2,  107.3516301	,       198.9859749000001,  199.4353433000001,  106.90226169999994, 1516.0443794726984, 3083.2820080315487, 'D79D9E4846D829DDE58A55B387AFABB7_Anaconda.png'),
(4	,1,	'TexxyPoo',     	1,  488,  2,  2,  0,    3,  102.16837340000006, 210.10738259999994, 189.15111959999996,	123.12463640000007, 1474.6476120409407,	3235.640623688722,  'D79D9E4846D829DDE58A55B387AFABB7_TexxyPoo.png'),
(5	,2,	'maiX',          	1,	1022, 7,  0,  0,	9,	65.26236670000004,  87.20742439999998,  85.34826339999996,  67.12152770000006,  1480.8239160574772, 1791.9893227618245, 'B794BEF742404B561B5F499D6ED3CC4E_maiX.png'),
(6	,2,	'User45',       	0,	546,  3,  0,  1,	4,	64.88408500000006,	87.16667109999997,	104.03684469999999,	48.013911400000055,	1429.8772304908812,	1536.832817797715,	'B794BEF742404B561B5F499D6ED3CC4E_User45.png'),
(7	,3,	'Twitchy',        	1,	231,  0,  0,  1,	0,	136.30325560000028,	233.13528419999975, 218.84014540000007, 150.59839439999996, 1501.368036955959,	4474.096157717324,  '39AEB8954363EA02F9C8268FE3BACEC4_Twitchy.png'),
(8	,3,	'Comet',        	1,	258,  1,  0,  1,	2,	128.14206790000003,	181.21348029999996,	206.10483749999997,	103.25071070000001,	1485.6344228517455,	4933.289978530231,  '39AEB8954363EA02F9C8268FE3BACEC4_Comet.png'),
(9	,3,	'Halalbae',     	1,	214,  0,  0,  1,	0,	112.67546870000004,	200.2835517,	    212.54818199999994, 100.4108384000001,  1433.9187410084246,	2805.6203476807004,	'39AEB8954363EA02F9C8268FE3BACEC4_Halalbae.png'),
(10	,3,	'housei',           0,	292,  1,  1,  0,	1,	127.3184501,	    185.56760629999997,	181.1062005999999,	131.77985580000006,	1582.8214186734097,	3157.2925210373774,	'39AEB8954363EA02F9C8268FE3BACEC4_housei.png'),
(11	,3,	'Mythologia''s End',0,	264,  0,  0,  1,	2,  139.74292079999998,	173.21658480000002,	178.96571280000006, 133.99379279999994,	1671.4306019377566,	2586.9907388591128, '39AEB8954363EA02F9C8268FE3BACEC4_Mythologia''s End.png'),
(12	,3,	'Jube',             0,  286,  1,  1,  0,	2,	117.22523960000004,	136.52965419999998,	126.63662809999995,	127.11826570000005,	1484.2819161131908,	3035.467402748257,	'39AEB8954363EA02F9C8268FE3BACEC4_Jube.png');

INSERT INTO matchPlayerBoostPads(MATCHPLAYERID, MATCHID, BOOSTPAD67, BOOSTPAD12, BOOSTPAD43, BOOSTPAD13, BOOSTPAD66,
BOOSTPAD18, BOOSTPAD11, BOOSTPAD17, BOOSTPAD5, BOOSTPAD14, BOOSTPAD4, BOOSTPAD10, BOOSTPAD7, BOOSTPAD41, BOOSTPAD3,
BOOSTPAD64, BOOSTPAD40, BOOSTPAD42, BOOSTPAD63, BOOSTPAD23, BOOSTPAD19, BOOSTPAD20, BOOSTPAD31, BOOSTPAD28, BOOSTPAD21,
BOOSTPAD36, BOOSTPAD68, BOOSTPAD32, BOOSTPAD38, BOOSTPAD34, BOOSTPAD35, BOOSTPAD33, BOOSTPAD65, BOOSTPAD39)
VALUES
(1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0),
(2,	1,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0),
(3,	1,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0),
(4,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0),
(5,	2,	0,	0,	0,	0,	0,	1,	1,	1,	0,	1,	1,	1,	1,	0,	0,	0,	1,	0,	1,	1,	1,	0,	0,	1,	1,	0,	1,	1,	1,	1,	1,	1,	1,	1),
(6,	2,	1,	0,	1,	0,	1,	0,	0,	0,	1,	0,	0,	0,	0,	1,	0,	0,	0,	1,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0),
(7,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	1,	0,	1,	1,	0,	0,	0,	1,	0,	0,	1,	0,	0),
(8,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	1,	0),
(9,	3,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	1,	1,	0,	0,	1,	1,	0,	0,	1),
(10,3,	0,	0,	0,	0,	0,	1,	1,	0,	0,	1,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0),
(11,3,	1,	1,	0,	1,	0,	0,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	1,	1,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0),
(12,3, 	0,	0,	1,	0,	1,	0,	0,	1,	1,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	1,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0,	0);