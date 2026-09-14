FROM confluentinc/cp-kafka:7.5.0

COPY kafka-init/start.sh /start.sh
COPY microcks/sinistres-asyncapi.yml /deployments/sinistres-asyncapi.yml

CMD ["/bin/bash", "/start.sh"]
