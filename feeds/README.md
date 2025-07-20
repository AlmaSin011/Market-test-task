# PriceFeed Service

## Overview
This is a lightweight mock service providing current stock/crypto prices by symbol. It exposes a simple REST API to fetch the latest price.

## Prerequisites
- Java 21
- Maven 3.9+
- JDK 21 runtime for production

## Build & Run

```bash
mvn clean package -DskipTests
java -jar target/pricefeed-0.0.1-SNAPSHOT.jar
```

## API Usage
Get Current Price
Retrieve the latest price for a given symbol.

HTTP GET /price?symbol={symbol}

### Example:

```bash
bash
curl -X GET "http://localhost:8080/price?symbol=AAPL" -H "Accept: application/json"
```
