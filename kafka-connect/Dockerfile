FROM confluentinc/cp-kafka-connect:7.5.0

USER root

RUN mkdir -p /usr/share/confluent-hub-components/mongodb-kafka-connect

RUN curl -sL -o /usr/share/confluent-hub-components/mongodb-kafka-connect/mongo-kafka-connect-1.11.0.jar \
    "https://repo1.maven.org/maven2/org/mongodb/kafka/mongo-kafka-connect/1.11.0/mongo-kafka-connect-1.11.0.jar" && \
    curl -sL -o /usr/share/confluent-hub-components/mongodb-kafka-connect/mongodb-driver-sync-4.6.0.jar \
    "https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.6.0/mongodb-driver-sync-4.6.0.jar" && \
    curl -sL -o /usr/share/confluent-hub-components/mongodb-kafka-connect/mongodb-driver-core-4.6.0.jar \
    "https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-core/4.6.0/mongodb-driver-core-4.6.0.jar" && \
    curl -sL -o /usr/share/confluent-hub-components/mongodb-kafka-connect/bson-4.6.0.jar \
    "https://repo1.maven.org/maven2/org/mongodb/bson/4.6.0/bson-4.6.0.jar" && \
    chown -R appuser:appuser /usr/share/confluent-hub-components/mongodb-kafka-connect

USER appuser
