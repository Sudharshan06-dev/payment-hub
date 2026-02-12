# Bill Management Platform

A comprehensive, production-grade bill management and payment processing system built with Spring Boot, designed to streamline bill tracking, payment processing, and financial transaction management. This platform provides a robust backend API with real-time bill status tracking, payment processing capabilities, and secure user authentication.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Usage](#usage)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Overview

The **Bill Management Platform** is an enterprise-grade payment processing system that enables users to:
- Track and manage multiple bills across different categories
- Monitor payment due dates with real-time status updates
- Process payments securely with audit trails
- Access comprehensive bill analytics and reporting
- Manage recurring bills and one-time payments

Built with industry-standard design patterns and security best practices, this platform is designed to handle high-volume financial transactions with reliability and scalability.

---

## ✨ Features

### Core Functionality
- **Bill Management**
  - Create, read, update, and delete bills
  - Support for monthly, quarterly, and one-time bills
  - Track bill status (PENDING, DUE, OVERDUE, PAID)
  - Multi-currency support

- **Payment Processing**
  - Secure payment processing with transaction tracking
  - Payment history and audit logs
  - Installment payment support
  - Payment failure handling and retry mechanisms

- **User Authentication & Authorization**
  - Secure user registration and login
  - JWT token-based authentication
  - Role-based access control
  - Password encryption and validation

- **Real-time Tracking**
  - Bill status updates
  - Payment transaction logging
  - Overdue bill alerts
  - Dashboard analytics

- **Search & Filtering**
  - Filter bills by status, date range, amount, and biller
  - Full-text search across bills
  - Advanced sorting capabilities

---

## 🛠 Tech Stack

### Backend
- **Framework:** Spring Boot 3.x
- **Language:** Java 17+
- **Build Tool:** Maven
- **ORM:** Spring Data JPA / Hibernate

### Database
- **Primary:** MySQL 8.0+
- **Schema Management:** Flyway/Liquibase

### Security
- **Authentication:** Spring Security with JWT
- **Encryption:** BCrypt for password hashing
- **Input Validation:** Spring Validation, custom validators

### API & Communication
- **REST API:** Spring Web (RESTful endpoints)
- **Data Transfer:** DTO Pattern for API contracts
- **JSON Processing:** Jackson

### Testing
- **Unit Testing:** JUnit 5
- **Integration Testing:** Spring Boot Test
- **Mocking:** Mockito

### Monitoring & Logging
- **Logging:** SLF4J with Logback
- **Metrics:** Spring Actuator
- **Error Tracking:** Custom exception handlers

---

## 🏗 Architecture

### Layered Architecture

```
┌─────────────────────────────────┐
│   REST Controller Layer          │
│  (HTTP Request Handling)         │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│   Service Layer                 │
│  (Business Logic)               │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│   Repository Layer              │
│  (Data Access)                  │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│   Entity Layer / Database        │
│  (Data Persistence)             │
└─────────────────────────────────┘
```

### Design Patterns Used

1. **Repository Pattern** - Data access abstraction
2. **Service Layer Pattern** - Business logic encapsulation
3. **DTO Pattern** - API contract isolation and security
4. **Singleton Pattern** - Spring Bean management
5. **Exception Translation** - Custom exception handling

### Key Components

- **Controllers** - HTTP request routing and response handling
- **Services** - Business logic and transaction management
- **Repositories** - Database query abstraction (Spring Data JPA)
- **Entities** - JPA domain models representing database tables
- **DTOs** - Data Transfer Objects for API contracts
- **Exception Handlers** - Global error handling and HTTP status mapping

---

## 📊 Database Schema

### Core Tables

#### `users`
```sql
CREATE TABLE users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### `bills`
```sql
CREATE TABLE bills (
  bill_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  biller_name VARCHAR(255) NOT NULL,
  account_number VARCHAR(50) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) DEFAULT 'USD',
  due_date TIMESTAMP NOT NULL,
  bill_status ENUM('PENDING', 'DUE', 'OVERDUE', 'PAID') DEFAULT 'PENDING',
  bill_frequency ENUM('MONTHLY', 'QUATERLY', 'ONETIME') DEFAULT 'MONTHLY',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted BOOLEAN DEFAULT false,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_status (user_id, bill_status),
  INDEX idx_due_date (due_date)
);
```

#### `payments`
```sql
CREATE TABLE payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  bill_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  payment_amount DECIMAL(10, 2) NOT NULL,
  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  payment_method VARCHAR(50),
  transaction_id VARCHAR(100) UNIQUE,
  payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  INDEX idx_user_date (user_id, payment_date)
);
```

---

## 📁 Project Structure

```
bill-management-platform/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/billservice/
│   │   │       ├── controller/          # REST Controllers
│   │   │       │   ├── BillController.java
│   │   │       │   ├── PaymentController.java
│   │   │       │   └── AuthController.java
│   │   │       ├── service/             # Service Layer (Business Logic)
│   │   │       │   ├── BillService.java
│   │   │       │   ├── PaymentService.java
│   │   │       │   └── UserService.java
│   │   │       ├── repository/          # Data Access Layer
│   │   │       │   ├── BillRepository.java
│   │   │       │   ├── PaymentRepository.java
│   │   │       │   └── UserRepository.java
│   │   │       ├── model/               # JPA Entities
│   │   │       │   ├── Bills.java
│   │   │       │   ├── Payments.java
│   │   │       │   └── Users.java
│   │   │       ├── dto/                 # Data Transfer Objects
│   │   │       │   ├── BillDTO.java
│   │   │       │   ├── BillListDTO.java
│   │   │       │   ├── PaymentDTO.java
│   │   │       │   └── UserDTO.java
│   │   │       ├── exception/           # Custom Exceptions
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── ValidationException.java
│   │   │       │   └── PaymentProcessingException.java
│   │   │       ├── config/              # Configuration Classes
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── JpaConfig.java
│   │   │       ├── util/                # Utility Classes
│   │   │       │   ├── JwtUtil.java
│   │   │       │   └── ValidationUtil.java
│   │   │       └── BillManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties    # Configuration
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/
│   │           └── migration/            # Database migrations
│   └── test/
│       └── java/
│           └── com/billservice/
│               ├── controller/           # Integration Tests
│               └── service/              # Unit Tests
├── pom.xml                              # Maven Configuration
├── README.md                            # This file
├── docker-compose.yml                   # Docker Configuration
└── .gitignore
```

---

## 🚀 Installation & Setup

### Prerequisites

- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Download](https://maven.apache.org/)
- **MySQL 8.0+** - [Download](https://www.mysql.com/downloads/)
- **Git** - [Download](https://git-scm.com/)

### Step 1: Clone Repository

```bash
git clone https://github.com/yourusername/bill-management-platform.git
cd bill-management-platform
```

### Step 2: Setup MySQL Database

```bash
# Create database
mysql -u root -p
CREATE DATABASE payment_hub_users;
USE payment_hub_users;

