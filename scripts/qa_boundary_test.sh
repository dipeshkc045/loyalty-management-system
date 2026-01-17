#!/bash
# Create a member
MEMBER_INFO=$(curl -s -X POST "http://localhost:8080/api/v1/members" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Limit Test",
    "email": "limit.'$(date +%s)'@example.com",
    "phone": "555-1111",
    "role": "CUSTOMER"
  }')
MEMBER_ID=$(echo $MEMBER_INFO | jq -r '.id')
echo "Member ID: $MEMBER_ID"

echo "Negative Transaction Request..."
curl -X POST "http://localhost:8080/api/v1/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"memberId\": $MEMBER_ID, \"amount\": -100.0, \"paymentMethod\": \"CASH\", \"productCategory\": \"MISC\"}"

echo -e "\nWaiting for processing..."
sleep 5

echo "Points after negative transaction:"
curl "http://localhost:8080/api/v1/members/$MEMBER_ID/points"
echo ""
