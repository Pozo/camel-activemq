package com.chemaxon.logging.remote;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServerBroker {
    private static final Logger logger = LoggerFactory.getLogger(ServerBroker.class);

    private static final int SLEEP = 3000;
    private static boolean listening = true;

    public static void main(String[] args) {
        new Thread(new Runnable() {
            public void run() {
                ConfigurableApplicationContext appContext = null;

                try {
                    appContext = new ClassPathXmlApplicationContext("applicationContext.xml");

                    while (listening) {
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e) {
                            logger.error(e.getMessage());
                        }
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage());
                } finally {
                    try {
                        if (appContext != null) {
                            appContext.close();
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

            }
        }, "CamelContext thread").start();

    }
}
