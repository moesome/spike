package com.moesome.spike.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {
	public static final String QUEUE = "queue";
	public static final String TOPIC_QUEUE1 = "topic.queue1";
	public static final String TOPIC_QUEUE2 = "topic.queue2";
	public static final String HEADER_QUEUE = "header.queue";
	public static final String TOPIC_EXCHANGE = "topicExchange";
	public static final String FANOUT_EXCHANGE = "fanoutExchange";
	public static final String HEADS_EXCHANGE = "headsExchange";



	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * direct 模式交换机，将接受到的请求直接发送到队列
	 */
	@Bean
	public Queue queue(){
		return new Queue("queue",true);
	}

	/**
	 * 以下为 topic 模式交换机示例
	 * 将根据发送值时附带的 topic 进行分发
	 */
	@Bean
	public Queue topicQueue1(){
		return new Queue(TOPIC_QUEUE1,true);
	}

	@Bean
	public Queue topicQueue2(){
		return new Queue(TOPIC_QUEUE2,true);

	}
	// 构造交换机
	@Bean
	public TopicExchange topicExchange(){
		return new TopicExchange(TOPIC_EXCHANGE);
	}

	// 根据返回值来确定该类是绑定类
	@Bean
	public Binding topicBinding1(){
		return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
	}
	@Bean
	public Binding topicBinding2(){
		return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
	}


	/**
	 *  以下为 Fanout 交换机
	 *  广播模式，广播到所有已绑定的队列
	 */

	@Bean
	public FanoutExchange fanoutExchange(){
		return new FanoutExchange(FANOUT_EXCHANGE);
	}

	@Bean
	public Binding fanoutBingding1(){
		return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
	}

	@Bean
	public Binding fanoutBingding2(){
		return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
	}

	/**
	 * Headers 交换机
	 */

	@Bean
	public HeadersExchange headersExchange(){
		return new HeadersExchange(HEADS_EXCHANGE);
	}

	@Bean
	public Queue headerQueue(){
		return new Queue(HEADER_QUEUE);
	}

	@Bean
	public Binding headerBinding(){
		Map<String,Object> map = new HashMap<>();
		map.put("header1","value1");
		map.put("header2","value2");
		return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
	}

}
