#!/bin/bash

BASE_URL="http://localhost:8081/api/v1"

echo "Warming up cache..."
curl -s "$BASE_URL/members/stats" > /dev/null
curl -s "$BASE_URL/members?page=0&size=20" > /dev/null

echo "------------------------------------------------"
echo "Testing /members/stats (Cached)"
echo "------------------------------------------------"
for i in {1..5}; do
    curl -w "Time: %{time_total}s\n" -o /dev/null -s "$BASE_URL/members/stats"
done

echo "------------------------------------------------"
echo "Testing /members (Paginated)"
echo "------------------------------------------------"
for i in {1..5}; do
    curl -w "Time: %{time_total}s\n" -o /dev/null -s "$BASE_URL/members?page=0&size=20"
done

echo "------------------------------------------------"
echo "Testing /members search (Filtered)"
echo "------------------------------------------------"
for i in {1..5}; do
    curl -w "Time: %{time_total}s\n" -o /dev/null -s "$BASE_URL/members?search=user"
done
