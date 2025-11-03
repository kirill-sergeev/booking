# **Booking Service**

This is a Spring Boot monolith application that provides a REST API for a simple accommodation booking system. It allows users to manage and book accommodation units, with features like dynamic pricing, availability checks, booking lifecycles (pending, confirmed, cancelled), and payment emulation.

The system uses PostgreSQL for its primary database (managed by Liquibase) and Redis for caching key statistics.

## **System Architecture**

The application is a single Spring Boot monolith service. It connects to two external services, both of which are managed via Docker:

1. **PostgreSQL (Database):** The primary data store for all persistent data, including units, users, bookings, and events.
2. **Redis (Cache):** Used to store and update a single, durable statistic: the total count of *available* units. This cache is updated transactionally with booking status changes.

### **Key Business Logic**

1. **Unit Management:**
    * Units can be added with properties (rooms, type, floor) and a baseCost.
    * The baseCost is the price set by the user.
2. **Booking Cost:**
    * The final booking totalCost is calculated as the unit.baseCost \+ a configurable system markup (default 15%).
    * This markup is defined in application.properties via booking.markup-percent.
3. **Availability & Search:**
    * The search endpoint (GET /api/v1/units/search) allows querying units by complex query.
    * It filters by unit properties (rooms, type, etc.) and, most importantly, by **date availability**.
    * The query supports full pagination and sorting via Spring Data JPA.
4. **Booking Lifecycle:**
    * **1\. PENDING:** A booking is created with POST /api/v1/bookings. Its status is PENDING.
        * At this point, the unit is considered "unavailable" and the "available units" count in Redis is **decremented**.
        * A 15-minute expiration timer is set on the booking (expiresAt).
    * **2\. CONFIRMED:** The user must "pay" by calling POST /api/v1/bookings/{id}/pay before the expiresAt time.
        * This emulates a successful payment.
        * The booking status changes to CONFIRMED and the expiresAt timer is removed.
        * The Redis count is *not* changed (it was already decremented).
    * **3\. EXPIRED (Auto-Cancel):** If the user does *not* pay within 15 minutes, a scheduled job (BookingCleanupService) runs every minute, finds all expired PENDING bookings, and changes their status to EXPIRED.
        * The Redis "available units" count is **incremented** (the unit is available again).
    * **4\. CANCELLED (User-Cancel):** A user can cancel a PENDING or CONFIRMED booking at any time by calling DELETE /api/v1/bookings/{id}.
        * The status changes to CANCELLED.
        * The Redis "available units" count is **incremented**.
5. **Redis Cache (Durability & Recovery):**
    * The docker-compose.yml file enables AOF (Append Only File) persistence for Redis, ensuring the cache value can be recovered after a Redis restart.
6. **Data Seeding:**
    * **10 Units:** 002-insert-initial-data.sql (Liquibase) inserts 10 specific units and their creation events.
    * **90 Units:** DataInitializer (Spring CommandLineRunner) runs on application start. It checks the total unit count and, if it's less than 100, it creates 90 random units and updates the Redis cache accordingly.

## **How to Build and Run**

### **Prerequisites**

* Java 21
* Docker & Docker Compose
* An internet connection (to download Gradle dependencies)

### **Steps**

1. Start the Database and Cache:  
   Open a terminal in the project root and run:  
   docker compose up \-d

   This will start PostgreSQL on port 5432 and Redis on port 6379\.
2. Build the Application:  
   In the same terminal, build the project using the Gradle wrapper:  
   \# On macOS/Linux  
   ./gradlew build

   \# On Windows  
   .\\gradlew.bat build

   This will compile the code, run the tests, and create an executable JAR file in build/libs/.
3. Run the Application:  
   You can run the application directly from Gradle (best for development):  
   ./gradlew bootRun

   Or, you can run the JAR file built in the previous step:  
   java \-jar build/libs/booking-service-1.0.0-SNAPSHOT.jar

4. **Access the Application:**
    * **API (Swagger UI):** Open your browser to [http://localhost:8080/api-docs.html](http://localhost:8080/api-docs.html)
    * **API (OpenAPI Spec):** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
    * **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
