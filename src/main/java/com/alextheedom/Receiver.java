package com.alextheedom;

import java.util.concurrent.CountDownLatch;

/**
 * Created by atheedom on 21/12/2015.
 */
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}
