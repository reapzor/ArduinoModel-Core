package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.Model.Core.PinEvent;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalInputPin;

/**
 * Created by chuck on 3/28/2016.
 */
public class ReportDigitalEvent extends PinEvent<DigitalInputPin> {
    private Boolean reporting;

    public ReportDigitalEvent(Boolean reporting) {
        this.reporting = reporting;
    }

    public Boolean getReporting() {
        return reporting;
    }
}
