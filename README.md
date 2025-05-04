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

## 2. Environment Setup
Do not forget to provide a .evn file based on example.evn
1. Copy the example environment file:
    ```bash
    cp example.env .env
    ```
2. Edit the .env file with your configuration:
    ```.dotenv
    EXCHANGE_RATE_SERVICE_PROVIDER_BASE_URL=https://www.alphavantage.co/query
    EXCHANGE_RATE_SERVICE_PROVIDER_API_KEY=your_api_key
    
    POSTGRES_USER=your_postgres_user
    POSTGRES_PASSWORD=your_postgres_password
    ```

## 3. Build the Application
```bash
mvn clean package
```

## 4. Build Docker Image
```bash
docker build -t transaction-processing-service:latest .
```

## 5. Start Services
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