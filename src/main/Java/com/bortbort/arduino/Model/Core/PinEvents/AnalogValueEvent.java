package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.Model.Core.PinEvent;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;

/**
 * Created by chuck on 3/14/2016.
 */
public class AnalogValueEvent extends PinEvent<AnalogPin> {
    private Integer previousValueInt;
    private Byte previousValueByte;
    private Integer currentValueInt;
    private Byte currentValueByte;

    public AnalogValueEvent(Integer previousValueInt, Byte previousValueByte,
                            Integer currentValueInt, Byte currentValueByte) {
        this.previousValueInt = previousValueInt;
        this.previousValueByte = previousValueByte;
        this.currentValueInt = currentValueInt;
        this.currentValueByte = currentValueByte;
    }

    public Integer getPreviousValueInt() {
        return previousValueInt;
    }

    public Byte getPreviousValueByte() {
        return previousValueByte;
    }

    public Integer getCurrentValueInt() {
        return currentValueInt;
    }

    public Byte getCurrentValueByte() {
        return currentValueByte;
    }

}
