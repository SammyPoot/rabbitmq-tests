package uk.gov.ons.fwmt.rabbitmq_tests;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;
import uk.gov.ons.fwmt.fwmtgatewaycommon.config.QueueNames;
import uk.gov.ons.fwmt.fwmtgatewaycommon.retry.CTPRetryPolicy;
import uk.gov.ons.fwmt.fwmtgatewaycommon.retry.CustomMessageRecover;

import static uk.gov.ons.fwmt.fwmtgatewaycommon.config.QueueNames.ADAPTER_JOB_SVC_DLQ;
import static uk.gov.ons.fwmt.fwmtgatewaycommon.config.QueueNames.JOB_SVC_ADAPTER_DLQ;

@Configuration
public class QueueConfig {

    @Bean
    public Queue adapterDeadLetterQueue() {
        return QueueBuilder.durable(ADAPTER_JOB_SVC_DLQ).build();
    }

    @Bean
    public Queue jobSvsDeadLetterQueue() {
        return QueueBuilder.durable(JOB_SVC_ADAPTER_DLQ).build();
    }

    @Bean
    public TopicExchange adapterExchange() {
        return new TopicExchange(QueueNames.RM_JOB_SVC_EXCHANGE);
    }

    @Bean
    public Binding adapterBinding(TopicExchange exchange) {
        Queue adapterQueue = QueueBuilder.durable(QueueNames.JOBSVC_TO_ADAPTER_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", JOB_SVC_ADAPTER_DLQ)
                .build();
        return BindingBuilder.bind(adapterQueue).to(exchange).with(QueueNames.JOB_SVC_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding jobsvcBinding(TopicExchange exchange) {
        Queue jobsvcQueue = QueueBuilder.durable(QueueNames.ADAPTER_TO_JOBSVC_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ADAPTER_JOB_SVC_DLQ)
                .build();
        return BindingBuilder.bind(jobsvcQueue).to(exchange).with(QueueNames.JOB_SVC_REQUEST_ROUTING_KEY);
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                                    @Qualifier("listenerAdapter") MessageListenerAdapter listenerAdapter,
                                                    RetryOperationsInterceptor interceptor) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        Advice[] adviceChain = {interceptor};

        container.setAdviceChain(adviceChain);
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QueueNames.ADAPTER_TO_JOBSVC_QUEUE);
        container.setMessageListener(listenerAdapter);
//        container.setConcurrentConsumers(5);
        return container;
    }

    @Bean
    public SimpleMessageListenerContainer container2(ConnectionFactory connectionFactory,
                                                    @Qualifier("listenerAdapter2") MessageListenerAdapter listenerAdapter,
                                                    RetryOperationsInterceptor interceptor) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

        Advice[] adviceChain = {interceptor};

        container.setAdviceChain(adviceChain);
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QueueNames.ADAPTER_TO_JOBSVC_QUEUE);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(Receiver receiver) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver, "receiveMessage");
        return listenerAdapter;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter2(Receiver receiver) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(receiver, "receiveMessage2");
        return listenerAdapter;
    }

    @Bean
    public RetryOperationsInterceptor interceptor(RetryOperations retryTemplate) {
        RetryOperationsInterceptor interceptor = new RetryOperationsInterceptor();
        interceptor.setRecoverer(new CustomMessageRecover());
        interceptor.setRetryOperations(retryTemplate);
        return interceptor;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(5000);
        backOffPolicy.setMultiplier(3.0);
        backOffPolicy.setMaxInterval(45000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        CTPRetryPolicy ctpRetryPolicy = new CTPRetryPolicy();
        retryTemplate.setRetryPolicy(ctpRetryPolicy);

//        retryTemplate.registerListener(new DefaultListenerSupport());

        return retryTemplate;
    }
}
