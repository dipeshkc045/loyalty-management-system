#!/bin/bash
# Direct ports to bypass gateway if it's unstable
MEMBER_URL="http://localhost:8081"
RULE_URL="http://localhost:8083"
EVENT_URL="http://localhost:8085"

echo "=== Generic Rule Verification (Direct) ==="

# 1. Create a member
EMAIL="test.$(date +%s)@example.com"
MEMBER_RESPONSE=$(curl -s -X POST "$MEMBER_URL/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Generic Rule Test\",
    \"email\": \"$EMAIL\",
    \"phone\": \"1234567890\",
    \"role\": \"CUSTOMER\"
  }")
MEMBER_ID=$(echo $MEMBER_RESPONSE | jq -r '.id')
if [ "$MEMBER_ID" == "null" ] || [ -z "$MEMBER_ID" ]; then
  echo "Failed to create member: $MEMBER_RESPONSE"
  exit 1
fi
echo "Created Member ID: $MEMBER_ID"

# 2. Create ONBOARDING rule
curl -s -X POST "$RULE_URL/api/v1/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "EVENT",
    "ruleName": "Onboarding Bonus (Generic)",
    "priority": 1,
    "isActive": true,
    "conditions": "{\"eventType\": \"ONBOARDING\"}",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"points\": 555, \"reason\": \"Welcome Bonus\", \"memberIdField\": \"memberId\", \"transactionIdField\": \"transactionId\"}]"
  }'
echo -e "\nCreated Onboarding Rule"

# 3. Create REFERRAL rule
REFERRER_EMAIL="referrer.$(date +%s)@example.com"
REFERRER_RESPONSE=$(curl -s -X POST "$MEMBER_URL/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Referrer Member\",
    \"email\": \"$REFERRER_EMAIL\",
    \"phone\": \"0000000000\",
    \"role\": \"CUSTOMER\"
  }")
REFERRER_ID=$(echo $REFERRER_RESPONSE | jq -r '.id')
echo "Created Referrer ID: $REFERRER_ID"

curl -s -X POST "$RULE_URL/api/v1/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "EVENT",
    "ruleName": "Referral Bonus (Generic)",
    "priority": 1,
    "isActive": true,
    "conditions": "{\"eventType\": \"REFERRAL\"}",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"points\": 300, \"reason\": \"Referrer Bonus\", \"memberIdField\": \"referrerId\", \"transactionIdField\": \"transactionId1\"}, {\"type\": \"AWARD_POINTS\", \"points\": 150, \"reason\": \"Referee Bonus\", \"memberIdField\": \"refereeId\", \"transactionIdField\": \"transactionId2\"}]"
  }'
echo -e "\nCreated Referral Rule"

# 4. Trigger Onboarding
curl -s -X POST "$EVENT_URL/api/v1/events/onboard" \
  -H "Content-Type: application/json" \
  -d "{\"memberId\": $MEMBER_ID}"
echo -e "\nTriggered Onboarding"

# 5. Trigger Referral
curl -s -X POST "$EVENT_URL/api/v1/events/referral" \
  -H "Content-Type: application/json" \
  -d "{\"referrerId\": $REFERRER_ID, \"refereeId\": $MEMBER_ID}"
echo -e "\nTriggered Referral"

# 6. Wait and check points
echo "Waiting 5 seconds for processing..."
sleep 5

echo -e "\nPoints for Member $MEMBER_ID (Expected: 555 + 150 = 705):"
curl -s "$MEMBER_URL/api/v1/members/$MEMBER_ID/points"

echo -e "\nPoints for Referrer $REFERRER_ID (Expected: 300):"
curl -s "$MEMBER_URL/api/v1/members/$REFERRER_ID/points"

echo -e "\n=== Test Complete ==="