# Run migrations
mysql -u root -p payment_hub_users < src/main/resources/db/migration/V1__init.sql
```

Or use Docker:

```bash
docker-compose up -d mysql
```

### Step 3: Configure Application Properties

Create `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api/v1

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/payment_hub_users
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.com.billservice=DEBUG
logging.level.org.springframework.web=INFO
```

### Step 4: Build Project

```bash
mvn clean install
```

### Step 5: Run Application

```bash
mvn spring-boot:run
```

Application will be available at: `http://localhost:8080`

---

## ⚙️ Configuration

### Database Configuration

**File:** `src/main/resources/application.properties`

```properties
# MySQL Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000

# JPA/Hibernate
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### Security Configuration

JWT token configuration in `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/v1/auth/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilter(new JwtAuthenticationFilter(authenticationManager()))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        return http.build();
    }
}
```

---

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=BillServiceTest
```

### Run Integration Tests

```bash
mvn test -Dgroups=integration
```

### Generate Coverage Report

```bash
mvn jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## 📈 Performance Considerations

- **Database Indexing:** Optimized queries with indexes on `user_id`, `bill_status`, and `due_date`
- **Lazy Loading:** Used strategically to reduce N+1 queries
- **DTO Transformation:** Reduces JSON payload size
- **Query Optimization:** JPQL with COALESCE for aggregations

---

## 🔒 Security Features

✅ **JWT Authentication** - Stateless, token-based authentication  
✅ **Password Encryption** - BCrypt hashing with salt  
✅ **Input Validation** - Spring Validation annotations  
✅ **SQL Injection Prevention** - JPA parameterized queries  
✅ **CORS Configuration** - Controlled cross-origin access  
✅ **Exception Handling** - No sensitive data in error messages  
✅ **DTO Pattern** - Prevents internal data exposure  
✅ **Transactional Integrity** - @Transactional annotations for ACID compliance  

---

## 📝 Sample Data

The project includes 100 pre-configured sample bills for testing across 7 users. Load sample data:

```bash
mysql -u root -p payment_hub_users < src/main/resources/db/sample-data/bills.sql
```

**Sample Bill Categories:**
- Utilities (Electric, Water, Gas)
- Housing (Rent, Mortgage, Property Tax)
- Insurance (Auto, Home, Health)
- Subscriptions (Streaming, Gym, Memberships)
- Professional (Consulting, Accounting, Software Licenses)

---

## 🤝 Contributing

### Development Workflow

1. **Create Feature Branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Follow Code Style:**
   - Use Google Java Style Guide
   - Run `mvn fmt:format` before commit

3. **Write Tests:**
   - Maintain >80% code coverage
   - Write unit and integration tests

4. **Commit Guidelines:**
   ```bash
   git commit -m "feat: add new bill filtering capability"
   git commit -m "fix: resolve N+1 query issue in BillService"
   git commit -m "docs: update API endpoint documentation"
   ```

5. **Push and Create Pull Request:**
   ```bash
   git push origin feature/your-feature-name
   ```

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 📧 Contact & Support

For questions, issues, or suggestions:
- **Email:** your-email@example.com
- **GitHub Issues:** [Report Bug / Request Feature](https://github.com/yourusername/bill-management-platform/issues)
- **Documentation:** [Wiki](https://github.com/yourusername/bill-management-platform/wiki)

---

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/projects/spring-data-jpa)
- [JWT Authentication](https://jwt.io/)
- [RESTful API Best Practices](https://restfulapi.net/)
- [Clean Code & Design Patterns](https://refactoring.guru/design-patterns)

---

Last Updated: January 2026