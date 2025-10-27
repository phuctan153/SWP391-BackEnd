-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: ev_rental_official
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `global_admin_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  PRIMARY KEY (`global_admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'vovumaigiang123@gmail.com','Võ Vũ Mai Giang','Vocumaigiang123@','0385283598','ACTIVE');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `booking_id` bigint NOT NULL AUTO_INCREMENT,
  `actual_return_time` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deposit_status` enum('PAID','PENDING','REFUNDED') DEFAULT NULL,
  `end_date_time` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `price_snapshot_per_day` double NOT NULL,
  `price_snapshot_per_hour` double NOT NULL,
  `start_date_time` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','EXPIRED','IN_USE','PENDING','RESERVED') NOT NULL,
  `total_amount` double DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `renter_id` bigint NOT NULL,
  `staff_id` bigint DEFAULT NULL,
  `vehicle_id` bigint NOT NULL,
  PRIMARY KEY (`booking_id`),
  KEY `FKa3vsvmgga5x01a5tid4781qi5` (`renter_id`),
  KEY `FK357w452pgne0tsl2dra6ompmx` (`staff_id`),
  KEY `FKejehywt60rdh29uvn8ejths82` (`vehicle_id`),
  CONSTRAINT `FK357w452pgne0tsl2dra6ompmx` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`),
  CONSTRAINT `FKa3vsvmgga5x01a5tid4781qi5` FOREIGN KEY (`renter_id`) REFERENCES `renter` (`renter_id`),
  CONSTRAINT `FKejehywt60rdh29uvn8ejths82` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
