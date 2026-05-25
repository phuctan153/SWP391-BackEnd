# EV Rental System (Backend)

EV Rental System is an enterprise-grade backend infrastructure built with **Java 21** and **Spring Boot 3.3.4** designed to automate and manage electric vehicle (EV) rentals. The platform offers a complete solution for vehicle management, station allocation, hourly/daily booking workflows, secure dual-gateway financial transactions, automated digital contract generation, and intelligent renter risk profiling.

---

## 🚀 Key Features

### 1. User Authentication & Security
* **Multi-Role RBAC:** Secure access control tailored for Renters, Staff, and Administrators using `@PreAuthorize` method security.
* **Hybrid Authentication:** Combines custom **JWT Token** authentication with **OAuth2 Client** integration for seamless Google Login.
* **Identity Verification:** Features secure email OTP verification flows powered by **Spring Mail** for password resets and critical profile updates.
* **Risk Management:** Includes a dynamic Renter Risk Profile system and Admin Blacklist controls to flag or restrict high-risk users.

### 2. Vehicle & Station Management
* **Inventory Control:** Comprehensive CRUD management for electric vehicles, models (e.g., VinFast VF e34, Feliz S), and battery capacities.
* **Station Mapping:** Assigns and tracks vehicles at physical rental stations for precise dispatching and real-time availability.
* **Media Cloud Storage:** Integrates **Cloudinary API** for secure, high-performance image uploads and hosting for vehicle logs.

### 3. Booking & Scheduled Workflows
* **Flexible Pricing:** Supports accurate snapshot pricing logic at the time of reservation for hourly or daily rental options.
* **Automated Task Tracking:** Utilizes **Spring Scheduler** (`@EnableScheduling`) for background processing to monitor vehicle battery levels, ride completions, and deadline status.

### 4. Integrated Payment Gateways
* **Dual-Gateway Support:** Fully integrates **MoMo Payment API** (Mobile Wallet) and **PayOS Gateway** (Online Banking) using **Apache HttpClient 5** for secure checkout URLs and asynchronous IPN/Webhook transaction processing.
* **In-App Wallet:** Implements a localized digital wallet architecture for users to deposit funds, maintain balances, and execute point-of-sale rentals.

### 5. Automated PDF Document Generation
* **Digital Contracts & Invoices:** Dynamically renders tailored digital rental agreements and financial invoices by transforming HTML templates into official PDF documents via **Flying Saucer** and **iText**.

---

## 🛠️ Technology Stack

* **Core Framework:** Java 21, Spring Boot 3.3.4, Spring Security, Spring Web, Spring Validation
* **Data Layer:** PostgreSQL (Primary Production DB), MySQL (Support Mode), Hibernate, Spring Data JPA
* **Object Mapping:** MapStruct 1.5.5 (Type-safe Entity-to-DTO conversion), Lombok
* **Integrations:** Apache HttpClient 5, OpenFeign (Microservices ready), Gson, FreeMarker Template Engine
* **API Documentation:** Springdoc OpenAPI 2.5.0 (Swagger UI)
* **Containerization:** Docker, Docker Compose

---

## 💾 Database Schema Overview
The system architecture operates on a relational 26-entity layout. Core database tables include:
* `renter` & `identity_document` — Profiles, identity storage, and risk metrics.
* `vehicle` & `vehicle_model` — EV status tracking, battery levels, and baseline specs.
* `station` — Branch locations and vehicle distributions.
* `booking`, `contract`, & `invoice` — Reservation logs, PDF legal contracts, and financial records.
* `payment_transaction` & `wallet` — Secure balance ledger and gateway payment details.
* `staff` & `admin` — Station personnel task assignments and global control metrics.

---

## 📦 Local Installation & Setup

### Prerequisites
* Java 21 SDK
* Maven 3.x
* PostgreSQL Database Server
* Active API credentials for external integrations (MoMo, PayOS, Google, Cloudinary, Gmail SMTP)

### Step-by-Step Execution

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/phuctan153/SWP391-BackEnd.git](https://github.com/phuctan153/SWP391-BackEnd.git)
   cd SWP391-BackEnd
   ```

2. **Configure Environment Variables:**
   Create a file named `.env` in the root directory of the project and define your configuration keys as shown below (replace the placeholders with your actual values):
   ```properties
   # Server Port Configuration
   PORT=8080

   # Database Settings (PostgreSQL)
   DB_URL=jdbc:postgresql://localhost:5432/ev_rental_db
   DB_USERNAME=your_database_username
   DB_PASSWORD=your_database_password

   # OAuth2 Google Credentials
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret

   # SMTP Mail Settings (Gmail)
   MAIL_USERNAME=your_email@gmail.com
   MAIL_PASSWORD=your_app_specific_password

   # Cloudinary Media Storage
   CLOUDINARY_NAME=your_cloudinary_cloud_name
   CLOUDINARY_API_KEY=your_cloudinary_api_key
   CLOUDINARY_API_SECRET=your_cloudinary_api_secret

   # MoMo Payment API Gateway
   MOMO_PARTNER_CODE=your_momo_partner_code
   MOMO_ACCESS_KEY=your_momo_access_key
   MOMO_SECRET_KEY=your_momo_secret_key
   MOMO_NGROK_DOMAIN=your_ngrok_tunnel_subdomain

   # PayOS Payment Gateway
   PAYOS_CLIENT_ID=your_payos_client_id
   PAYOS_API_KEY=your_payos_api_key
   PAYOS_CHECKSUM_KEY=your_payos_checksum_key
   ```

3. **Build the Application Packaging:**
   ```bash
   mvn clean package -DskipTests
   ```

4. **Run the Application locally:**
   ```bash
   mvn spring-boot:run
   ```
   The backend service will boot up at `http://localhost:8080`. You can inspect and interact with the live REST endpoints through the Swagger UI dashboard at `http://localhost:8080/swagger-ui.html`.

---

## 👥 Contributors
* **Nguyễn Phúc Tấn** - Core Backend Infrastructure, Database Design & API Integrations.
