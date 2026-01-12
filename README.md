# Rate Limiter – Spring Boot

A Spring Boot–based **API Rate Limiter** implementation designed to control incoming request traffic and protect backend services from overload, abuse, and excessive usage.

This project demonstrates a **token bucket–style rate limiting mechanism**, with support for scalable and configurable limits, making it suitable for real-world backend systems.

---

## Features

- API request throttling to prevent abuse
- Token Bucket–based rate limiting logic
- Configurable request limits and refill intervals
- Designed for scalability and extensibility
- Clean, modular Spring Boot architecture
- Easy to integrate with existing services

---

## Why Rate Limiting?

Rate limiting is a critical backend concern used to:
- Protect APIs from excessive traffic
- Ensure fair usage among clients
- Improve system stability and availability
- Prevent denial-of-service–like scenarios

This project showcases how to implement these concepts cleanly using Spring Boot.

---

## Tech Stack

- **Java**
- **Spring Boot**
- **Maven**
- **Redis** (for distributed token storage, if configured)
- **JUnit / Mockito** (for testing)

---

## Project Structure

Rate-Limiter-Spring-Boot
├── src/main/java
│ ├── config # Rate limiter configurations
│ ├── controller # REST endpoints
│ ├── service # Core rate limiting logic
│ └── util # Helper classes
├── src/main/resources
│ └── application.yml
└── pom.xml

---

## Configuration

Rate limiting behavior can be configured in `application.yml` or `application.properties`.

Example:

```yaml
rate-limiter:
  max-requests: 10
  refill-interval-ms: 1000
