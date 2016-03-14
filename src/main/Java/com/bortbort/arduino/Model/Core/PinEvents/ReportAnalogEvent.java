package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.Model.Core.PinEvent;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;

/**
 * Created by chuck on 3/14/2016.
 */
public class ReportAnalogEvent extends PinEvent<AnalogPin> {
    private Boolean reporting;

    public ReportAnalogEvent(AnalogPin pin, Boolean reporting) {
        super(pin);
        this.reporting = reporting;
    }

    public Boolean getReporting() {
        return reporting;
    }
}
