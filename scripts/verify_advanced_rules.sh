#!/bin/bash
BASE_URL="http://localhost:8080/api/v1"

echo "Verifying Advanced Loyalty Logic..."

# 1. Create a product if not exists
echo "Creating QR_PAYMENT product..."
curl -s -X POST "$BASE_URL/products" \
  -H "Content-Type: application/json" \
  -d '{"code": "QR_PAYMENT", "name": "QR Payment", "category": "PAYMENT", "description": "QR scan payment"}'

# 2. Setup Rules
echo "Setting up rules..."
bash scripts/setup_advanced_rules.sh

# 3. Create a member
echo "Creating member..."
MEMBER_ID=$(curl -s -X POST "$BASE_URL/members" \
  -H "Content-Type: application/json" \
  -d '{"email": "adv_test_3@example.com", "name": "Advanced Tester 3", "phone": "5550003"}' | jq '.id')
echo "Member ID: $MEMBER_ID"

# 4. Trigger Onboarding Event
echo "Triggering Onboarding event..."
curl -s -X POST "$BASE_URL/events/trigger" \
  -H "Content-Type: application/json" \
  -d "{\"eventType\": \"ONBOARDING\", \"memberId\": $MEMBER_ID}"

# 5. Create a QR Payment Transaction (Should trigger 50 bonus points)
echo "Creating QR Payment transaction (Amount: 500)..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"memberId\": $MEMBER_ID, \"amount\": 500, \"paymentMethod\": \"QR\", \"productCategory\": \"QR_PAYMENT\"}"

# Wait for async processing
echo "Waiting for reward processing..."
sleep 5

# 6. Check points
echo "Checking member points..."
curl -s "$BASE_URL/members/$MEMBER_ID" | jq '{name: .name, totalPoints: .totalPoints}'

echo "Verification complete."
