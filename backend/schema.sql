CREATE DATABASE IF NOT EXISTS localisation CHARACTER SET utf8 COLLATE utf8_general_ci;
USE localisation;

CREATE TABLE IF NOT EXISTS `position` (
  `id`        INT(11)      NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `latitude`  DOUBLE       NOT NULL,
  `longitude` DOUBLE       NOT NULL,
  `date`      DATETIME     NOT NULL,
  `imei`      VARCHAR(20)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
