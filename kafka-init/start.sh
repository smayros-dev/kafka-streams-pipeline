#!/bin/bash
set -e

BOOTSTRAP=${BOOTSTRAP_SERVERS:-kafka:29092}
CONNECT_URL=${KAFKA_CONNECT_URL:-http://kafka-connect:8083}

echo "Waiting for Kafka..."
until kafka-topics --bootstrap-server "$BOOTSTRAP" --list > /dev/null 2>&1; do
  sleep 2
done

echo "Creating Kafka topics..."
kafka-topics --bootstrap-server "$BOOTSTRAP" --create --topic raw-sinistres --partitions 6 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server "$BOOTSTRAP" --create --topic sinistres-critiques --partitions 6 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server "$BOOTSTRAP" --create --topic stats-contrat-5m --partitions 6 --replication-factor 1 --if-not-exists
kafka-topics --bootstrap-server "$BOOTSTRAP" --create --topic sinistres-enrichis --partitions 6 --replication-factor 1 --if-not-exists

echo "Waiting for Kafka Connect..."
until curl -sf "$CONNECT_URL/connectors" > /dev/null 2>&1; do
  sleep 2
done

echo "Creating MongoDB Sink Connector for sinistres-critiques..."
curl -s -X POST "$CONNECT_URL/connectors" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mongo-sinistres-critiques",
    "config": {
      "connector.class": "com.mongodb.kafka.connect.MongoSinkConnector",
      "tasks.max": "1",
      "topics": "sinistres-critiques",
      "connection.uri": "mongodb://admin:password@mongodb:27017",
      "database": "dommages_db",
      "collection": "sinistres_critiques",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "insert.mode": "insert",
      "bulk.write.enabled": "true"
    }
  }'
echo ""

echo "Creating MongoDB Sink Connector for stats-contrat-5m..."
curl -s -X POST "$CONNECT_URL/connectors" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mongo-stats-contrat",
    "config": {
      "connector.class": "com.mongodb.kafka.connect.MongoSinkConnector",
      "tasks.max": "1",
      "topics": "stats-contrat-5m",
      "connection.uri": "mongodb://admin:password@mongodb:27017",
      "database": "dommages_db",
      "collection": "stats_contrat",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "insert.mode": "insert",
      "bulk.write.enabled": "true"
    }
  }'
echo ""

echo "Kafka init complete (topics + sink connectors created)."
echo "kafka-producer will produce dynamic mock data to raw-sinistres topic."
