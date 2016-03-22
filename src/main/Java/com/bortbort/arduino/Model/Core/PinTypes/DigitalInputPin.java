package com.bortbort.arduino.Model.Core.PinTypes;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.DigitalPortMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.MultiStatePin;
import com.bortbort.arduino.Model.Core.PinEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by chuck on 3/21/2016.
 */
public class DigitalInputPin extends MultiStatePin {
    private static final Logger log = LoggerFactory.getLogger(DigitalInputPin.class);
    DigitalPinValue value;
    Integer integerValue;

    MessageListener<DigitalPortMessage> digitalPortListener = MessageListener.from(message -> {
        //message.
    });

    public DigitalInputPin(Firmata firmata, PinEventManager eventManager, Integer id) {
        super(firmata, eventManager, id, PinCapability.INPUT, PinCapability.INPUT, PinCapability.INPUT_PULLUP);
    }

    public Boolean pullupEnabled() {
        return state == PinCapability.INPUT_PULLUP;
    }

    public void setPullup(Boolean enable) {
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
