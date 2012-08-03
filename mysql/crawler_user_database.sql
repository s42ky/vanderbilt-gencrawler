-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Aug 03, 2012 at 10:52 AM
-- Server version: 5.5.22-log
-- PHP Version: 5.3.14-pl0-gentoo

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `crawler_users`
--
CREATE DATABASE `crawler_users` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `crawler_users`;

-- --------------------------------------------------------

--
-- Table structure for table `_users`
--

CREATE TABLE IF NOT EXISTS `_users` (
  `uid` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `first_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `last_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `organization` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=35 ;

--
-- Dumping data for table `_users`
--

INSERT INTO `_users` (`uid`, `email`, `first_name`, `last_name`, `organization`) VALUES
(1, 'Wanda.Granat@msn.com', 'Wanda', 'Granat', 'TJU'),
(2, 'Lol.Cat@msn.com', 'Lol', 'Cat', 'TJU'),
(3, 'Will.Holcomb@gmail.com', 'Will', 'Holcomb', 'TJU'),
(4, 'Alex.Willingham@gmail.com', 'Alex', 'Willingham', 'TJU'),
(5, 'Moa.Mao@msn.com', 'Moa', 'Mao', 'TJU'),
(6, 'Craig.Verzosa@msn.com', 'Craig', 'Verzosa', 'TJU'),
(7, 'Thomas.Griffith@msn.com', 'Thomas', 'Griffith', 'TJU'),
(8, 'lixwhome@gmail.com', 'richard', 'lee', 'TJU'),
(9, 'CAlvin.Watters@gmail.com', 'Calvin', 'Watters', 'TJU'),
(10, 'Wen.Zhang@qq.com', 'Wen', 'Zhang', 'TJU'),
(11, 'Bob.Gilbert@gmail.com', 'Bob', 'Gilbert', 'TJU'),
(12, 'Ryan.Millay@msn.com', 'Ryan', 'Millay', 'TJU'),
(13, 'admin1@gmail.com', 'admin', 'admin', 'TJU'),
(14, 'lixw@msn.com', 'xiaowei', 'li', 'TJU'),
(15, 'Foo@Bar.com', 'Foo', 'Bar', 'TJA'),
(16, 'AAnother.Email', 'Invalid', 'Email', 'TJU'),
(17, 'Test.User@test.com', 'Test', 'User', 'TJB'),
(18, 'Sara.Morimoto@gmail.com', 'Sara', 'Morimoto', 'TJU'),
(19, 'Bryce.Boe@gmail.com', 'Bryce', 'Boe', 'TJU'),
(20, 'Evan.Kaiser@msn.com', 'Evan', 'Kaiser', 'TJU'),
(21, 'Mohammad.Mulla@gmail.com', 'Mohammad', 'Mulla', 'TJU'),
(22, 'Bobby.McDonald@gmail.com', 'Bobby', 'McDonald', 'TJU'),
(23, 'Anushi.Shah@gmail.com', 'Anushi', 'Shah', 'TJU'),
(24, 'David.Zheng@qq.com', 'David', 'Zheng', 'TJU'),
(25, 'Tux@linux.net', 'Tux', 'Penguin', 'TJK'),
(26, 'Wei.Yan@qq.com', 'Wei', 'Yan', 'TJU'),
(27, 'Hacker.McHackerson@msn.com', 'Hacker', 'McHackerson', 'TJU'),
(28, 'MOA.mao@qq.com', 'MOa', 'Mao', 'TJU'),
(29, 'test@gmail.com', 'test', 'hacker', 'TJU'),
(30, 'lixwbupt@gmail.com', 'villa', 'Vanderbilt', 'TJU'),
(31, 'Hacking@.com', 'A', 'Hacker', 'IA9'),
(32, 'Demetri.Miller@gmail.com', 'Demetri', 'Miller', 'TJU'),
(33, 'Will.Chappell@gmail.com', 'Will', 'Chappell', 'TJU'),
(34, 'Jiannian.Weng@qq.com', 'Jiannian', 'Weng', 'TJU');

-- --------------------------------------------------------

--
-- Table structure for table `scarf`
--

CREATE TABLE IF NOT EXISTS `scarf` (
  `uid` int(11) NOT NULL,
  `level` int(11) NOT NULL DEFAULT '1',
  `password` varchar(30) NOT NULL DEFAULT '''12345''',
  UNIQUE KEY `uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `scarf`
--

INSERT INTO `scarf` (`uid`, `level`, `password`) VALUES
(1, 1, '12345678'),
(2, 2, '12345678'),
(3, 1, '12345678'),
(4, 2, '12345678'),
(5, 2, '12345678'),
(6, 1, '12345678'),
(7, 1, '12345678'),
(8, 1, '12345678'),
(9, 1, '12345678'),
(10, 1, '12345678'),
(11, 2, '12345678'),
(12, 1, '12345678'),
(13, 1, '12345678'),
(14, 1, '12345678'),
(15, 1, '12345678'),
(16, 1, '12345678'),
(17, 1, '12345678'),
(18, 1, '12345678'),
(19, 1, '12345678'),
(20, 1, '12345678'),
(21, 1, '12345678'),
(22, 1, '12345678'),
(23, 1, '12345678'),
(24, 1, '12345678'),
(25, 1, '12345678'),
(26, 1, '12345678'),
(27, 1, '12345678'),
(28, 1, '12345678'),
(29, 1, '12345678'),
(30, 1, '12345678'),
(31, 1, '12345678'),
(32, 1, '12345678'),
(33, 2, '12345678'),
(34, 2, '12345678');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(200) NOT NULL,
  `password` varchar(50) NOT NULL,
  `firstname` varchar(200) NOT NULL,
  `lastname` varchar(200) NOT NULL,
  `affiliation` varchar(200) NOT NULL,
  `showemail` tinyint(1) NOT NULL DEFAULT '0',
  `privilege` enum('admin','user') NOT NULL DEFAULT 'user',
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=197 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `email`, `password`, `firstname`, `lastname`, `affiliation`, `showemail`, `privilege`) VALUES
(68, 'admin1@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'admin', 'admin', 'TJU', 1, 'user'),
(90, 'Wanda.Granat@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Wanda', 'Granat', 'TJU', 0, 'user'),
(70, 'Jiannian.Weng@qq.com', '25d55ad283aa400af464c76d713c07ad', 'Jiannian', 'Weng', 'TJU', 0, 'admin'),
(51, 'Moa.Mao@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Moa', 'Mao', 'TJU', 0, 'admin'),
(45, 'lixw@msn.com', '25d55ad283aa400af464c76d713c07ad', 'xiaowei', 'li', 'TJU', 0, 'user'),
(69, 'Demetri.Miller@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Demetri', 'Miller', 'TJU', 0, 'user'),
(46, 'Bobby.McDonald@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Bobby', 'McDonald', 'TJU', 1, 'user'),
(37, 'Will.Chappell@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Anushi Shah', 'Ryan Millay', 'TJU', 0, 'admin'),
(55, 'lixwhome@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Ryan Millay', 'Hacker McHackerson', 'TJU', 1, 'user'),
(88, 'Ryan.Millay@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Ryan', 'Millay', 'TJU', 1, 'user'),
(56, 'Lol.Cat@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Lol', 'Cat', 'TJU', 1, 'admin'),
(50, 'Alex.Willingham@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Alex', 'Willingham', 'TJU', 1, 'admin'),
(59, 'Wen.Zhang@qq.com', '25d55ad283aa400af464c76d713c07ad', 'Wen', 'Zhang', 'TJU', 1, 'user'),
(19, 'Thomas.Griffith@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Thomas', 'Griffith', 'TJU', 1, 'user'),
(57, 'Bob.Gilbert@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Hacker McHackerson', 'Will Holcomb', 'TJU', 0, 'admin'),
(60, 'CAlvin.Watters@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Calvin', 'Watters', 'TJU', 1, 'user'),
(87, 'Will.Holcomb@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Mohammad Mulla', 'Will Holcomb', 'TJU', 1, 'user'),
(89, 'Craig.Verzosa@msn.com', '2ad9b22e6f6342d22c66cecea838d545', 'Craig', 'Verzosa', 'TJU', 0, 'user'),
(84, 'test@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Will Holcomb', 'Mohammad Mulla', 'TJU', 0, 'user'),
(91, 'Foo@Bar.com', '46972de0a3874e1ca497ddf45f6dd6a7', 'Foo', 'Bar', 'TJA', 0, 'user'),
(92, 'Test.User@test.com', '13a7ce9a31f77aaf9a7bdbedeae19543', 'Test', 'User', 'TJB', 0, 'user'),
(93, 'Sara.Morimoto@gmail.com', '5097be9c79fda4935a3647bd7b386260', 'Sara', 'Morimoto', 'TJU', 0, 'user'),
(94, 'Bryce.Boe@gmail.com', '4b87dd3662a08915f6fb111a17023383', 'Bryce', 'Boe', 'TJU', 0, 'user'),
(95, 'Evan.Kaiser@msn.com', 'd6d4815441ab9d0461385665cdc0ff73', 'Evan', 'Kaiser', 'TJU', 0, 'user'),
(96, 'Mohammad.Mulla@gmail.com', '1c1e91ce40b92cac025c4ca8651a0a06', 'Mohammad', 'Mulla', 'TJU', 0, 'user'),
(97, 'Anushi.Shah@gmail.com', '0683e5d108e1d88f23ede3475b20d9d7', 'Anushi', 'Shah', 'TJU', 0, 'user'),
(98, 'David.Zheng@qq.com', '39410dc3a3e4af0a27788a2a2572d9a9', 'David', 'Zheng', 'TJU', 0, 'user'),
(99, 'Tux@linux.net', '8d882cd69d0909563ecf20df0b43422e', 'Tux', 'Penguin', 'TJK', 0, 'user'),
(100, 'Wei.Yan@qq.com', '4a03846afe768eab32b783f67abf71d3', 'Wei', 'Yan', 'TJU', 0, 'user'),
(101, 'Hacker.McHackerson@msn.com', 'a8bd35d11e77eb3853d9847ddbca2957', 'Hacker', 'McHackerson', 'TJU', 0, 'user'),
(102, 'MOA.mao@qq.com', '05b983433190c9a15d7e69981996e0c9', 'MOa', 'Mao', 'TJU', 0, 'user'),
(103, 'lixwbupt@gmail.com', '167778b11a67c8f763a779e981e4d97c', 'villa', 'Vanderbilt', 'TJU', 0, 'user');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `scarf`
--
ALTER TABLE `scarf`
  ADD CONSTRAINT `scarf_ibfk_1` FOREIGN KEY (`uid`) REFERENCES `_users` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
