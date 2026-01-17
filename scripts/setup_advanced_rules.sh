#!/bin/bash
BASE_URL="http://localhost:8080/api/v1/rules"

echo "Setting up new Advanced Rules..."

# 1. QR Payment Rule (Per Transaction)
# Admin selects QR_PAYMENT, range 100-1000, 50 points
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "SIMPLE",
    "ruleName": "QR Payment Bonus (Large Range)",
    "evaluationType": "TRANSACTION",
    "targetProductCode": "QR_PAYMENT",
    "minAmount": 10,
    "maxAmount": 10000,
    "rewardType": "POINTS",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"points\": 500, \"reason\": \"QR Payment Bonus\"}]",
    "priority": 10,
    "isActive": true
  }'
echo -e "\nAdded QR Payment Rule"

# 2. Customer Onboarding Rule (Event Based)
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "EVENT",
    "ruleName": "Customer Onboarding Welcome",
    "evaluationType": "EVENT",
    "rewardType": "POINTS",
    "conditions": "{\"eventType\": \"ONBOARDING\"}",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"points\": 200, \"reason\": \"Onboarding Welcome Points\"}]",
    "priority": 1,
    "isActive": true
  }'
echo -e "\nAdded Onboarding Rule"

# 3. Monthly Volume Bonus (Aggregated)
# If > 5 transactions in a month, award 100 points
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "SIMPLE",
    "ruleName": "Monthly Power User",
    "evaluationType": "MONTHLY",
    "minVolume": 5,
    "rewardType": "POINTS",
    "actions": "[{\"type\": \"AWARD_POINTS\", \"points\": 100, \"reason\": \"Monthly Power User Bonus\"}]",
    "priority": 5,
    "isActive": true
  }'
echo -e "\nAdded Monthly Volume Bonus"

# 4. Quarterly High Spender (Aggregated)
# If amount > 5000 in a quarter, 10% discount on next purchase (or points)
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "SIMPLE",
    "ruleName": "Quarterly High Spender",
    "evaluationType": "QUARTERLY",
    "minAmount": 5000,
    "rewardType": "DISCOUNT",
    "actions": "[{\"type\": \"AWARD_DISCOUNT\", \"discountPercentage\": 10, \"reason\": \"Quarterly High Spender Badge\"}]",
    "priority": 5,
    "isActive": true
  }'
echo -e "\nAdded Quarterly High Spender"

echo -e "\nRules setup completed."
