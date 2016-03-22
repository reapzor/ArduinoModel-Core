package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.Model.Core.Pin;
import com.bortbort.arduino.Model.Core.PinEvent;

/**
 * Created by chuck on 3/21/2016.
 */
public class PullupValueEvent extends PinEvent<Pin> {
    Integer previousIntegerValue;
    Integer newIntegerValue;
    DigitalPinValue previousValue;
    DigitalPinValue newValue;

    public PullupValueEvent(Integer previousIntegerValue, Integer newIntegerValue, DigitalPinValue previousValue, DigitalPinValue newValue) {
        this.previousIntegerValue = previousIntegerValue;
        this.newIntegerValue = newIntegerValue;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    public Integer getPreviousIntegerValue() {
        return previousIntegerValue;
    }

    public Integer getNewIntegerValue() {
        return newIntegerValue;
    }

    public DigitalPinValue getPreviousValue() {
        return previousValue;
    }

    public DigitalPinValue getNewValue() {
        return newValue;
    }
}
