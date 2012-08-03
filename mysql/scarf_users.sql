-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Aug 03, 2012 at 10:57 AM
-- Server version: 5.5.22-log
-- PHP Version: 5.3.14-pl0-gentoo

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `scarf`
--

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
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=214 ;

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
(89, 'Craig.Verzosa@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Craig', 'Verzosa', 'TJU', 0, 'user'),
(84, 'test@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Will Holcomb', 'Mohammad Mulla', 'TJU', 0, 'user'),
(91, 'Foo@Bar.com', '25d55ad283aa400af464c76d713c07ad', 'Foo', 'Bar', 'TJA', 0, 'user'),
(92, 'Test.User@test.com', '25d55ad283aa400af464c76d713c07ad', 'Test', 'User', 'TJB', 0, 'user'),
(93, 'Sara.Morimoto@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Sara', 'Morimoto', 'TJU', 0, 'user'),
(94, 'Bryce.Boe@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Bryce', 'Boe', 'TJU', 0, 'user'),
(95, 'Evan.Kaiser@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Evan', 'Kaiser', 'TJU', 0, 'user'),
(96, 'Mohammad.Mulla@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Mohammad', 'Mulla', 'TJU', 0, 'user'),
(97, 'Anushi.Shah@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'Anushi', 'Shah', 'TJU', 0, 'user'),
(98, 'David.Zheng@qq.com', '25d55ad283aa400af464c76d713c07ad', 'David', 'Zheng', 'TJU', 0, 'user'),
(99, 'Tux@linux.net', '25d55ad283aa400af464c76d713c07ad', 'Tux', 'Penguin', 'TJK', 0, 'user'),
(100, 'Wei.Yan@qq.com', '25d55ad283aa400af464c76d713c07ad', 'Wei', 'Yan', 'TJU', 0, 'user'),
(101, 'Hacker.McHackerson@msn.com', '25d55ad283aa400af464c76d713c07ad', 'Hacker', 'McHackerson', 'TJU', 0, 'user'),
(102, 'MOA.mao@qq.com', '25d55ad283aa400af464c76d713c07ad', 'MOa', 'Mao', 'TJU', 0, 'user'),
(103, 'lixwbupt@gmail.com', '25d55ad283aa400af464c76d713c07ad', 'villa', 'Vanderbilt', 'TJU', 0, 'user'),
(213, '', 'fe506877a3b107f7215506b77a217fc3', '', '', '', 0, 'user'),
(212, '', '318654ecbbf3e1fde7c5e4a4c2c2cae8', '', '', '', 0, 'user'),
(211, '', 'd4e1e028ba20855761e22ce38c990b8c', '', '', '', 0, 'user'),
(210, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(209, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(208, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(207, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(206, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(205, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(204, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(203, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(202, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(201, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(200, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(199, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(198, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user'),
(197, '', '3f8de6d1d403ddf843b255ec8abe2772', '', '', '', 0, 'user');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
