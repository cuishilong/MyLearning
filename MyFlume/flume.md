## Flume

http://flume.apache.org/releases/content/1.9.0/FlumeUserGuide.html


### 一、工程配置

```shell script
bin/flume-ng agent --conf conf --conf-file example.conf --name a1 -Dflume.root.logger=INFO,console
```

#### 1、Source

Flume收集数据的起始源数据

- a、Avro Source

```shell script
a1.sources = r1
a1.channels = c1

a1.sources.r1.type = avro
a1.sources.r1.channels = c1
a1.sources.r1.bind = 0.0.0.0
a1.sources.r1.port = 4141

# 使用最大线程数
a1.sources.r1.threads = 3
```

- b、Spooling Directory Source

```shell script
a1.channels = ch-1
a1.sources = src-1

a1.sources.src-1.type = spooldir
a1.sources.src-1.channels = ch-1
a1.sources.src-1.spoolDir = /var/log/apache/flumeSpool

a1.sources.src-1.fileSuffix = .COMPLETED
a1.sources.src-1.batchSize = 100
a1.sources.src-1.recursiveDirectorySearch = false
```

- c、Kafka Source

```shell script
a1.sources.source1.type = org.apache.flume.source.kafka.KafkaSource                                                                   
a1.sources.source1.channels = channel1
a1.sources.source1.kafka.bootstrap.servers = localhost:9092
# 主题列表
a1.sources.source1.kafka.topics = test1, test2
# 正则匹配主题
a1.sources.source1.kafka.topics.regex = ^topic[0-9]$
a1.sources.source1.kafka.consumer.group.id = custom.g.id

a1.sources.source1.batchSize = 5000
a1.sources.source1.batchDurationMillis = 2000
```

### 2、Channel

- a、Memory Channel

```shell script
a1.channels = c1

a1.channels.c1.type = memory
# channel缓存的最大事件数据量
a1.channels.c1.capacity = 10000
# channel缓存的最大字节数据量
a1.channels.c1.byteCapacity = 800000
# 从source拉取或者推送到sink，每次处理的事件数据量，即拉取/推送的batch大小
a1.channels.c1.transactionCapacity = 100
# 预警比例，考虑的是最大缓存字节数据量byteCapacity
a1.channels.c1.byteCapacityBufferPercentage = 20
```

- b、File Channel

```shell script
a1.channels = c1

a1.channels.c1.type = file
a1.channels.c1.checkpointDir = /mnt/flume/checkpoint
a1.channels.c1.dataDirs = /mnt/flume/data
```

- c、Kafka Channel

```shell script
a1.channels.channel1.type = org.apache.flume.channel.kafka.KafkaChannel
a1.channels.channel1.kafka.bootstrap.servers = kafka-1:9092,kafka-2:9092,kafka-3:9092
a1.channels.channel1.kafka.topic = channel1
a1.channels.channel1.kafka.consumer.group.id = flume-consumer
```


### 3、Sink

- a、HDFS Sink
```shell script
a1.channels = c1
a1.sinks = k1

a1.sinks.k1.type = hdfs
a1.sinks.k1.channel = c1
a1.sinks.k1.hdfs.path = /flume/events/%y-%m-%d/%H%M/%S

a1.sinks.k1.hdfs.filePrefix = events-
# 基于时间的文件滚动，0表示忽略，单位秒
a1.sinks.k1.hdfs.rollInterval = true
# 基于文件size的文件滚动，0表示忽略，单位bytes
a1.sinks.k1.hdfs.rollSize = 10
# 基于事件数量的文件滚动，0表示忽略，单位条
a1.sinks.k1.hdfs.rollCount = minute
# 缓存量，达到缓存量则flush到hdfs中
a1.sinks.k1.hdfs.batchSize = 100
# gzip, bzip2, lzo, lzop, snappy
a1.sinks.k1.hdfs.codeC = -
# SequenceFile, DataStream or CompressedStream
a1.sinks.k1.hdfs.fileType = SequenceFile
# 允许打开的最大文件句柄数
a1.sinks.k1.hdfs.maxOpenFiles = 100
# Text or Writable
a1.sinks.k1.hdfs.writeFormat = Writable
# 
a1.sinks.k1.hdfs.threadsPoolSize = 10
# 时间戳的问题
a1.sinks.k1.hdfs.round = 100
a1.sinks.k1.hdfs.roundValue = 100
a1.sinks.k1.hdfs.roundUnit = 100
```

