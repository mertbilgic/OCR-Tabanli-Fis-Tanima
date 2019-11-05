-- phpMyAdmin SQL Dump
-- version 4.8.5
-- https://www.phpmyadmin.net/
--
-- Anamakine: 127.0.0.1
-- Üretim Zamanı: 05 Kas 2019, 12:31:43
-- Sunucu sürümü: 10.1.38-MariaDB
-- PHP Sürümü: 7.2.17

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Veritabanı: `yazlab1.1`
--

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `company`
--

CREATE TABLE `company` (
  `ID` int(11) NOT NULL,
  `Name` text COLLATE utf8_turkish_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_turkish_ci;

--
-- Tablo döküm verisi `company`
--

INSERT INTO `company` (`ID`, `Name`) VALUES
(53, 'PEYNİRCİ BABA'),
(54, 'BİM BİRLEŞİK MAĞAZALAR A.Ş.'),
(55, 'MARKET 1'),
(56, 'SOK MARKETLER T.A.S. Ç'),
(57, 'ONUR ERİKLİ MAXIMA');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `plug`
--

CREATE TABLE `plug` (
  `ID` int(11) DEFAULT NULL,
  `date` text COLLATE utf8_turkish_ci NOT NULL,
  `plugNo` text COLLATE utf8_turkish_ci,
  `product` text COLLATE utf8_turkish_ci,
  `total` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_turkish_ci;

--
-- Tablo döküm verisi `plug`
--

INSERT INTO `plug` (`ID`, `date`, `plugNo`, `product`, `total`) VALUES
(53, '15.10.2019', '0126', '\nLOKUM KG GULLÜU PRENS %08 *5, 02\nLOKUM KG GULLU %08 *3, 61\n%08 *0, 64 X8, 63', 8.63),
(54, '26.12.2018', '0179', '\nM.NEKTARI 1LT %08 *3,50\nAÇ BİTİRTOST—ÇEÇ. %08 *2, 50\nTRİLEÇE 150 GR %08 *2, 75\nAYRAN 200ML DOST %08 *%0, 60', 9.35),
(55, '14.09.2018', '00332', '\nTAVUK BONFİLE %08 *9.98\nSUCUK %08 *10.25\nKARPUZ %08 *15.14\nÇİKOLATA %08 *7.78', 100),
(56, '037/04/2018', '25', '\nPRINGLES 165 GR+COCA %8 *7, 35\nPRINGLES 165 GR+COCA %8 *7, 95\nCHUPA CHUPS PATLAYAN %8 *1, 00', 83.35),
(56, '037/04/2018', '25', '\nPRINGLES 165 GR+COCA %8 *7, 35\nPRINGLES 165 GR+COCA %8 *7, 95\nCHUPA CHUPS PATLAYAN %8 *1, 00', 83.35),
(57, '31/0572017', '0078', '\nCLEAR SAMP 6SOML MEN %18 *13,90\nCLEAR SAMP 65OML WOM %18 *13,90\nDOVE CREAM BAR 100GR %18 *2, 95\n. DOVE CREAM BAR 100GR %18 _ *2,95', 5.72);

--
-- Dökümü yapılmış tablolar için indeksler
--

--
-- Tablo için indeksler `company`
--
ALTER TABLE `company`
  ADD PRIMARY KEY (`ID`);

--
-- Tablo için indeksler `plug`
--
ALTER TABLE `plug`
  ADD KEY `ID` (`ID`);

--
-- Dökümü yapılmış tablolar için AUTO_INCREMENT değeri
--

--
-- Tablo için AUTO_INCREMENT değeri `company`
--
ALTER TABLE `company`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- Dökümü yapılmış tablolar için kısıtlamalar
--

--
-- Tablo kısıtlamaları `plug`
--
ALTER TABLE `plug`
  ADD CONSTRAINT `plug_ibfk_1` FOREIGN KEY (`ID`) REFERENCES `company` (`ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
