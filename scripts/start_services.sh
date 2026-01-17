#!/bin/bash

# Create logs directory if it doesn't exist
mkdir -p logs

SERVICES=(
  "api-gateway"
  "member-service"
  "transaction-service"
  "rule-engine-service"
  "reward-service"
  "event-service"
  "notification-service"
)

echo "Starting LMS Microservices..."

for SERVICE in "${SERVICES[@]}"; do
  echo "Checking $SERVICE..."
  if ps aux | grep -v grep | grep "$SERVICE-0.0.1-SNAPSHOT.jar" > /dev/null; then
    echo "$SERVICE is already running."
  else
    echo "Starting $SERVICE..."
    JAR_PATH="services/$SERVICE/build/libs/$SERVICE-0.0.1-SNAPSHOT.jar"
    if [ -f "$JAR_PATH" ]; then
      nohup java -jar "$JAR_PATH" > "logs/$SERVICE.log" 2>&1 &
      echo "$SERVICE started."
    else
      echo "Error: $JAR_PATH not found. Building first?"
      ./gradlew ":services:$SERVICE:bootJar"
      nohup java -jar "$JAR_PATH" > "logs/$SERVICE.log" 2>&1 &
      echo "$SERVICE built and started."
    fi
  fi
done

echo "All services initiated. Check logs/ for status."
