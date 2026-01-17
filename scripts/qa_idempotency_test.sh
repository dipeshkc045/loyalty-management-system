#!/bash
# Create member A
MEMBER_A=$(curl -s -X POST "http://localhost:8080/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Member A\",
    \"email\": \"a.$(date +%s)@example.com\",
    \"phone\": \"111\",
    \"role\": \"CUSTOMER\"
  }" | jq -r '.id')

# Create member B
MEMBER_B=$(curl -s -X POST "http://localhost:8080/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Member B\",
    \"email\": \"b.$(date +%s)@example.com\",
    \"phone\": \"222\",
    \"role\": \"CUSTOMER\"
  }" | jq -r '.id')

echo "Member A: $MEMBER_A, Member B: $MEMBER_B"

echo "Onboarding Member A..."
curl -s -X POST "http://localhost:8080/api/v1/events/onboard" -H "Content-Type: application/json" -d "{\"memberId\": $MEMBER_A}"
sleep 5
POINTS_A=$(curl -s "http://localhost:8080/api/v1/members/$MEMBER_A/points")
echo "Member A points: $POINTS_A"

echo "Onboarding Member B..."
curl -s -X POST "http://localhost:8080/api/v1/events/onboard" -H "Content-Type: application/json" -d "{\"memberId\": $MEMBER_B}"
sleep 5
POINTS_B=$(curl -s "http://localhost:8080/api/v1/members/$MEMBER_B/points")
echo "Member B points: $POINTS_B"
