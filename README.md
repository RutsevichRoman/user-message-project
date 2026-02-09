1. Пример создания топика: 
docker exec -it kafka_broker /opt/kafka/bin/kafka-topics.sh \
   --bootstrap-server kafka:9092 \
   --create \
   --topic user.message.topic \
   --partitions 1 \
   --replication-factor 1 
   --config cleanup.policy=delete
2. Пример добавление для топика параметра, например, cleanup.policy=delete:
   docker exec -it kafka_broker /opt/kafka/bin/kafka-configs.sh \
   --bootstrap-server kafka:9092 \
   --entity-type topics \
   --entity-name user.message.topic \
   --alter \
   --add-config cleanup.policy=delete

3. Run from root dir user-message-project: 
   docker compose up -d --build
4. Stop:
   docker compose down -v

Notes:
- Один проект consumer-user-messages-1 для 2ух consumer с разными group id для одного файл
application-consumer1.yml, для второго application-consumer2.yml
- Добавлен constraint в энтити для обеспечения идемпотентности
  columnNames = {"topic", "partition_id", "offset_id"}
Возможно можно было бы проверять просто запросом по наличию message_key так это поле есть id в UserMessage
- Наверное если добавлять новые консьюмеры в группу нужно делать распределённый кэш через Redis