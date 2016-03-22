package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SetPinModeMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chuck on 3/5/2016.
 */
public abstract class MultiStatePin extends Pin {
    private static final Logger log = LoggerFactory.getLogger(MultiStatePin.class);
    protected List<PinCapability> supportedStates = null;

    protected MultiStatePin(Firmata firmata, PinEventManager eventManager, Integer id, PinCapability defaultState,
                            PinCapability... supportedStates) {
        super(firmata, eventManager, id, defaultState);
        this.supportedStates = Arrays.asList(supportedStates);
    }

    public Boolean enterState(PinCapability desiredState) {
        if (!supportedStates.contains(desiredState)) {
            log.error("Told pin {} to go into unsupported state {}!",
                    getPinIdentifier(), desiredState);
            return false;
        }

        if (firmata.sendMessage(new SetPinModeMessage(pinIdentifier, desiredState))) {
            return updateState();
        }

        log.error("Unable to transmit pin state change request from {} to {} for pin {}",
                state, desiredState, pinIdentifier);
        return false;
    }

    protected void setDefaultState(PinCapability defaultState) {
        if (!supportsState(defaultState)) {
            log.error("The requested resource {} on pin {} does not support state {}",
                    getClass().getSimpleName(), pinIdentifier, defaultState);
            throw new RuntimeException("Tried to allocate a resource to an unsupported state.");
        }

        this.defaultState = defaultState;
    }

    public List<PinCapability> getSupportedStates() {
        return supportedStates;
    }

    public Boolean supportsState(PinCapability state) {
        return supportedStates.contains(state);
    }
}
