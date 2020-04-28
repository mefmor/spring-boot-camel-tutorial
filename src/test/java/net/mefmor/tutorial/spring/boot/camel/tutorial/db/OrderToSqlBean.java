package net.mefmor.tutorial.spring.boot.camel.tutorial.db;

import org.apache.camel.Headers;
import org.apache.camel.language.xpath.XPath;

import java.util.Map;

public class OrderToSqlBean {

    public String toSql(@XPath("order/@name") String name,
                        @XPath("order/@amount") int amount,
                        @XPath("order/@customer") String customer,
                        @Headers Map<String, Object> outHeaders) {
        outHeaders.put("partName", name);
        outHeaders.put("quantity", amount);
        outHeaders.put("customer", customer);
        return "insert into incoming_orders (part_name, quantity, customer) values (:?partName, :?quantity, :?customer)";
    }
}
