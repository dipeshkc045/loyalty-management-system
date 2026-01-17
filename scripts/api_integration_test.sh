#!/bin/bash

BASE_URL="http://localhost:8080" # API Gateway

echo "=== Loyalty Management System API Integration Test ==="

# 1. Create a Member
echo -e "\n1. Creating a platinum-potential member..."
EMAIL="jane.doe.$(date +%s)@example.com"
MEMBER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/members" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Jane Doe\",
    \"email\": \"$EMAIL\",
    \"phone\": \"9876543210\",
    \"role\": \"CUSTOMER\"
  }")
MEMBER_ID=$(echo $MEMBER_RESPONSE | jq -r '.id')
echo "Created Member ID: $MEMBER_ID"

# 2. Setup a Drools Rule (Platinum Bonus)
echo -e "\n2. Creating a Drools rule for platinum members..."
curl -s -X POST "$BASE_URL/api/v1/rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "DROOLS",
    "ruleName": "Platinum 2x Multiplier",
    "conditions": "{}",
    "actions": "{}",
    "drlContent": "package com.lms.rules;\nimport com.lms.rule.model.TransactionFact;\nrule \"Platinum Bonus\"\nwhen\n  $f: TransactionFact(memberTier == \"PLATINUM\")\nthen\n  $f.setPointMultiplier(2.0);\n  $f.setBonusPoints(100);\nend",
    "priority": 10,
    "isActive": true
  }'

# 3. Create a Transaction
echo -e "\n3. Creating a transaction for the member..."
TX_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/transactions" \
  -H "Content-Type: application/json" \
  -d "{
    \"memberId\": $MEMBER_ID,
    \"amount\": 500.0,
    \"paymentMethod\": \"QR\",
    \"productCategory\": \"ELECTRONICS\"
  }")
TX_ID=$(echo $TX_RESPONSE | jq -r '.id')
echo "Created Transaction ID: $TX_ID"

# Wait for event processing
echo "Waiting 3 seconds for event processing..."
sleep 3

# 4. Check Point Balance
echo -e "\n4. Verifying point balance..."
curl -s -X GET "$BASE_URL/api/v1/members/$MEMBER_ID/points"

# 5. Check Transaction Summary
echo -e "\n5. Checking transaction summary..."
curl -s -X GET "$BASE_URL/api/v1/transactions/summary/$MEMBER_ID?period=MONTHLY"

# 6. List Active Rules
echo -e "\n6. Listing active rules..."
curl -s -X GET "$BASE_URL/api/v1/rules"

echo -e "\n=== Test Complete ==="
