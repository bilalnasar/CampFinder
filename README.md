# CampFinder

CampFinder is a Spring Boot application designed to check and notify users about the availability of camping sites in Ontario Provincial Parks. It periodically checks for available campsites and sends SMS notifications when sites become available.

## Features

- Periodic checking of campsite availability
- SMS notifications for available campsites
- RESTful API endpoints to start and stop availability checks
- Configurable date range for availability checks

## Prerequisites

- Java 17 or higher
- Twilio account for SMS notifications

## Setup

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/CampFinder.git
   cd CampFinder
   ```

2. Configure Twilio credentials:
   Create a `application.properties` file in `src/main/resources/` and add the following properties:
   ```
   twilio.account.sid=your_account_sid
   twilio.auth.token=your_auth_token
   twilio.phone.number=your_twilio_phone_number
   twilio.to.phone.number=recipient_phone_number
   ```

3. Build the project:
   ```
   mvn clean package
   ```

## Running the Application

1. Run the application using Maven:
   ```
   mvn spring-boot:run
   ```

2. The application will start on `http://localhost:8080`

## Usage

1. Start an availability check:
   ```
   GET http://localhost:8080/check-availability?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&parks=Park1
   ```
   Replace YYYY-MM-DD with your desired start and end dates and parks with the names of the parks you want to check. For example:
   ```
   GET http://localhost:8080/check-availability?startDate=2024-08-31&endDate=2024-09-02&parks=Killbear&parks=Chutes
   ```

2. Stop the availability check:
   ```
   GET http://localhost:8080/stop-availability-check
   ```

## Docker Support

You can also run the application using Docker:

1. Build the Docker image:
   ```
   docker build -t campfinder .
   ```

2. Run the Docker container:
   ```
   docker run -p 8080:8080 campfinder
   ```

## Using the Frontend

If you want to use the frontend, you can run it using npm (make sure that the backend is running on http://localhost:8080):

1. cd campfinder-frontend
2. npm install
3. npm start
4. Open http://localhost:3000 in your browser