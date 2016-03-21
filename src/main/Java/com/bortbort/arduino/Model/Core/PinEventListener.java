package com.bortbort.arduino.Model.Core;

import net.jodah.typetools.TypeResolver;

import java.util.function.Consumer;

/**
 * Created by chuck on 3/6/2016.
 */
public abstract class PinEventListener<T extends PinEvent> {
    Integer pinIdentifier = null;
    Class<? extends PinEvent> eventType;

    @SuppressWarnings("unchecked")
    public PinEventListener() {
        Class[] typeArguments = TypeResolver.resolveRawArguments(PinEventListener.class, getClass());
        eventType = typeArguments[0];
    }

    public PinEventListener(Integer pinIdentifier) {
        this();
        this.pinIdentifier = pinIdentifier;
    }


    public static <K extends PinEvent> PinEventListener<K> from(Consumer<K> consumer) {
        return new ConsumerPinEventListener<>(consumer);
    }

    public static <K extends PinEvent> PinEventListener<K> from(Integer pinIdentifier, Consumer<K> consumer) {
        return new ConsumerPinEventListener<>(pinIdentifier, consumer);
    }


    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

    public Class<? extends PinEvent> getEventType() {
        return eventType;
    }



    public abstract void eventReceived(T pinEvent);
}