- b、Avro Sink

```shell script
a1.channels = c1
a1.sinks = k1

a1.sinks.k1.type = avro
a1.sinks.k1.channel = c1
a1.sinks.k1.hostname = 10.10.10.10
a1.sinks.k1.port = 4545
```

- c、File Roll Sink
```shell script
a1.channels = c1
a1.sinks = k1

a1.sinks.k1.type = file_roll
a1.sinks.k1.channel = c1
a1.sinks.k1.sink.directory = /var/log/flume
# 基于时间的文件滚动，0表示忽略，单位秒
a1.sinks.k1.sink.rollInterval = 30
# 基于时间数量的文件滚动
a1.sinks.k1.sink.batchSize = 100
```

- d、Kafka Sink

```shell script
a1.sinks.k1.channel = c1

a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.kafka.topic = mytopic
a1.sinks.k1.kafka.bootstrap.servers = localhost:9092

# 每个batch事件数
a1.sinks.k1.kafka.flumeBatchSize = 100
# 0 (Never wait for acknowledgement), 1 (wait for leader only), -1 (wait for all replicas)
a1.sinks.k1.kafka.producer.acks = 1
# 配置默认kafka分区
a1.sinks.k1.kafka.defaultPartitionId = -
```

#### 4、Channel Selector

channel选择器

- a、Replicating

source把数据副本形式发送给多个channel（每个channel都全部接收到source的所有数据）

```shell script
a1.sources = r1
a1.channels = c1 c2 c3
a1.sources.r1.selector.type = replicating
# 多channel副本数据（每个channel都全部接收source的数据）
a1.sources.r1.channels = c1 c2 c3
a1.sources.r1.selector.optional = c3
```

- b、Mutiplexing

source把数据分发形式发送给多个channel（每个channel接收到source的一部分数据，所有channel数据的加和量等于source发送的数据总量）

```shell script
a1.sources = r1
a1.channels = c1 c2 c3 c4
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = state
a1.sources.r1.selector.mapping.CZ = c1
a1.sources.r1.selector.mapping.US = c2 c3
a1.sources.r1.selector.default = c4
```

#### 5、Sink Processors

sink处理器
可选项：default,failover,load_balance

- a、Default Sink Processor
单个sink对单个channel
无需任何配置

- b、Failover Sink Processor

sink设置优先级

````shell script
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = failover
a1.sinkgroups.g1.processor.priority.k1 = 5
a1.sinkgroups.g1.processor.priority.k2 = 10
a1.sinkgroups.g1.processor.maxpenalty = 10000
````

- c、Load balancing Sink Processor

```shell script
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.backoff = true
# round_robin, random
a1.sinkgroups.g1.processor.selector = random
```

#### 6、Event Serializers

事件序列化器

- a、Text Serializer

```shell script
a1.sinks = k1
a1.sinks.k1.type = file_roll
a1.sinks.k1.channel = c1
a1.sinks.k1.sink.directory = /var/log/flume
a1.sinks.k1.sink.serializer = text
a1.sinks.k1.sink.serializer.appendNewline = false
```

- b、"Flume Event" Avro Event Serializer

```shell script
a1.sinks.k1.type = hdfs
a1.sinks.k1.channel = c1
a1.sinks.k1.hdfs.path = /flume/events/%y-%m-%d/%H%M/%S
a1.sinks.k1.serializer = avro_event
a1.sinks.k1.serializer.compressionCodec = snappy
```

#### 7、Interceptors

事件拦截器
Flume has the capability to modify/drop events in-flight.

- a、UUID Interceptor

````shell script
a1.sources.r1.interceptors.i1.type = org.apache.flume.sink.solr.morphline.UUIDInterceptor$Builder
````

- b、Regex Filtering Interceptor

````shell script
a1.sources.r1.interceptors.i1.type = regex_filter
````