INSERT INTO `booking` VALUES (1,'2025-10-20 09:00:00.000000','2025-10-19 09:49:53.000000','REFUNDED','2025-10-20 08:00:00.000000','2025-10-18 09:00:00.000000',1000000,200000,'2025-10-18 08:00:00.000000','EXPIRED',1000000,'2025-10-26 17:14:34.312400',6,1,63);
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_image`
--

DROP TABLE IF EXISTS `booking_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_image` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image_type` enum('AFTER_RENTAL','BEFORE_RENTAL','DAMAGE','OTHER') NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `booking_id` bigint NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `FKmtbid4a144qi0emovxgaqojmc` (`booking_id`),
  CONSTRAINT `FKmtbid4a144qi0emovxgaqojmc` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_image`
--

LOCK TABLES `booking_image` WRITE;
/*!40000 ALTER TABLE `booking_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_rating`
--

DROP TABLE IF EXISTS `booking_rating`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_rating` (
  `rating_id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `staff_rating` int NOT NULL,
  `vehicle_rating` int NOT NULL,
  `booking_id` bigint DEFAULT NULL,
  PRIMARY KEY (`rating_id`),
  UNIQUE KEY `UK6e8qa5siy2cjijq63us37i92` (`booking_id`),
  CONSTRAINT `FKk5hsvwhqcutd5t911bf4pyfeq` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_rating`
--

LOCK TABLES `booking_rating` WRITE;
/*!40000 ALTER TABLE `booking_rating` DISABLE KEYS */;
/*!40000 ALTER TABLE `booking_rating` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contract`
--

DROP TABLE IF EXISTS `contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contract` (
  `contract_id` bigint NOT NULL AUTO_INCREMENT,
  `admin_signature` varchar(512) DEFAULT NULL,
  `admin_signed_at` datetime(6) DEFAULT NULL,
  `contract_date` datetime(6) DEFAULT NULL,
  `contract_file_url` varchar(255) DEFAULT NULL,
  `contract_type` enum('ELECTRONIC','PAPER') NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `renter_signature` varchar(512) DEFAULT NULL,
  `renter_signed_at` datetime(6) DEFAULT NULL,
  `status` enum('ADMIN_SIGNED','CANCELLED','FULLY_SIGNED','PENDING_ADMIN_SIGNATURE') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `admin_id` bigint DEFAULT NULL,
  `booking_id` bigint NOT NULL,
  PRIMARY KEY (`contract_id`),
  UNIQUE KEY `UKpnx0w0h7dj91rugrcj7rjcwit` (`booking_id`),
  KEY `FKdqdoo7ctedlbd6b6rlt1gcwi9` (`admin_id`),
  CONSTRAINT `FKaki0oanaoiufio9w5y7pt8vh5` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`),
  CONSTRAINT `FKdqdoo7ctedlbd6b6rlt1gcwi9` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`global_admin_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contract`
--

LOCK TABLES `contract` WRITE;
/*!40000 ALTER TABLE `contract` DISABLE KEYS */;
INSERT INTO `contract` VALUES (33,NULL,'2025-10-24 12:06:03.031168','2025-10-24 12:01:31.280162','http://localhost:8080/files/contracts/contract_33.pdf','ELECTRONIC','2025-10-24 12:01:31.283333',NULL,'2025-10-24 12:08:24.273568','FULLY_SIGNED','2025-10-24 12:08:27.628684',1,1);
/*!40000 ALTER TABLE `contract` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `identity_document`
--

DROP TABLE IF EXISTS `identity_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `identity_document` (
  `document_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `document_number` varchar(30) NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `issue_date` date DEFAULT NULL,
  `status` enum('PENDING','REJECTED','VERIFIED') NOT NULL,
  `type` enum('DRIVER_LICENSE','NATIONAL_ID') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `renter_id` bigint NOT NULL,
  PRIMARY KEY (`document_id`),
  KEY `FKa6f4c2mfeyuyr1htu8oq97nva` (`renter_id`),
  CONSTRAINT `FKa6f4c2mfeyuyr1htu8oq97nva` FOREIGN KEY (`renter_id`) REFERENCES `renter` (`renter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `identity_document`
--

LOCK TABLES `identity_document` WRITE;
/*!40000 ALTER TABLE `identity_document` DISABLE KEYS */;
INSERT INTO `identity_document` VALUES (5,'2025-10-18 08:34:24.314433','075205013584','2030-10-18','LÊ ANH KHOA','2022-10-25','VERIFIED','NATIONAL_ID','2025-10-18 08:34:24.314433','2025-10-19 08:34:24.314433',6),(6,'2025-10-18 08:34:24.314433','075205032564','2030-10-18','LÊ ANH KHOA','2022-10-25','VERIFIED','DRIVER_LICENSE','2025-10-18 08:34:24.314433','2025-10-19 08:34:24.314433',6),(7,'2025-10-25 09:13:42.463236','079123456789','2040-03-12','NGUYỄN TẤN TRUNG','2020-03-12','VERIFIED','NATIONAL_ID','2025-10-25 09:15:31.600413','2025-10-25 09:15:31.598416',14),(8,'2025-10-25 09:13:42.472495','079987654321','2031-09-05','NGUYỄN TẤN TRUNG','2021-09-05','VERIFIED','DRIVER_LICENSE','2025-10-25 09:15:31.616176','2025-10-25 09:15:31.615173',14);
/*!40000 ALTER TABLE `identity_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice`
--

DROP TABLE IF EXISTS `invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT,
  `completed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deposit_amount` double NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `payment_method` enum('CASH','MOMO','WALLET') NOT NULL,
  `status` enum('CANCELLED','PAID','PARTIALLY_PAID','UNPAID') NOT NULL,
  `total_amount` double NOT NULL,
  `type` enum('DEPOSIT','FINAL') NOT NULL,
  `booking_id` bigint NOT NULL,
  PRIMARY KEY (`invoice_id`),
  KEY `FK4jd6uuk7w0d72riyre2w14fl7` (`booking_id`),
  CONSTRAINT `FK4jd6uuk7w0d72riyre2w14fl7` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice`
--

LOCK TABLES `invoice` WRITE;
/*!40000 ALTER TABLE `invoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invoice_detail`
--

DROP TABLE IF EXISTS `invoice_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoice_detail` (
  `invoice_detail_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `line_total` double DEFAULT NULL,
  `quantity` int NOT NULL,
  `type` enum('PENALTY','SERVICE','SPAREPART') NOT NULL,
  `unit_price` double DEFAULT NULL,
  `invoice_id` bigint NOT NULL,
  `sparepart_id` bigint DEFAULT NULL,
  `price_list_id` bigint DEFAULT NULL,
  PRIMARY KEY (`invoice_detail_id`),
  UNIQUE KEY `UKn6vp4ig0uj1jb3x2o13f438r4` (`sparepart_id`),
  UNIQUE KEY `UKbnsmnfdbked50y1fjgpajd4dr` (`price_list_id`),
  KEY `FKit1rbx4thcr6gx6bm3gxub3y4` (`invoice_id`),
  CONSTRAINT `FK9hwpni30i09mhr8bgi1b2mcr4` FOREIGN KEY (`price_list_id`) REFERENCES `price_list` (`price_id`),
  CONSTRAINT `FKit1rbx4thcr6gx6bm3gxub3y4` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`),
  CONSTRAINT `FKjgaimxp8uq368jqjlu0dgyw4n` FOREIGN KEY (`sparepart_id`) REFERENCES `spare_part` (`sparepart_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invoice_detail`
--

LOCK TABLES `invoice_detail` WRITE;
/*!40000 ALTER TABLE `invoice_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `invoice_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `is_read` bit(1) NOT NULL,
  `message` text,
  `recipient_id` bigint NOT NULL,
  `recipient_type` enum('ADMIN','RENTER','STAFF') NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`notification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,_binary '','HIHI',1,'STAFF','CON CÁC'),(2,_binary '\0','Staff đã gửi hợp đồng #3 lên để ký duyệt. Vui lòng kiểm tra.',1,'ADMIN','? Hợp đồng mới cần ký duyệt'),(4,_binary '\0','Staff đã gửi hợp đồng #19 lên để ký duyệt.\nXem tại: http://localhost:8080/files/contracts/contract_19.pdf',1,'ADMIN','? Hợp đồng mới cần ký duyệt'),(5,_binary '\0','Staff đã gửi hợp đồng #32 lên để ký duyệt.\nXem tại: http://localhost:8080/files/contracts/contract_32.pdf',1,'ADMIN','? Hợp đồng mới cần ký duyệt'),(6,_binary '\0','Staff đã gửi hợp đồng #33 lên để ký duyệt.\nXem tại: http://localhost:8080/files/contracts/contract_33.pdf',1,'ADMIN','? Hợp đồng mới cần ký duyệt'),(7,_binary '\0','Đặt xe của bạn đã bị hủy vì không hoàn tất thủ tục nhận xe trong vòng 1 giờ kể từ thời điểm bắt đầu thuê.',6,'RENTER','⚠️ Đặt xe đã hết hạn'),(8,_binary '\0','Đặt xe của bạn đã bị hủy vì không hoàn tất thủ tục nhận xe trong vòng 1 giờ kể từ thời điểm bắt đầu thuê.',6,'RENTER','⚠️ Đặt xe đã hết hạn');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_verification`
--

DROP TABLE IF EXISTS `otp_verification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_verification` (
  `otp_id` bigint NOT NULL AUTO_INCREMENT,
  `attempt_count` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `expired_at` datetime(6) DEFAULT NULL,
  `otp_code` varchar(6) NOT NULL,
  `status` enum('FAILED','PENDING','VERIFIED') DEFAULT NULL,
  `verified_at` datetime(6) DEFAULT NULL,
  `contract_id` bigint NOT NULL,
  PRIMARY KEY (`otp_id`),
  KEY `FKomybj9dv8vohsjnpxuxdv0y6f` (`contract_id`),
  CONSTRAINT `FKomybj9dv8vohsjnpxuxdv0y6f` FOREIGN KEY (`contract_id`) REFERENCES `contract` (`contract_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_verification`
--

LOCK TABLES `otp_verification` WRITE;
/*!40000 ALTER TABLE `otp_verification` DISABLE KEYS */;
INSERT INTO `otp_verification` VALUES (7,0,'2025-10-24 12:04:53.819853','2025-10-24 12:09:53.819853','510059','VERIFIED','2025-10-24 12:06:03.024644',33),(8,0,'2025-10-24 12:07:39.950158','2025-10-24 12:12:39.950158','622545','VERIFIED','2025-10-24 12:08:24.273568',33);
/*!40000 ALTER TABLE `otp_verification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `otp_verification_email`
--

DROP TABLE IF EXISTS `otp_verification_email`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_verification_email` (
  `otp_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `otp_code` varchar(6) NOT NULL,
  `status` enum('EXPIRED','UNVERIFIED','VERIFIED') DEFAULT NULL,
  `renter_id` bigint NOT NULL,
  PRIMARY KEY (`otp_id`),
  KEY `FK7pymihmbytui6nsdl1em0yloc` (`renter_id`),
  CONSTRAINT `FK7pymihmbytui6nsdl1em0yloc` FOREIGN KEY (`renter_id`) REFERENCES `renter` (`renter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `otp_verification_email`
--

LOCK TABLES `otp_verification_email` WRITE;
/*!40000 ALTER TABLE `otp_verification_email` DISABLE KEYS */;
INSERT INTO `otp_verification_email` VALUES (2,'2025-10-18 08:34:58.828778','2025-10-18 08:39:58.828778','417934','VERIFIED',6),(3,'2025-10-25 09:09:18.638094','2025-10-25 09:14:18.638094','369584','VERIFIED',14),(4,'2025-10-25 10:09:32.054726','2025-10-25 10:14:32.054726','694079','VERIFIED',14);
/*!40000 ALTER TABLE `otp_verification_email` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_transaction`
--

DROP TABLE IF EXISTS `payment_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_transaction` (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(38,2) NOT NULL,
  `status` enum('FAILED','PENDING','SUCCESS') NOT NULL,
  `transaction_time` datetime(6) DEFAULT NULL,
  `transaction_type` enum('INVOICE_CASH','INVOICE_MOMO','INVOICE_WALLET','WALLET_TOPUP','WALLET_WITHDRAW','WALLET_REFUND_DEPOSIT') NOT NULL,
  `invoice_id` bigint DEFAULT NULL,
  `wallet_id` bigint DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  KEY `FKfdqxbup9flmh4er9jpx2pvdl` (`invoice_id`),
  KEY `FK8s21d37470nrn9fr4jua85mqt` (`wallet_id`),
  CONSTRAINT `FK8s21d37470nrn9fr4jua85mqt` FOREIGN KEY (`wallet_id`) REFERENCES `wallet` (`wallet_id`),
  CONSTRAINT `FKfdqxbup9flmh4er9jpx2pvdl` FOREIGN KEY (`invoice_id`) REFERENCES `invoice` (`invoice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_transaction`
--

LOCK TABLES `payment_transaction` WRITE;
/*!40000 ALTER TABLE `payment_transaction` DISABLE KEYS */;
INSERT INTO `payment_transaction` VALUES (1,5000000.00,'SUCCESS','2025-10-26 17:09:10.490054','WALLET_REFUND_DEPOSIT',NULL,1),(2,5000000.00,'SUCCESS','2025-10-26 17:14:34.289332','WALLET_REFUND_DEPOSIT',NULL,1);
/*!40000 ALTER TABLE `payment_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `price_list`
--

DROP TABLE IF EXISTS `price_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_list` (
  `price_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `item_name` varchar(100) DEFAULT NULL,
  `price_type` enum('DEPOSIT','OTHER','SPARE_PART') NOT NULL,
  `stock_quantity` int DEFAULT NULL,
  `unit_price` double NOT NULL,
  PRIMARY KEY (`price_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `price_list`
--

LOCK TABLES `price_list` WRITE;
/*!40000 ALTER TABLE `price_list` DISABLE KEYS */;
INSERT INTO `price_list` VALUES (1,'Tiền cọc thuê xe mặc định cho mọi booking','Deposit Fee','DEPOSIT',NULL,5000000);
/*!40000 ALTER TABLE `price_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `renter`
--

DROP TABLE IF EXISTS `renter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `renter` (
  `renter_id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `auth_provider` enum('GOOGLE','LOCAL') DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `is_blacklisted` bit(1) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `status` enum('DELETED','PENDING_VERIFICATION','VERIFIED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`renter_id`),
  UNIQUE KEY `UK8lx5melb9aiqsx6uaw8ssbb5r` (`email`),
  UNIQUE KEY `UKa8u2p7klysjk5vwgggv9jdb4c` (`google_id`),
  UNIQUE KEY `UKqj2v8aodemlgihm20v8vftj71` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `renter`
--

LOCK TABLES `renter` WRITE;
/*!40000 ALTER TABLE `renter` DISABLE KEYS */;
INSERT INTO `renter` VALUES (6,'TÂN VẠN, TP BIÊN HÒA, ĐỒNG NAI','LOCAL','2025-10-18 08:34:24.314433','2002-12-28','concatt1123456@gmail.com','Le Anh Khoa',NULL,_binary '\0','Leanhkhoa123@@','0972714956','VERIFIED','2025-10-22 07:42:13.719796'),(13,NULL,'GOOGLE','2025-10-18 14:03:59.452763',NULL,'thanhhaivu0501@gmail.com','Hải Thanh','114207206028010445277',_binary '\0',NULL,NULL,'PENDING_VERIFICATION','2025-10-18 14:03:59.452763'),(14,'123 Đường Trần Hưng Đạo, Quận 5, TP. Hồ Chí Minh','LOCAL','2025-10-25 09:08:27.752570','1998-07-15','hgum1189@gmail.com','Nguyễn Tấn Trung',NULL,_binary '\0','Nguyentantrung123@','0385284512','VERIFIED','2025-10-25 10:10:40.149543');
/*!40000 ALTER TABLE `renter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `risk_profile`
--

DROP TABLE IF EXISTS `risk_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `risk_profile` (
  `risk_id` bigint NOT NULL AUTO_INCREMENT,
  `last_violation_at` datetime(6) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `risk_level` enum('HIGH','LOW','MEDIUM') DEFAULT NULL,
  `violation_count` int NOT NULL,
  `renter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`risk_id`),
  UNIQUE KEY `UKai42pol01frhc1jnfhwn9gyot` (`renter_id`),
  CONSTRAINT `FK483hewwl31vp62wnpmqnbcbe9` FOREIGN KEY (`renter_id`) REFERENCES `renter` (`renter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `risk_profile`
--

LOCK TABLES `risk_profile` WRITE;
/*!40000 ALTER TABLE `risk_profile` DISABLE KEYS */;
/*!40000 ALTER TABLE `risk_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `spare_part`
--

DROP TABLE IF EXISTS `spare_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spare_part` (
  `sparepart_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `part_name` varchar(255) DEFAULT NULL,
  `stock_quantity` int NOT NULL,
  `unit_price` double DEFAULT NULL,
  PRIMARY KEY (`sparepart_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spare_part`
--

LOCK TABLES `spare_part` WRITE;
/*!40000 ALTER TABLE `spare_part` DISABLE KEYS */;
/*!40000 ALTER TABLE `spare_part` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(15) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `UKpvctx4dbua9qh4p4s3gm3scrh` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'2025-10-18 08:40:17.000000','duongminhchau@evrental.com','Dương Minh Châu','Chau@2025','0901234567','ACTIVE');
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff_station`
--

DROP TABLE IF EXISTS `staff_station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff_station` (
  `staff_station_id` bigint NOT NULL AUTO_INCREMENT,
  `assigned_at` datetime(6) DEFAULT NULL,
  `role_at_station` enum('STATION_ADMIN','STATION_STAFF') DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  `unassigned_at` datetime(6) DEFAULT NULL,
  `staff_id` bigint DEFAULT NULL,
  `station_id` bigint DEFAULT NULL,
  PRIMARY KEY (`staff_station_id`),
  KEY `FKc1ome6fc32kevcw3e43whdvp` (`staff_id`),
  KEY `FK9ga7b5sskkvqc9x9fxdx6m3f1` (`station_id`),
  CONSTRAINT `FK9ga7b5sskkvqc9x9fxdx6m3f1` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`),
  CONSTRAINT `FKc1ome6fc32kevcw3e43whdvp` FOREIGN KEY (`staff_id`) REFERENCES `staff` (`staff_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff_station`
--

LOCK TABLES `staff_station` WRITE;
/*!40000 ALTER TABLE `staff_station` DISABLE KEYS */;
INSERT INTO `staff_station` VALUES (1,'2025-10-24 12:06:03.031168','STATION_STAFF','ACTIVE',NULL,1,9);
/*!40000 ALTER TABLE `staff_station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `station`
--

DROP TABLE IF EXISTS `station`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `station` (
  `station_id` bigint NOT NULL AUTO_INCREMENT,
  `car_number` int NOT NULL,
  `latitude` double DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  PRIMARY KEY (`station_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station`
--

LOCK TABLES `station` WRITE;
/*!40000 ALTER TABLE `station` DISABLE KEYS */;
INSERT INTO `station` VALUES (9,6,10.7769,'12 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP.HCM',106.7009,'VinFast Station - Nguyễn Huệ','ACTIVE'),(10,7,10.7845,'50 Lý Chính Thắng, Phường 8, Quận 3, TP.HCM',106.6823,'VinFast Station - Lý Chính Thắng','ACTIVE'),(11,5,10.7575,'324 Trần Hưng Đạo, Phường 11, Quận 5, TP.HCM',106.6671,'VinFast Station - Trần Hưng Đạo','ACTIVE'),(12,7,10.7299,'801 Nguyễn Văn Linh, Phường Tân Phong, Quận 7, TP.HCM',106.7219,'VinFast Station - Phú Mỹ Hưng','ACTIVE'),(13,6,10.8525,'500 Kha Vạn Cân, Phường Linh Đông, TP. Thủ Đức, TP.HCM',106.7387,'VinFast Station - Kha Vạn Cân','ACTIVE');
/*!40000 ALTER TABLE `station` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `term_condition`
--

DROP TABLE IF EXISTS `term_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `term_condition` (
  `term_condition_id` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `term_content` text,
  `term_number` int NOT NULL,
  `term_title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contract_id` bigint NOT NULL,
  PRIMARY KEY (`term_condition_id`),
  KEY `FKe5wj2sf3mjp3jqcij2l29sgbo` (`contract_id`),
  CONSTRAINT `FKe5wj2sf3mjp3jqcij2l29sgbo` FOREIGN KEY (`contract_id`) REFERENCES `contract` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `term_condition`
--

LOCK TABLES `term_condition` WRITE;
/*!40000 ALTER TABLE `term_condition` DISABLE KEYS */;
INSERT INTO `term_condition` VALUES ('051a98c6-46d1-4acc-ad32-8915d1f4fdd1','2025-10-24 12:01:31.308241','Tiền cọc được hoàn lại sau khi kiểm tra xe không hư hỏng. Nếu hư hỏng, tiền cọc được dùng để bù chi phí sửa chữa.',3,'Tiền cọc và bồi thường',NULL,33),('536f8084-1e01-4f99-988e-3553f91c217e','2025-10-24 12:01:31.308241','Xe phải được trả đúng hạn. Mọi trường hợp trễ giờ sẽ tính thêm phí.',2,'Thời gian thuê và trả xe',NULL,33),('8c774535-7f3c-4f85-a75e-3da02a33ed68','2025-10-24 12:01:31.308241','Người thuê có thể hủy hợp đồng trước 12 giờ để được hoàn cọc toàn phần.',4,'Chính sách hủy hợp đồng',NULL,33),('ca8d0e6f-9bb9-40c6-96ed-0930b2227b06','2025-10-24 12:01:31.306225','Người thuê chịu trách nhiệm bảo quản xe, tuân thủ quy định giao thông và không cho người khác mượn xe.',1,'Trách nhiệm người thuê',NULL,33);
/*!40000 ALTER TABLE `term_condition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle`
--

DROP TABLE IF EXISTS `vehicle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT,
  `battery_level` double DEFAULT NULL,
  `description` text,
  `mileage` double DEFAULT NULL,
  `plate_number` varchar(20) NOT NULL,
  `price_per_day` double DEFAULT NULL,
  `price_per_hour` double DEFAULT NULL,
  `status` enum('AVAILABLE','CANCELLED','IN_REPAIR','IN_USE','MAINTENANCE','REPAIRED') NOT NULL,
  `vehicle_name` varchar(100) NOT NULL,
  `model_id` bigint NOT NULL,
  `station_id` bigint NOT NULL,
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `UKavfc6x9pcl38sop7lqocxppbb` (`plate_number`),
  KEY `FK7v9f0lgrlk23e5xekv7bb3f9v` (`model_id`),
  KEY `FKeixoqde7yk5c3oacpn6gotokn` (`station_id`),
  CONSTRAINT `FK7v9f0lgrlk23e5xekv7bb3f9v` FOREIGN KEY (`model_id`) REFERENCES `vehicle_model` (`model_id`),
  CONSTRAINT `FKeixoqde7yk5c3oacpn6gotokn` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle`
--

LOCK TABLES `vehicle` WRITE;
/*!40000 ALTER TABLE `vehicle` DISABLE KEYS */;
INSERT INTO `vehicle` VALUES (63,92,'VF8 màu trắng, nội thất cao cấp',12000,'51H-10001',1000000,200000,'AVAILABLE','VinFast VF8 - Trắng Pearl',1,9),(64,88,'VF8 bản Eco, pin 90kWh',15500,'51H-10002',1000000,200000,'AVAILABLE','VinFast VF8 - Đen Midnight',1,9),(65,94,'SUV 7 chỗ, phù hợp gia đình',8500,'51H-10003',1500000,300000,'IN_USE','VinFast VF9 - Bạc Luxury',2,9),(66,89,'VF9 bản Plus, nội thất da nâu',13500,'51H-10004',1500000,300000,'AVAILABLE','VinFast VF9 - Đỏ Ruby',2,9),(67,91,'Xe nhỏ gọn chạy nội thành',9500,'51H-10005',800000,150000,'AVAILABLE','VinFast VF e34 - Xanh Sky',3,9),(68,85,'Xe tiết kiệm điện năng',11000,'51H-10006',800000,150000,'MAINTENANCE','VinFast VF e34 - Trắng Ice',3,9),(69,90,'VF8 mạnh mẽ, pin 90kWh',9000,'51H-20001',1000000,200000,'AVAILABLE','VinFast VF8 - Đỏ Flame',1,10),(70,87,'VF8 màu xanh đậm sang trọng',16500,'51H-20002',1000000,200000,'AVAILABLE','VinFast VF8 - Xanh Deep Ocean',1,10),(71,86,'VF8 phiên bản Eco',18000,'51H-20003',1000000,200000,'IN_USE','VinFast VF8 - Bạc Metal',1,10),(72,95,'VF9 bản cao cấp, SUV 7 chỗ',8000,'51H-20004',1500000,300000,'AVAILABLE','VinFast VF9 - Trắng Snow',2,10),(73,93,'VF9 Plus, nội thất da nâu',9100,'51H-20005',1500000,300000,'AVAILABLE','VinFast VF9 - Đen Premium',2,10),(74,90,'Xe điện nhỏ gọn, phù hợp nội thành',9800,'51H-20006',800000,150000,'AVAILABLE','VinFast VF e34 - Trắng',3,10),(75,85,'VF e34 tiết kiệm pin',12000,'51H-20007',800000,150000,'MAINTENANCE','VinFast VF e34 - Xanh Lá',3,10),(76,91,'VF8 bản Eco',10500,'51H-30001',1000000,200000,'AVAILABLE','VinFast VF8 - Trắng',1,11),(77,88,'VF8 bản cao cấp',12000,'51H-30002',1000000,200000,'AVAILABLE','VinFast VF8 - Đen',1,11),(78,92,'VF9 7 chỗ, pin lớn',9500,'51H-30003',1500000,300000,'AVAILABLE','VinFast VF9 - Đỏ',2,11),(79,86,'VF e34 bản Eco',11000,'51H-30004',800000,150000,'IN_USE','VinFast VF e34 - Xanh',3,11),(80,90,'Xe điện nhỏ gọn',9000,'51H-30005',800000,150000,'AVAILABLE','VinFast VF e34 - Trắng',3,11),(81,93,'VF8 mới, pin tốt',9500,'51H-40001',1000000,200000,'AVAILABLE','VinFast VF8 - Đỏ',1,12),(82,89,'VF8 Plus, sang trọng',11000,'51H-40002',1000000,200000,'AVAILABLE','VinFast VF8 - Bạc',1,12),(83,96,'VF9 Premium',7000,'51H-40003',1500000,300000,'AVAILABLE','VinFast VF9 - Đen',2,12),(84,90,'SUV 7 chỗ nội thất da',14000,'51H-40004',1500000,300000,'IN_USE','VinFast VF9 - Xanh',2,12),(86,87,'VF e34 Eco',9500,'51H-40006',800000,150000,'AVAILABLE','VinFast VF e34 - Đen',3,12),(87,89,'Xe điện nhỏ gọn',9900,'51H-40007',800000,150000,'MAINTENANCE','VinFast VF e34 - Xanh',3,12),(88,94,'VF8 Eco, pin đầy đủ',8200,'51H-50001',1000000,200000,'AVAILABLE','VinFast VF8 - Trắng',1,13),(89,92,'VF8 bản cao cấp',8700,'51H-50002',1000000,200000,'AVAILABLE','VinFast VF8 - Xanh',1,13),(90,97,'VF9 sang trọng, nội thất da',6000,'51H-50003',1500000,300000,'AVAILABLE','VinFast VF9 - Đỏ',2,13),(91,89,'VF9 bản Premium',11200,'51H-50004',1500000,300000,'IN_USE','VinFast VF9 - Đen',2,13),(92,91,'Xe điện nhỏ gọn',9700,'51H-50005',800000,150000,'AVAILABLE','VinFast VF e34 - Trắng',3,13),(93,87,'VF e34 bản Eco',9900,'51H-50006',800000,150000,'AVAILABLE','VinFast VF e34 - Xanh',3,13),(94,90,'Bản cao cấp có camera 360',NULL,'51F-99999',NULL,160000,'AVAILABLE','VinFast VF e34 - Premium',2,10);
/*!40000 ALTER TABLE `vehicle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle_image`
--

DROP TABLE IF EXISTS `vehicle_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_image` (
  `image_id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `public_id` varchar(255) DEFAULT NULL,
  `vehicle_id` bigint NOT NULL,
  PRIMARY KEY (`image_id`),
  KEY `FKc87e8nqciduip706iimbc9f8b` (`vehicle_id`),
  CONSTRAINT `FKc87e8nqciduip706iimbc9f8b` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle_image`
--

LOCK TABLES `vehicle_image` WRITE;
/*!40000 ALTER TABLE `vehicle_image` DISABLE KEYS */;
INSERT INTO `vehicle_image` VALUES (1,'Ảnh xe tải lên Cloudinary','https://res.cloudinary.com/df6hi63cx/image/upload/v1761139044/ev_rental/vehicles/epcqyulvbofj8wf98dlq.webp','ev_rental/vehicles/epcqyulvbofj8wf98dlq',94);
/*!40000 ALTER TABLE `vehicle_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehicle_model`
--

DROP TABLE IF EXISTS `vehicle_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehicle_model` (
  `model_id` bigint NOT NULL AUTO_INCREMENT,
  `battery_capacity` double DEFAULT NULL,
  `manufacturer` varchar(255) DEFAULT NULL,
  `model_name` varchar(255) DEFAULT NULL,
  `seating_capacity` int NOT NULL,
  PRIMARY KEY (`model_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehicle_model`
--

LOCK TABLES `vehicle_model` WRITE;
/*!40000 ALTER TABLE `vehicle_model` DISABLE KEYS */;
INSERT INTO `vehicle_model` VALUES (1,90,'VinFast','VinFast VF8',5),(2,123,'VinFast','VinFast VF9',7),(3,42,'VinFast','VinFast VF e34',5);
/*!40000 ALTER TABLE `vehicle_model` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet`
--

DROP TABLE IF EXISTS `wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet` (
  `wallet_id` bigint NOT NULL AUTO_INCREMENT,
  `balance` decimal(38,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `renter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`wallet_id`),
  UNIQUE KEY `UK13mwraetr568dve629vyew7v2` (`renter_id`),
  CONSTRAINT `FKc225vfvcyd03emccxts1ynnen` FOREIGN KEY (`renter_id`) REFERENCES `renter` (`renter_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet`
--

LOCK TABLES `wallet` WRITE;
/*!40000 ALTER TABLE `wallet` DISABLE KEYS */;
INSERT INTO `wallet` VALUES (1,5000000.00,'2025-10-18 08:34:24.355935','ACTIVE','2025-10-26 17:14:34.310881',6),(6,0.00,'2025-10-18 14:03:59.441765','ACTIVE','2025-10-18 14:03:59.483759',13),(7,0.00,'2025-10-25 09:08:27.798381','ACTIVE','2025-10-25 09:08:27.799887',14);
/*!40000 ALTER TABLE `wallet` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-27 15:18:45
