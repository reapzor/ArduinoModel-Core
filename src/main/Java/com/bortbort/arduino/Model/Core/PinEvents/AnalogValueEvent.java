package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.Model.Core.PinEvent;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;

/**
 * Created by chuck on 3/14/2016.
 */
public class AnalogValueEvent extends PinEvent<AnalogPin> {
    private Integer currentValueInt;
    private Byte currentValueByte;

    public AnalogValueEvent(Integer currentValueInt, Byte currentValueByte) {
        this.currentValueInt = currentValueInt;
        this.currentValueByte = currentValueByte;
    }

    public Integer getCurrentValueInt() {
        return currentValueInt;
    }

    public Byte getCurrentValueByte() {
        return currentValueByte;
    }

}
