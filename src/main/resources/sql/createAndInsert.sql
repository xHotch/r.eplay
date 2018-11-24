CREATE TABLE IF NOT EXISTS qanda
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question TEXT NOT NULL,
  answer TEXT NOT NULL
);

INSERT INTO qanda (question, answer) VALUES
('question of life, the universe, and everything', '42');