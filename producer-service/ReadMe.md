Topic creation example:

docker exec -it kafka_broker /opt/kafka/bin/kafka-topics.sh \
--bootstrap-server kafka:9092 \
--create \
--topic user.message.topic \
--partitions 1 \
--replication-factor 1