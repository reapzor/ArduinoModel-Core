package com.bortbort.arduino.Model.Core;

import net.jodah.typetools.TypeResolver;

import java.util.function.Consumer;

/**
 * Created by chuck on 3/20/2016.
 */
class ConsumerPinEventListener<K extends PinEvent> extends PinEventListener<K> {
    Consumer<K> consumer;

    @SuppressWarnings("unchecked")
    public ConsumerPinEventListener(Consumer<K> consumer) {
        Class[] typeArguments = TypeResolver.resolveRawArguments(Consumer.class, consumer.getClass());
        eventType = typeArguments[0];
        this.consumer = consumer;
    }

    public ConsumerPinEventListener(Integer pinIdentifier, Consumer<K> consumer) {
        this(consumer);
        this.pinIdentifier = pinIdentifier;
    }

    @Override
    public void eventReceived(K pinEvent) {
        consumer.accept(pinEvent);
    }
}
