## Kafka



### 常用命令

bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties

kafka-topics --create --bootstrap-server 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092 --replication-factor 2 --partitions 3 --topic topic-nginx-log
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test

bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
kafka-console-producer --broker-list 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092 --topic topic-test

bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
kafka-console-consumer --bootstrap-server 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092 --topic topic-test

bin/kafka-topics.sh --list --bootstrap-server 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092
kafka-topics --list --bootstrap-server 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092

kafka-topics --delete --bootstrap-server 192.159.60.135:9092,192.159.60.126:9092,192.159.60.162:9092 --topic 

