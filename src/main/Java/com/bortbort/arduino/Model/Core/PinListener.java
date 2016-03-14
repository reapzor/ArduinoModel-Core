package com.bortbort.arduino.Model.Core;

import net.jodah.typetools.TypeResolver;

/**
 * Created by chuck on 3/6/2016.
 */
public abstract class PinListener<T extends PinEvent> {
    Integer pinIdentifier = null;
    Class<? extends PinEvent> eventType;

    @SuppressWarnings("unchecked")
    public PinListener() {
        Class[] typeArguments = TypeResolver.resolveRawArguments(PinListener.class, getClass());
        eventType = typeArguments[0];
    }

    PinListener(Integer pinIdentifier) {
        this();
        this.pinIdentifier = pinIdentifier;
    }

    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

    public Class<? extends PinEvent> getEventType() {
        return eventType;
    }


    abstract void eventReceived(T pinEvent);
}
