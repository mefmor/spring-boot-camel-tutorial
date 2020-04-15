package net.mefmor.tutorial.spring.boot.camel.tutorial.notify;

public class OrderService {

    public boolean validateOrder(String order) {
        return order.startsWith("123");
    }

    public String processOrder(String order) {
        if (order.endsWith("9999")) {
            throw new IllegalArgumentException("Invalid order");
        }

        return "OK," + order;
    }
}
