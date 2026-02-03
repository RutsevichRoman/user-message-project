1. Run from root dir user-message-project: 
   docker compose up -d --build
2. Stop:
   docker compose down -v

Notes:
- Один проект consumer-user-messages-1 для 2ух consumer с разными group id для одного файл
application-consumer1.yml, для второго application-consumer2.yml
- Добавлен constraint в энтити для обеспечения идемпотентности
  columnNames = {"topic", "partition_id", "offset_id"}
Возможно можно было бы проверять просто запросом по наличию message_key так это поле есть id в UserMessage
- Наверное если добавлять новые консьюмеры в группу нужно делать распределённый кэш через Redis