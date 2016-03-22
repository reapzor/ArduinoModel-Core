package com.bortbort.arduino.Model.Core.PinEvents;

import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.Pin;
import com.bortbort.arduino.Model.Core.PinEvent;

/**
 * Created by chuck on 3/21/2016.
 */
public class StateEvent extends PinEvent<Pin> {
    PinCapability previousState;
    PinCapability newState;

    public StateEvent(PinCapability previousState, PinCapability newState) {
        this.previousState = previousState;
        this.newState = newState;
    }

    public PinCapability getPreviousState() {
        return previousState;
    }

    public PinCapability getNewState() {
        return newState;
    }
}
