package com.bortbort.arduino.Model.Core.PinTypes;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.AnalogMessage;
import com.bortbort.arduino.FiloFirmata.Messages.ReportAnalogPinMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.AnalogPinMapper;
import com.bortbort.arduino.Model.Core.Pin;
import com.bortbort.arduino.Model.Core.PinEventManager;
import com.bortbort.arduino.Model.Core.PinEvents.AnalogValueEvent;
import com.bortbort.arduino.Model.Core.PinEvents.ReportAnalogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chuck on 3/4/2016.
 */
public class AnalogPin extends Pin {
    private static final Logger log = LoggerFactory.getLogger(AnalogPin.class);
    private Integer currentValueInt;
    private Byte currentValueByte;
    private Integer analogPinID;

    private MessageListener<AnalogMessage> analogListener = MessageListener.from(message -> {
        // Remove when stable
        if (!message.getChannelInt().equals(analogPinID)) {
            log.error("Listener hit for pin {} but is really for pin {}!",
                    analogPinID, message.getChannelInt());
            throw new RuntimeException("Coding error. Listener firing wrong pin.");
        }

        currentValueInt = message.getAnalogValue();
        currentValueByte = message.getAnalogValueByte();

        dispatch(new AnalogValueEvent(currentValueInt, currentValueByte));
    });


    public AnalogPin(Firmata firmata, PinEventManager eventManager, Integer id) {
        super(firmata, eventManager, id, PinCapability.ANALOG);
        analogPinID = AnalogPinMapper.getAnalogPinIdentifier(id);
    }


    public Boolean togglePinEventing(Boolean enable) {
        // No guarantee :(
        if (firmata.sendMessage(new ReportAnalogPinMessage(analogPinID, enable))) {
            dispatch(new ReportAnalogEvent(enable));
            return true;
        }
        return false;
    }


    @Override
    protected Boolean startup() {
        firmata.addMessageListener(analogPinID, analogListener);
        return true;
    }

    @Override
    protected void shutdown() {
        firmata.removeMessageListener(analogPinID, analogListener);
    }


    public Integer getCurrentValueInt() {
        return currentValueInt;
    }

    public Byte getCurrentValueByte() {
        return currentValueByte;
    }

    public Integer getAnalogPinID() {
        return analogPinID;
    }
}
