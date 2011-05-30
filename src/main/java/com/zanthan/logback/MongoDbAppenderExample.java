package com.zanthan.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple example program to test logging messages to MongoDB.
 *
 * @author amoffat Alex Moffat
 */
public class MongoDbAppenderExample {

    final static Logger logger =
            LoggerFactory.getLogger(MongoDbAppenderExample.class);

    public static void main(String[] args) {

        int iterationCount = Integer.parseInt(args[0]);
        long delay = Long.parseLong(args[1]);

        new MongoDbAppenderExample(iterationCount, delay).run();
    }

    private final int iterationCount;

    private final long delay;

    private MongoDbAppenderExample(int iterationCount, long delay) {
        this.iterationCount = iterationCount;
        this.delay = delay;
    }

    private void run() {
        for (int i = 0; i < iterationCount; i++) {
            if (logger.isDebugEnabled()) {
                logger.debug("Debug Message.");
            }

            logger.error("Error Message.");

            synchronized (this) {
                try {
                    wait(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
