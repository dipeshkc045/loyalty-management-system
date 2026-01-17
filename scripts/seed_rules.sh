#!/bin/bash

BASE_URL="http://localhost:8080/api/v1/rules"

echo "Seeding rules to Rule Engine Service..."

# 1. Gold Tier Multiplier
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "DROOLS",
    "ruleName": "Gold Tier Multiplier",
    "conditions": "{}",
    "actions": "{}",
    "drlContent": "package com.lms.rules;\nimport com.lms.rule.model.TransactionFact;\nrule \"Gold Tier Multiplier\"\nwhen\n  $f: TransactionFact(memberTier == \"GOLD\")\nthen\n  $f.setPointMultiplier($f.getPointMultiplier() * 1.5);\nend",
    "priority": 5,
    "isActive": true
  }'
echo -e "\nAdded Gold Tier Multiplier"

# 2. Electronics Category Bonus
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "DROOLS",
    "ruleName": "Electronics Bonus",
    "conditions": "{}",
    "actions": "{}",
    "drlContent": "package com.lms.rules;\nimport com.lms.rule.model.TransactionFact;\nrule \"Electronics Bonus\"\nwhen\n  $f: TransactionFact(productCategory == \"ELECTRONICS\")\nthen\n  $f.setBonusPoints($f.getBonusPoints() + 50);\nend",
    "priority": 10,
    "isActive": true
  }'
echo -e "\nAdded Electronics Bonus"

# 3. Weekend Promo (simplified for now to just a named rule)
curl -s -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleType": "DROOLS",
    "ruleName": "Weekend Promo",
    "conditions": "{}",
    "actions": "{}",
    "drlContent": "package com.lms.rules;\nimport com.lms.rule.model.TransactionFact;\nrule \"Weekend Promo\"\nwhen\n  $f: TransactionFact(paymentMethod == \"CREDIT_CARD\")\nthen\n  $f.setBonusPoints($f.getBonusPoints() + 20);\nend",
    "priority": 1,
    "isActive": true
  }'
echo -e "\nAdded Weekend Promo (Credit Card bonus)"

echo -e "\nRules seeding completed."
