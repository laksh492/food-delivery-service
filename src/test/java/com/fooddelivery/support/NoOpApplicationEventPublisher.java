package com.fooddelivery.support;

import org.springframework.context.ApplicationEventPublisher;

public final class NoOpApplicationEventPublisher implements ApplicationEventPublisher {

    @Override
    public void publishEvent(Object event) {
        // no-op for tests
    }
}
