package com.zanthan.logback;

/**
 * Used in the configuration of MongoDBAppender.
 *
 * @author amoffat Alex Moffat
 */
public class MongoServerAddress {

    /**
     * Address to connect to.
     */
    private String address = null;

    /**
     * Port to connect to.
     */
    private int port = 0;

    /**
     * Used in configuration.
     *
     * @param address The address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    String getAddress() {
        return address;
    }

    /**
     * Used in configuration.
     *
     * @param port The port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    int getPort() {
        return port;
    }

    /**
     * True if address and port have been set.
     *
     * @return True or false.
     */
    boolean isValid() {
        return address != null && port > 0;
    }
}
