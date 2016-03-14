package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.Model.Core.Pin;

/**
 * Created by chuck on 3/6/2016.
 */
public abstract class PinEvent<T extends Pin> {
    private T pin;
    private Integer pinIdentifier;

    public PinEvent(T pin) {
        this.pin = pin;
        pinIdentifier = pin.getId();
    }

    public T getPin() {
        return pin;
    }

    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

}
