import json
import random
import time
import os
from kafka import KafkaProducer

BOOTSTRAP_SERVERS = os.environ.get("KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
TOPIC = os.environ.get("KAFKA_TOPIC", "raw-sinistres")
FREQUENCY = int(os.environ.get("PRODUCE_FREQUENCY", "3"))

SCENARIOS = [
    {"name": "Normal auto claim", "montant": 3500.00, "type": "AUTO", "contrat": "CTR-100", "seuil": False},
    {"name": "Critical auto claim", "montant": 25000.00, "type": "AUTO", "contrat": "CTR-100", "seuil": True},
    {"name": "Habitation claim", "montant": 8500.00, "type": "HABITATION", "contrat": "CTR-200", "seuil": False},
    {"name": "Storm damage", "montant": 32000.00, "type": "HABITATION", "contrat": "CTR-200", "seuil": True},
    {"name": "Health claim", "montant": 2200.00, "type": "SANTE", "contrat": "CTR-300", "seuil": False},
    {"name": "Large health claim", "montant": 15000.00, "type": "SANTE", "contrat": "CTR-300", "seuil": True},
    {"name": "Multi-claim contract - high", "montant": 12000.00, "type": "AUTO", "contrat": "CTR-100", "seuil": True},
    {"name": "Multi-claim contract - low", "montant": 7500.00, "type": "AUTO", "contrat": "CTR-100", "seuil": False},
    {"name": "Edge case - minimal", "montant": 150.00, "type": "AUTO", "contrat": "CTR-200", "seuil": False},
    {"name": "Edge case - very large", "montant": 95000.00, "type": "AUTO", "contrat": "CTR-300", "seuil": True},
    {"name": "Collision claim", "montant": 4500.00, "type": "COLLISION", "contrat": "CTR-100", "seuil": False},
    {"name": "Fire claim", "montant": 28000.00, "type": "INCENDIE", "contrat": "CTR-200", "seuil": True},
    {"name": "Theft claim", "montant": 6500.00, "type": "VOL", "contrat": "CTR-300", "seuil": False},
    {"name": "Recent claim", "montant": 9800.00, "type": "AUTO", "contrat": "CTR-200", "seuil": False},
    {"name": "Historical claim", "montant": 18000.00, "type": "HABITATION", "contrat": "CTR-300", "seuil": True},
]


def build_message(scenario):
    return {
        "sinistreId": f"SIN-{random.randint(100, 999)}",
        "contratId": scenario["contrat"],
        "montantSinistre": scenario["montant"],
        "typeSinistre": scenario["type"],
        "dateDeclaration": int(time.time() * 1000),
        "seuilDepasse": scenario["seuil"],
    }


def main():
    print(f"Connecting to Kafka at {BOOTSTRAP_SERVERS}...")
    producer = KafkaProducer(
        bootstrap_servers=BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
        key_serializer=lambda k: k.encode("utf-8") if k else None,
        retries=5,
        retry_backoff_ms=1000,
    )

    print(f"Connected. Producing to topic '{TOPIC}' every {FREQUENCY}s")
    count = 0
    while True:
        scenario = random.choice(SCENARIOS)
        message = build_message(scenario)
        producer.send(TOPIC, key=message["contratId"], value=message)
        count += 1
        if count % 10 == 0:
            print(f"Produced {count} messages (last: {scenario['name']} - {scenario['montant']} EUR)")
        time.sleep(FREQUENCY)


if __name__ == "__main__":
    time.sleep(10)
    main()
