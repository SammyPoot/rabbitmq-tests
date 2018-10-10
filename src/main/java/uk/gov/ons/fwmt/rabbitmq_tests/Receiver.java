package uk.gov.ons.fwmt.rabbitmq_tests;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Receiver {
    int count = 0;
    public void receiveMessage(byte[] message) {
        count++;
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {}
        System.out.println("1: " + new String(message) + " count=" + count);
    }
    public void receiveMessage2(byte[] message) {
        count++;
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {}
        System.out.println("2: " + new String(message) + " count=" + count);
    }
}
