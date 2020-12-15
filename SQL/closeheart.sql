-- phpMyAdmin SQL Dump
-- version 5.0.4
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- 생성 시간: 20-12-15 21:06
-- 서버 버전: 10.3.27-MariaDB-0+deb10u1
-- PHP 버전: 7.3.19-1~deb10u1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 데이터베이스: `closeheart`
--

-- --------------------------------------------------------

--
-- 테이블 구조 `account`
--

CREATE TABLE `account` (
  `user_id` varchar(128) NOT NULL,
  `user_mail` varchar(256) NOT NULL,
  `user_pw` varchar(512) NOT NULL,
  `user_nick` varchar(128) NOT NULL,
  `user_birthday` date NOT NULL DEFAULT '1900-01-01',
  `user_statusmsg` varchar(128) NOT NULL DEFAULT '',
  `user_lasttime` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 테이블 구조 `covid19api`
--

CREATE TABLE `covid19api` (
  `date` varchar(8) NOT NULL,
  `decideCnt` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 테이블 구조 `friend`
--

CREATE TABLE `friend` (
  `user1_id` varchar(128) NOT NULL,
  `user2_id` varchar(128) NOT NULL,
  `type` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 테이블 구조 `session`
--

CREATE TABLE `session` (
  `user_id` varchar(128) NOT NULL,
  `token` varchar(64) NOT NULL,
  `clientIP` varchar(15) NOT NULL,
  `expiredTime` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 덤프된 테이블의 인덱스
--

--
-- 테이블의 인덱스 `account`
--
ALTER TABLE `account`
  ADD PRIMARY KEY (`user_id`);

--
-- 테이블의 인덱스 `covid19api`
--
ALTER TABLE `covid19api`
  ADD PRIMARY KEY (`date`);

--
-- 테이블의 인덱스 `friend`
--
ALTER TABLE `friend`
  ADD PRIMARY KEY (`user1_id`,`user2_id`),
  ADD KEY `user2fk` (`user2_id`);

--
-- 테이블의 인덱스 `session`
--
ALTER TABLE `session`
  ADD PRIMARY KEY (`token`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `user_id` (`user_id`);

--
-- 덤프된 테이블의 제약사항
--

--
-- 테이블의 제약사항 `friend`
--
ALTER TABLE `friend`
  ADD CONSTRAINT `user1fk` FOREIGN KEY (`user1_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user2fk` FOREIGN KEY (`user2_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 테이블의 제약사항 `session`
--
ALTER TABLE `session`
  ADD CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `account` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
