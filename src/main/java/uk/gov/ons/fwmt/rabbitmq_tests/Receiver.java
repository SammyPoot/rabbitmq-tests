package uk.gov.ons.fwmt.rabbitmq_tests;

public class Receiver {
    public void receive(String message) {
        System.out.println(message);
    }
}
