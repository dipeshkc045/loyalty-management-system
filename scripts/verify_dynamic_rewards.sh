#!/bin/bash
BASE_URL="http://localhost:8080/api/v1"

echo "Verifying Dynamic Proportional Rewards..."

# 1. Setup Proportional Rule
# This rule gives 2.5 points per dollar for any transaction
curl -s -X POST "$BASE_URL/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "SIMPLE",
    "ruleName": "Proportional 2.5x Reward",
    "evaluationType": "TRANSACTION",
    "rewardType": "POINTS",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"pointsMultiplier\": 2.5, \"reason\": \"Proportional Reward\"}]",
    "priority": 15,
    "isActive": true
  }'
echo -e "\nAdded Proportional Rule (2.5x)"

# 2. Create a new member
MEMBER_ID=$(curl -s -X POST "$BASE_URL/members" \
  -H "Content-Type: application/json" \
  -d '{"email": "prop_test@example.com", "name": "Proportional Tester", "phone": "5551111"}' | jq '.id')
echo "Created Member ID: $MEMBER_ID"

# 3. Create a transaction (Amount: 1000)
# Expected Bonus: 1000 * 2.5 = 2500
# Expected Base: 1000
# Expected Total: 3500
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "{\"memberId\": $MEMBER_ID, \"amount\": 1000, \"paymentMethod\": \"CARD\", \"productCategory\": \"GENERAL\"}"
echo "Transaction created (Amount: 1000)"

# Wait for processing
sleep 20

# 4. Final verification
TOTAL_POINTS=$(curl -s "$BASE_URL/members/$MEMBER_ID" | jq '.totalPoints')
echo "Member Total Points: $TOTAL_POINTS"

if [ "$TOTAL_POINTS" -eq 3500 ]; then
  echo "✅ SUCCESS: Dynamic proportional reward applied correctly (1000 base + 2500 bonus)."
else
  echo "❌ FAILURE: Expected 3500 points, but got $TOTAL_POINTS."
fi
