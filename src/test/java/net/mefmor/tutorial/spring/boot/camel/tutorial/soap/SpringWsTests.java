package net.mefmor.tutorial.spring.boot.camel.tutorial.soap;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.ws.SpringWebserviceConstants;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.example.Add;
import org.example.ObjectFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SpringWsTests extends CamelTestSupport {
    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:xml")
                        .marshal().jaxb(Add.class.getPackage().getName())
                        .setHeader(SpringWebserviceConstants.SPRING_WS_SOAP_ACTION, simple("http://Example.org/ICalculator/Add"))
                        .to("spring-ws:http://127.0.0.1:8080/ICalculator");
            }
        };
    }

    @Test
    @Disabled("For using with SOAP UI")
    void testSendMessageAdd() {
        Add add = new ObjectFactory().createAdd();
        add.setA(2);
        add.setB(3);

        template.sendBody("direct:xml", add);

    }
}
