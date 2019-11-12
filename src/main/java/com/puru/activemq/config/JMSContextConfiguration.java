package com.puru.activemq.config;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.puru.activemq.service.JmsErrorHandler;
import com.puru.activemq.service.QueueMessageListener;
import com.puru.activemq.service.TopicMessageListener;

/**
 * @author rayp
 */

@EnableJms
public class JMSContextConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(JMSContextConfiguration.class);

    private static final String NON_HUMAN_RUNTIME_USER_PREFIX = "user";

    private String topicName = "Message.notificationTopic";

    private String runTimeUserName = System.getProperty("user.name");

    @Inject
    private TopicMessageListener topicMessageListener;
    
    @Inject
    private QueueMessageListener queueMessageListener;
    
    @Inject
    private JmsErrorHandler jmsErrorHandler;

    @Bean
    public Destination messageActiveMQDestinationTopic() {
        Destination destination = new ActiveMQTopic(topicName);
        return destination;
    }

    @Bean
    public ActiveMQConnectionFactory messageActiveMQConnectionFactory() {
        String brokerUrl = System.getProperty("activemq.jms.url");
        LOGGER.info("Connecting to JMS Instance at : " + brokerUrl);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        // Bless all packages as trusted
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public PooledConnectionFactory messagePooledConnectionFactory(
            ActiveMQConnectionFactory messageActiveMQConnectionFactory) {
        PooledConnectionFactory poolFactory = new PooledConnectionFactory();
        poolFactory.setConnectionFactory(messageActiveMQConnectionFactory);
        poolFactory.setMaxConnections(5);
        return poolFactory;
    }

    @Bean("activeMQTopicJmsTemplate")
    public JmsTemplate topicNotificationTemplate(PooledConnectionFactory messagePooledConnectionFactory,
            Destination messageActiveMQDestinationTopic) {
        JmsTemplate jmsTem = new JmsTemplate(messagePooledConnectionFactory);
        jmsTem.setPubSubDomain(true); // topic not queue.
        jmsTem.setDefaultDestination(messageActiveMQDestinationTopic);

        return jmsTem;
    }

    @Bean("activeMQQueueJmsTemplate")
    public JmsTemplate queueNotificationTemplate(PooledConnectionFactory messagePooledConnectionFactory) {
        JmsTemplate jmsTem = new JmsTemplate(messagePooledConnectionFactory);
        jmsTem.setDefaultDestination(activeMqQueue());
        return jmsTem;
    }

    @Bean
    public DefaultMessageListenerContainer topicDefaultMessageListenerContainer(
            ActiveMQConnectionFactory messageActiveMQConnectionFactory, Destination messageActiveMQDestinationTopic) {

        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(messageActiveMQConnectionFactory);
        listenerContainer.setPubSubDomain(true); // topic not queue.
        listenerContainer.setDestination(messageActiveMQDestinationTopic); // Listen message.

        boolean durableModeListener = false;
        if (runTimeUserName.contains(NON_HUMAN_RUNTIME_USER_PREFIX)) {
            // Dont make durable connection to dev queue if runtime user is a dev/qa user
            durableModeListener = true;

        }
        LOGGER.info("Default Pub/Sub Topic Listener is in : DurableMode : " + durableModeListener + " RuntimeUser : "
                + runTimeUserName);

        listenerContainer.setConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.concurrent.consumers")));
        listenerContainer.setMaxConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.max.concurrent.consumers")));
        listenerContainer.setSubscriptionDurable(durableModeListener);
        listenerContainer.setSubscriptionName(String.format("%s_%s_Self_Listener", runTimeUserName, topicName));
        listenerContainer.setClientId(String.format("%s_%s_Client_ID", runTimeUserName, topicName));
        listenerContainer.setMessageListener(topicMessageListener);
        listenerContainer.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);//default is AUTO_ACKNOWLEDGE
        listenerContainer.setAutoStartup(false);
        listenerContainer.setErrorHandler(jmsErrorHandler);
        return listenerContainer;
    }

    @Bean
    public DefaultMessageListenerContainer queueDefaultMessageListenerContainer(
            ActiveMQConnectionFactory messageActiveMQConnectionFactory) {

        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(messageActiveMQConnectionFactory);
        listenerContainer.setDestination(activeMqQueue()); // Listen message.
        listenerContainer.setConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.concurrent.consumers")));
        listenerContainer.setMaxConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.max.concurrent.consumers")));
        listenerContainer.setMessageListener(queueMessageListener);
        listenerContainer.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        listenerContainer.setAutoStartup(false);
        listenerContainer.setErrorHandler(jmsErrorHandler);
        return listenerContainer;
    }

    @Bean("activeMqQueue")
    public Queue activeMqQueue() {
        // broker.queue : Message.ActiveMQDataQueue?consumer.prefetchSize=10
        String queueName = System.getProperty("broker.queue");
        return new ActiveMQQueue(queueName);
    }

}
