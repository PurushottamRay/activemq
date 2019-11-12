package com.puru.activemq.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 * @author rayp
 */

@Service
public class JmsErrorHandler implements ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmsErrorHandler.class);

    public void handleError(Throwable t) {
        LOGGER.warn("In default jms error handler...");
        LOGGER.error("Error Message : {}", t.getMessage());
    }

}
