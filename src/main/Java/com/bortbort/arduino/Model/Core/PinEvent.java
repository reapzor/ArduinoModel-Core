package com.bortbort.arduino.Model.Core;


import net.jodah.typetools.TypeResolver;

/**
 * Created by chuck on 3/6/2016.
 */
public abstract class PinEvent<T extends Pin> {
    private Class<T> pinType;
    private T pin;
    private Integer pinIdentifier;

    @SuppressWarnings("unchecked")
    public PinEvent() {
        Class[] typeArguments = TypeResolver.resolveRawArguments(PinEvent.class, getClass());
        this.pinType = typeArguments[0];
    }

    public T getPin() {
        return pin;
    }

    protected void setPin(T pin) {
        this.pin = pin;
        pinIdentifier = pin.getId();
    }

    protected Class<T> getPinType() {
        return pinType;
    }

    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

}
