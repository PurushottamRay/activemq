package com.puru.activemq.service;

import javax.inject.Named;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author rayp
 */
@Named("puruListenerService")
public class PuruListenerService implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PuruListenerService.class);
    
    public void onMessage(Message message) {
        // Check if message received is of type text message
        if (!(message instanceof ObjectMessage)) {
            LOGGER.error("Message received is not of type 'Object message': " + message);
            return;
        }
        try {
            //ActualMessageType notification = (ActualMessageType) ((ObjectMessage) message).getObject();

        } catch (Exception e) {
            LOGGER.error("Exception occured while processing notification message ", e);
        }
    }
}
