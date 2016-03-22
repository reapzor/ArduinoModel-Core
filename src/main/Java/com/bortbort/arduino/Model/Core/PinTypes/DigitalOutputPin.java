package com.bortbort.arduino.Model.Core.PinTypes;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SetDigitalPinValueMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.Pin;
import com.bortbort.arduino.Model.Core.PinEventManager;

/**
 * Created by chuck on 3/21/2016.
 */
public class DigitalOutputPin extends Pin {
    //private static final Logger log = LoggerFactory.getLogger(DigitalOutputPin.class);
    DigitalPinValue outputValue = null;

    public DigitalOutputPin(Firmata firmata, PinEventManager eventManager, Integer id) {
        super(firmata, eventManager, id, PinCapability.OUTPUT);
    }

    public Boolean write (Byte pinValue) {
        return write(DigitalPinValue.valueFromByte(pinValue));
    }

    public Boolean write(Integer pinValue) {
        return write(DigitalPinValue.valueFromInt(pinValue));
    }

    public Boolean write(DigitalPinValue pinValue) {
        if (firmata.sendMessage(new SetDigitalPinValueMessage(id, pinValue))) {
            outputValue = pinValue;
            return true;
        }
        return false;
    }

    public DigitalPinValue getOutputValue() {
        return outputValue;
    }

    @Override
    protected Boolean startup() {
        return write(DigitalPinValue.LOW);
    }

    @Override
    protected void shutdown() {
        write(DigitalPinValue.LOW);
    }
}