#!/bin/bash
set -e

BOOTSTRAP=${KAFKA_BOOTSTRAP_SERVERS:-kafka:29092}
TOPIC=${KAFKA_TOPIC:-raw-sinistres}
FREQUENCY=${PRODUCE_FREQUENCY:-3}

echo "Waiting for Kafka..."
until kafka-topics --bootstrap-server "$BOOTSTRAP" --list > /dev/null 2>&1; do
  sleep 2
done

echo "Kafka ready. Producing sinistres to '$TOPIC' every ${FREQUENCY}s"

while true; do
  TYPE_INDEX=$((RANDOM % 15))

  case $TYPE_INDEX in
    0)  # Normal auto claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="3500.00"
        TYPE="AUTO"
        CONTRAT="CTR-100"
        SEUIL="false"
        ;;
    1)  # Critical auto claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="25000.00"
        TYPE="AUTO"
        CONTRAT="CTR-100"
        SEUIL="true"
        ;;
    2)  # Habitation claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="8500.00"
        TYPE="HABITATION"
        CONTRAT="CTR-200"
        SEUIL="false"
        ;;
    3)  # Storm damage
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="32000.00"
        TYPE="HABITATION"
        CONTRAT="CTR-200"
        SEUIL="true"
        ;;
    4)  # Health claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="2200.00"
        TYPE="SANTE"
        CONTRAT="CTR-300"
        SEUIL="false"
        ;;
    5)  # Large health claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="15000.00"
        TYPE="SANTE"
        CONTRAT="CTR-300"
        SEUIL="true"
        ;;
    6)  # Multi-claim contract - high
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="12000.00"
        TYPE="AUTO"
        CONTRAT="CTR-100"
        SEUIL="true"
        ;;
    7)  # Multi-claim contract - low
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="7500.00"
        TYPE="AUTO"
        CONTRAT="CTR-100"
        SEUIL="false"
        ;;
    8)  # Edge case - minimal
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="150.00"
        TYPE="AUTO"
        CONTRAT="CTR-200"
        SEUIL="false"
        ;;
    9)  # Edge case - very large
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="95000.00"
        TYPE="AUTO"
        CONTRAT="CTR-300"
        SEUIL="true"
        ;;
    10) # Collision claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="4500.00"
        TYPE="COLLISION"
        CONTRAT="CTR-100"
        SEUIL="false"
        ;;
    11) # Fire claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="28000.00"
        TYPE="INCENDIE"
        CONTRAT="CTR-200"
        SEUIL="true"
        ;;
    12) # Theft claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="6500.00"
        TYPE="VOL"
        CONTRAT="CTR-300"
        SEUIL="false"
        ;;
    13) # Recent claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="9800.00"
        TYPE="AUTO"
        CONTRAT="CTR-200"
        SEUIL="false"
        ;;
    14) # Historical claim
        SINISTRE_ID="SIN-$((RANDOM % 900 + 100))"
        MONTANT="18000.00"
        TYPE="HABITATION"
        CONTRAT="CTR-300"
        SEUIL="true"
        ;;
  esac

  DATE_EPOCH=$(($(date +%s) * 1000))

  JSON="{\"sinistreId\":\"${SINISTRE_ID}\",\"contratId\":\"${CONTRAT}\",\"montantSinistre\":${MONTANT},\"typeSinistre\":\"${TYPE}\",\"dateDeclaration\":${DATE_EPOCH},\"seuilDepasse\":${SEUIL}}"

  echo "$JSON" | kafka-console-producer --broker-list "$BOOTSTRAP" --topic "$TOPIC" 2>/dev/null

  sleep "$FREQUENCY"
done
