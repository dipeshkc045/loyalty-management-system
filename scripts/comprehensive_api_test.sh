#!/bin/bash

BASE_URL="http://localhost:8080"
echo "=== Comprehensive LMS API Test ==="
echo "Testing all endpoints..."

# Test 1: Member Service - Create Member
echo -e "\n[TEST 1] Creating a new member..."
EMAIL="test.$(date +%s)@example.com"
MEMBER_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Test User\", \"email\": \"$EMAIL\", \"phone\": \"1234567890\", \"role\": \"CUSTOMER\"}")
HTTP_STATUS=$(echo "$MEMBER_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
MEMBER_BODY=$(echo "$MEMBER_RESPONSE" | sed '/HTTP_STATUS/d')
echo "Status: $HTTP_STATUS"
echo "Response: $MEMBER_BODY"
MEMBER_ID=$(echo "$MEMBER_BODY" | jq -r '.id' 2>/dev/null || echo "null")
echo "Member ID: $MEMBER_ID"

# Test 2: Member Service - Get Member Points
echo -e "\n[TEST 2] Getting member points..."
curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/api/v1/members/$MEMBER_ID/points"

# Test 3: Rule Engine - List Rules
echo -e "\n[TEST 3] Listing all rules..."
RULES=$(curl -s -w "\nHTTP_STATUS:%{http_code}" "$BASE_URL/api/v1/rules")
HTTP_STATUS=$(echo "$RULES" | grep "HTTP_STATUS" | cut -d: -f2)
echo "Status: $HTTP_STATUS"
echo "$RULES" | sed '/HTTP_STATUS/d' | jq -r 'length' 2>/dev/null && echo "rules found" || echo "Error"

# Test 4: Transaction Service - Create Transaction (if member exists)
if [ "$MEMBER_ID" != "null" ] && [ -n "$MEMBER_ID" ]; then
    echo -e "\n[TEST 4] Creating a transaction..."
    TX_RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST "$BASE_URL/api/v1/transactions" \
      -H "Content-Type: application/json" \
      -d "{\"memberId\": $MEMBER_ID, \"amount\": 100.0, \"paymentMethod\": \"CASH\", \"productCategory\": \"FOOD\"}")
    HTTP_STATUS=$(echo "$TX_RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
    echo "Status: $HTTP_STATUS"
    echo "$TX_RESPONSE" | sed '/HTTP_STATUS/d'
    
    # Wait for event processing
    echo "Waiting 5 seconds for event processing..."
    sleep 5
    
    # Test 5: Check points after transaction
    echo -e "\n[TEST 5] Checking points after transaction..."
    curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/api/v1/members/$MEMBER_ID/points"
    
    # Test 6: Transaction Summary
    echo -e "\n[TEST 6] Getting transaction summary..."
    curl -s -w "\nHTTP Status: %{http_code}\n" "$BASE_URL/api/v1/transactions/summary/$MEMBER_ID"
else
    echo -e "\n[SKIP] Tests 4-6 skipped (no valid member ID)"
fi

# Test 7: Event Service - Onboarding
echo -e "\n[TEST 7] Testing onboarding event..."
if [ "$MEMBER_ID" != "null" ] && [ -n "$MEMBER_ID" ]; then
    curl -s -w "\nHTTP Status: %{http_code}\n" -X POST "$BASE_URL/api/v1/events/onboard" \
      -H "Content-Type: application/json" \
      -d "{\"memberId\": $MEMBER_ID}"
    sleep 3
    echo "Points after onboarding:"
    curl -s "$BASE_URL/api/v1/members/$MEMBER_ID/points"
    echo ""
fi

# Test 8: QA Bug Fixes - Duplicate Member
echo -e "\n[TEST 8] Testing duplicate member handling (should return 409)..."
curl -i -s -X POST "$BASE_URL/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Duplicate\", \"email\": \"$EMAIL\", \"phone\": \"999\"}" \
  | grep "HTTP/1.1"

# Test 9: QA Bug Fixes - Negative Transaction
echo -e "\n[TEST 9] Testing negative transaction validation (should return 400)..."
if [ "$MEMBER_ID" != "null" ] && [ -n "$MEMBER_ID" ]; then
    curl -i -s -X POST "$BASE_URL/api/v1/transactions" \
      -H "Content-Type: application/json" \
      -d "{\"memberId\": $MEMBER_ID, \"amount\": -50.0, \"paymentMethod\": \"CASH\"}" \
      | grep "HTTP/1.1"
fi

echo -e "\n=== Test Complete ==="
