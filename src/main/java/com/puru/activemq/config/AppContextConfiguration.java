package com.puru.activemq.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author rayp
 */

@Configuration
@Import({ JMSContextConfiguration.class})
@ComponentScan(basePackages = "com.puru.activemq")
@EnableMBeanExport
@EnableAspectJAutoProxy
@EnableScheduling
public class AppContextConfiguration {

}
