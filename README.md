# Transaction Processing Microservice - Setup Guide

## Prerequisites
- Java 21+ JDK installed
- Maven 3.8+
- Docker 20.10+ and Docker Compose v2+
- Git

## 1. Clone the Repository
```bash
git clone https://github.com/konst375/transaction-processing-microservice.git
cd transaction-processing-microservice
```

## 2. Build the Application
```bash
mvn clean package
```

## 3. Build Docker Image
```bash
docker build -t transaction-processing-service:latest .
```

## 4. Start Services
```bash
docker compose up -d
```

## Verification
Check that services are running:
```bash
docker compose ps
```

The application will be available at:
http://localhost:8080