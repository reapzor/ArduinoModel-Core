package com.bortbort.arduino.Model.Core.PinTypes;

import com.bortbort.arduino.FiloFirmata.*;
import com.bortbort.arduino.FiloFirmata.Messages.DigitalPortMessage;
import com.bortbort.arduino.Model.Core.MultiStatePin;
import com.bortbort.arduino.Model.Core.PinEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chuck on 3/21/2016.
 */
public class DigitalInputPin extends MultiStatePin {
    private static final Logger log = LoggerFactory.getLogger(DigitalInputPin.class);
    private DigitalPinValue inputValue;

    MessageListener<DigitalPortMessage> digitalPortListener = MessageListener.from(message -> {
        //message.getPinMappedValues().
    });

    public DigitalInputPin(Firmata firmata, PinEventManager eventManager, Integer pinIdentifier) {
        super(firmata, eventManager, pinIdentifier, PinCapability.INPUT, PinCapability.INPUT, PinCapability.INPUT_PULLUP);
    }

    public Boolean pullupEnabled() {
        return state == PinCapability.INPUT_PULLUP && outputValue == DigitalPinValue.HIGH;
    }

    public void enablePullup(Boolean enable) {
        enterState(enable ? PinCapability.INPUT_PULLUP : PinCapability.INPUT);
    }





    @Override
    protected Boolean startup() {
        return null;
    }

    @Override
    protected void shutdown() {

    }
}
