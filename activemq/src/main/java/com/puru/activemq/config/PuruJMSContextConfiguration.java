package com.puru.activemq.config;

import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.Queue;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.puru.activemq.service.PuruListenerService;

/**
 * @author rayp
 */
public class PuruJMSContextConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PuruJMSContextConfiguration.class);

    private static final String NON_HUMAN_RUNTIME_USER_PREFIX = "puru";

    private String topicName = "Puru.notificationTopic";

    private String runTimeUserName = System.getProperty("user.name");

    @Inject
    private PuruListenerService puruListenerService;

    @Bean
    public Destination puruActiveMQDestinationTopic() {
        Destination destination = new ActiveMQTopic(topicName);
        return destination;
    }

    @Bean
    public ActiveMQConnectionFactory puruActiveMQConnectionFactory() {
        String brokerUrl = System.getProperty("activemq.jms.url");
        LOGGER.info("Connecting to JMS Instance at : " + brokerUrl);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        // Bless all packages as trusted
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean
    public PooledConnectionFactory puruPooledConnectionFactory(
            ActiveMQConnectionFactory puruActiveMQConnectionFactory) {
        PooledConnectionFactory poolFactory = new PooledConnectionFactory();
        poolFactory.setConnectionFactory(puruActiveMQConnectionFactory);
        poolFactory.setMaxConnections(5);

        return poolFactory;
    }

    @Bean("activeMQTopicJmsTempplate")
    public JmsTemplate puruTopicNotificationTemplate(PooledConnectionFactory puruPooledConnectionFactory,
            Destination puruActiveMQDestinationTopic) {
        JmsTemplate jmsTem = new JmsTemplate(puruPooledConnectionFactory);
        jmsTem.setPubSubDomain(true); // topic not queue.
        jmsTem.setDefaultDestination(puruActiveMQDestinationTopic);

        return jmsTem;
    }
    
    @Bean("activeMQQueueJmsTempplate")
    public JmsTemplate puruQueueNotificationTemplate(PooledConnectionFactory puruPooledConnectionFactory) {
        JmsTemplate jmsTem = new JmsTemplate(puruPooledConnectionFactory);
        jmsTem.setDefaultDestination(activeMqQueue());
        return jmsTem;
    }

    @Bean
    public DefaultMessageListenerContainer puruTopicDefaultMessageListenerContainer(
            ActiveMQConnectionFactory puruActiveMQConnectionFactory, Destination puruActiveMQDestinationTopic) {
        
        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(puruActiveMQConnectionFactory);
        listenerContainer.setPubSubDomain(true); // topic not queue.
        listenerContainer.setDestination(puruActiveMQDestinationTopic); // Listen message.

        boolean durableModeListener = false;
        if (runTimeUserName.contains(NON_HUMAN_RUNTIME_USER_PREFIX)) {
            // Dont make durable connection to dev queue if runtime user is not cocoa or cocoadev. True to CI also.
            durableModeListener = true;

        }
        LOGGER.info("Default Puru Pub/Sub Topic Listener is in : DurableMode : " + durableModeListener
                + " RuntimeUser : " + runTimeUserName);
        
        listenerContainer.setConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.concurrent.consumers")));
        listenerContainer.setSubscriptionDurable(durableModeListener);
        listenerContainer.setSubscriptionName(String.format("%s_%s_Self_Listener", runTimeUserName, topicName));
        listenerContainer.setClientId(String.format("%s_%s_Client_ID", runTimeUserName, topicName));
        listenerContainer.setMessageListener(puruListenerService);

        return listenerContainer;
    }
    
    @Bean
    public DefaultMessageListenerContainer puruQueueDefaultMessageListenerContainer(
            ActiveMQConnectionFactory puruActiveMQConnectionFactory) {
        
        DefaultMessageListenerContainer listenerContainer = new DefaultMessageListenerContainer();
        listenerContainer.setConnectionFactory(puruActiveMQConnectionFactory);
        //listenerContainer.setPubSubDomain(true); // topic not queue.
        listenerContainer.setDestination(activeMqQueue()); // Listen message.

        /*boolean durableModeListener = false;
        if (runTimeUserName.contains(NON_HUMAN_RUNTIME_USER_PREFIX)) {
            // Dont make durable connection to dev queue if runtime user is not cocoa or cocoadev. True to CI also.
            durableModeListener = true;

        }
        LOGGER.info("Default Puru Pub/Sub Topic Listener is in : DurableMode : " + durableModeListener
                + " RuntimeUser : " + runTimeUserName);*/
        
        listenerContainer.setConcurrentConsumers(Integer.valueOf(System.getProperty("activemq.concurrent.consumers")));
        //listenerContainer.setSubscriptionDurable(durableModeListener);
        //listenerContainer.setSubscriptionName(String.format("%s_%s_Self_Listener", runTimeUserName, topicName));
       // listenerContainer.setClientId(String.format("%s_%s_Client_ID", runTimeUserName, topicName));
        listenerContainer.setMessageListener(puruListenerService);

        return listenerContainer;
    }
    @Bean("activeMqQueue")
    public Queue activeMqQueue(){
        //broker.queue : Puru.ActiveMQDataQueue?consumer.prefetchSize=10
        String queueName = System.getProperty("broker.queue");
        return new ActiveMQQueue(queueName);
    }

}
