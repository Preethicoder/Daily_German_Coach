#!/bin/bash
# Test script for Daily German Coach API

cd "/Users/preethisivakumar/Documents/Daily German Coach API"
source .env

echo "🚀 Starting Daily German Coach API..."
mvn spring-boot:run > /tmp/dgc-app.log 2>&1 &
APP_PID=$!
echo "App started with PID: $APP_PID"

echo "⏳ Waiting 15 seconds for app to start..."
sleep 15

echo ""
echo "🧪 Testing API with German word: Baum"
echo "----------------------------------------"
curl -X POST http://localhost:8080/api/german/enrich/preview \
  -H "Content-Type: application/json" \
  -d '{"germanWord": "Baum"}' | jq .

echo ""
echo ""
echo "✅ Test complete! Stopping app..."
kill $APP_PID
wait $APP_PID 2>/dev/null
echo "✨ Done!"
