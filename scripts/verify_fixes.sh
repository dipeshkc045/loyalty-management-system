#!/bash
echo "--- Verifying QA bug fixes ---"

# 1. Verify Rule Engine Auto-Reload
echo "Testing Rule Engine Auto-Reload..."
# Create a dummy rule
RULE_INFO=$(curl -s -X POST "http://localhost:8080/api/v1/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "Verification Test Rule",
    "conditions": "memberTier == BRONZE",
    "actions": "pointsEarned += 1",
    "isActive": true,
    "drlContent": "package com.lms.rule;\nrule \"Verification Test\"\nwhen\n  $f: com.lms.rule.model.TransactionFact(memberTier == \"BRONZE\")\nthen\n  $f.setBonusPoints(10);\nend"
  }')
RULE_ID=$(echo $RULE_INFO | jq -r '.id')
echo "Created Rule ID: $RULE_ID"
# Check if it's there
curl -s "http://localhost:8080/api/v1/rules" | grep "Verification Test Rule"

# 2. Verify Transaction Service Validation
echo -e "\nTesting Transaction Service Validation..."
curl -s -X POST "http://localhost:8080/api/v1/transactions" \
  -H "Content-Type: application/json" \
  -d '{"memberId": 1, "amount": -10.0, "paymentMethod": "CASH", "productCategory": "MISC"}' \
  | jq .

# 3. Verify Member Service Duplicate Handling
echo -e "\nTesting Member Service Duplicate Handling..."
# Create a member first (or use a known email)
EMAIL="duplicate.$(date +%s)@example.com"
curl -s -X POST "http://localhost:8080/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Original\", \"email\": \"$EMAIL\", \"phone\": \"123\"}" > /dev/null

echo "Attempting to create duplicate..."
curl -i -s -X POST "http://localhost:8080/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Duplicate\", \"email\": \"$EMAIL\", \"phone\": \"456\"}" \
  | grep "HTTP/1.1 409"

# 4. Verify Event Service Idempotency (Visual check of logic or dry run)
echo -e "\nTesting Event Service Idempotency (Manual check of IDs in logs)..."
# Trigger onboarding twice and check if IDs are different (though IDs are internal, we check if it responds)
curl -s -X POST "http://localhost:8080/api/v1/events/onboard" -H "Content-Type: application/json" -d '{"memberId": 1}'
echo ""
curl -s -X POST "http://localhost:8080/api/v1/events/onboard" -H "Content-Type: application/json" -d '{"memberId": 1}'
echo ""

echo -e "\n--- Verification Complete ---"
