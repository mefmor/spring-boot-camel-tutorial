package net.mefmor.tutorial.spring.boot.camel.tutorial.notify;

import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.seda.SedaEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.apache.camel.builder.Builder.body;
import static org.assertj.core.api.Assertions.assertThat;

public class NotifyTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("seda:order")
                        .choice()
                        .when().method(OrderService.class, "validateOrder")
                        .bean(OrderService.class, "processOrder").to("seda:confirm")
                        .otherwise()
                        .to("seda:invalid")
                        .end();

                from("seda:quote")
                        .delay(2000)
                        .to("log:quote");
            }
        };
    }

    @Test
    void testNotifyFrom() {
        NotifyBuilder notify = new NotifyBuilder(context).from("seda:order").whenDone(1).create();

        template.sendBody("seda:quote", "Camel rocks");
        template.sendBody("seda:order", "123,2017-04-20'T'15:47:59,4444,5555");

        assertThat(notify.matches(5, TimeUnit.SECONDS)).isTrue();

        SedaEndpoint confirm = context.getEndpoint("seda:confirm", SedaEndpoint.class);
        assertThat(confirm.getExchanges().size()).isEqualTo(1);
        assertThat(confirm.getExchanges().get(0).getIn().getBody()).isEqualTo("OK,123,2017-04-20'T'15:47:59,4444,5555");
    }

    @Test
    void testNotifyWhenAnyDoneMatches() {
        NotifyBuilder notify = new NotifyBuilder(context)
                .from("seda:order").whenAnyDoneMatches(body().isEqualTo("OK,123,2017-04-20'T'15:48:00,2222,3333")).create();

        template.sendBody("seda:order", "123,2017-04-20'T'15:47:59,4444,5555");
        template.sendBody("seda:order", "123,2017-04-20'T'15:48:00,2222,3333");

        assertThat(notify.matches(5, TimeUnit.SECONDS)).isTrue();

        SedaEndpoint confirm = context.getEndpoint("seda:confirm", SedaEndpoint.class);

        assertThat(confirm.getExchanges().size()).isEqualTo(2);
        assertThat(confirm.getExchanges().get(1).getIn().getBody()).isEqualTo("OK,123,2017-04-20'T'15:48:00,2222,3333");
    }

    @Test
    void testNotifyOr() {
        // shows how to stack multiple expressions using binary operations (or)
        NotifyBuilder notify = new NotifyBuilder(context)
                .from("seda:quote").whenReceived(1).or().whenFailed(1).create();

        template.sendBody("seda:quote", "Camel rocks");
        template.sendBody("seda:order", "123,2017-04-20'T'15:48:00,2222,3333");

        assertThat(notify.matches(5, TimeUnit.SECONDS)).isTrue();
    }


}
