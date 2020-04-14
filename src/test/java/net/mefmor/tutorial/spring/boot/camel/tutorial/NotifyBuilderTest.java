package net.mefmor.tutorial.spring.boot.camel.tutorial;

import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.camel.test.junit5.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class NotifyBuilderTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("file://target/inbox").to("file://target/outbox");
            }
        };
    }

    @BeforeEach
    public void setUp() throws Exception {
        TestSupport.deleteDirectory("target/inbox");
        TestSupport.deleteDirectory("target/outbox");
        super.setUp();
    }

    @Test
    void testMoveFile() {
        NotifyBuilder notify = new NotifyBuilder(context).whenDone(1).create();

        template.sendBodyAndHeader("file://target/inbox", "Hello World",
                Exchange.FILE_NAME, "hello.txt");

        assertTrue(notify.matchesWaitTime());

        File target = new File("target/outbox/hello.txt");
        assertTrue(target.exists());

        String content = context.getTypeConverter().convertTo(String.class, target);
        assertEquals("Hello World", content);
    }
}
