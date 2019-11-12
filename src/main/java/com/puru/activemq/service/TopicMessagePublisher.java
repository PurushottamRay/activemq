package com.puru.activemq.service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.puru.activemq.model.TopicMessage;

/**
 * @author rayp
 */
@Named("topicMessagePublisher")
public class TopicMessagePublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicMessagePublisher.class);

    @Inject
    private JmsTemplate activeMQTopicJmsTemplate;

    public void publishTopicMessage(TopicMessage message) {
        LOGGER.info("Calling publishTopicMessage for message:{}",message);
        activeMQTopicJmsTemplate.send(activeMQTopicJmsTemplate.getDefaultDestination(),
                new TopicMessageCreator(message));
    }

    /**
     * Message Creator
     **/
    private static final class TopicMessageCreator implements MessageCreator {

        private final TopicMessage message;

        private TopicMessageCreator(TopicMessage message) {
            this.message = message;
        }

        /**
         * {@inheritDoc}
         */
        public Message createMessage(Session session) throws JMSException {
            return session.createObjectMessage(message);
        }
    }
}
