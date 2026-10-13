package org.example.erp.platform;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** In-process delivery: listeners run synchronously inside the publisher's transaction. */
@Component
class SpringIntegrationEventPublisher implements IntegrationEventPublisher {
    private final ApplicationEventPublisher delegate;

    SpringIntegrationEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void publish(IntegrationEvent event) {
        delegate.publishEvent(event);
    }
}
