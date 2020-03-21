//package com.lahuan.common.queue;
//
//
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Properties;
//
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//
//public class SearchEngineKafkaQueue implements SearchEngineQueue{
//	String topic;
//	String kafkaUrl;
//	int s=10;
//	KafkaProducer<String, String> kafkaProducer;
//	KafkaConsumer<String, String> kafkaConsumer;
//	
//	public SearchEngineKafkaQueue(String topic, String kafkaUrl) {
//		super();
//		this.topic = topic;
//		this.kafkaUrl = kafkaUrl;
//		initConsumer();
//		initProducer();
//	}
//
//	public void init() {
//		initProducer();
//		initConsumer();
//	}
//
//	public void close() {
//		kafkaProducer.close();
//		kafkaProducer.close();
//	}
//
//	private void initConsumer() {
//		Properties props = new Properties();
//		// 定义kakfa 服务的地址，不需要将所有broker指定上
//		props.put("bootstrap.servers", kafkaUrl);
//		// 制定consumer group
//		props.put("group.id", "search_engine_demo");
//		// 是否自动确认offset
//		props.put("enable.auto.commit", "true");
//		// 自动确认offset的时间间隔
//		props.put("auto.commit.interval.ms", "1000");
//		// key的序列化类
//		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//		// value的序列化类
//		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//		// 定义consumer
//		kafkaConsumer = new KafkaConsumer<>(props);
//
//		kafkaConsumer.subscribe(Arrays.asList(topic));
//	}
//
//	private void initProducer() {
//		Properties props = new Properties();
//		// Kafka服务端的主机名和端口号
//		props.put("bootstrap.servers", kafkaUrl);
//		// 等待所有副本节点的应答
//		props.put("acks", "all");
//		// 消息发送最大尝试次数
//		props.put("retries", 0);
//		// 一批消息处理大小
//		props.put("batch.size", 16384);
//		// 增加服务端请求延时
//		props.put("linger.ms", 1);
//		// 发送缓存区内存大小
//		props.put("buffer.memory", 33554432);
//		// key序列化
//		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//		// value序列化
//		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//
//		kafkaProducer = new KafkaProducer<>(props);
//	}
//
//	@Override
//	public void send(String url) {
//		kafkaProducer.send(new ProducerRecord<String, String>(topic, url));
//	}
//	@Override
//	public List<String> get() {
//		ConsumerRecords<String, String> records = kafkaConsumer.poll(s);
//		List<String> res = new LinkedList<String>();
//		for (ConsumerRecord<String, String> record : records) {
//			// System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(),
//			// record.key(), record.value());
//			res.add(record.value());
//		}
//		return res;
//	}
//	
//	
//	
//	
//}
