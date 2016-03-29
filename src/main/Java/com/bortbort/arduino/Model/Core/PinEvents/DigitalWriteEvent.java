package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.Model.Core.PinEvent;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalOutputPin;

/**
 * Created by chuck on 3/28/2016.
 */
public class DigitalWriteEvent extends PinEvent<DigitalOutputPin> {
    private Integer previousIntegerValue;
    private DigitalPinValue previousValue;
    private Integer currentIntegerValue;
    private DigitalPinValue currentValue;

    public DigitalWriteEvent(Integer previousIntegerValue, DigitalPinValue previousValue, Integer currentIntegerValue, DigitalPinValue currentValue) {
        this.previousIntegerValue = previousIntegerValue;
        this.previousValue = previousValue;
        this.currentIntegerValue = currentIntegerValue;
        this.currentValue = currentValue;
    }

    public Integer getPreviousIntegerValue() {
        return previousIntegerValue;
    }

    public DigitalPinValue getPreviousValue() {
        return previousValue;
    }

    public Integer getCurrentIntegerValue() {
        return currentIntegerValue;
    }

    public DigitalPinValue getCurrentValue() {
        return currentValue;
    }
}
