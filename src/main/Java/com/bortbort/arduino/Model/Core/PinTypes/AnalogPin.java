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
import com.bortbort.arduino.Model.Core.PinResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chuck on 3/4/2016.
 */
public class AnalogPin extends Pin {
    private static final Logger log = LoggerFactory.getLogger(AnalogPin.class);
    private Integer analogPinID;
    private Integer currentValueInt = null;
    private Byte currentValueByte = null;
    private Boolean pinEventing = false;

    private MessageListener<AnalogMessage> analogListener = MessageListener.from(message -> {
        // Remove when stable
        if (!message.getChannelInt().equals(analogPinID)) {
            log.error("Listener hit for pin {} but is really for pin {}!",
                    analogPinID, message.getChannelInt());
            throw new RuntimeException("Coding error. Listener firing wrong pin.");
        }

        Integer previousValueInt = currentValueInt;
        Byte previousValueByte = currentValueByte;
        currentValueInt = message.getAnalogValue();
        currentValueByte = message.getAnalogValueByte();

        fireEvent(new AnalogValueEvent(previousValueInt, previousValueByte, currentValueInt, currentValueByte));
    });


    public AnalogPin(Firmata firmata, PinEventManager eventManager, PinResource pinResource, Integer pinIdentifier) {
        super(firmata, eventManager, pinResource, pinIdentifier, PinCapability.ANALOG);
        analogPinID = AnalogPinMapper.getAnalogPinIdentifier(pinIdentifier);
    }


    public void togglePinEventing(Boolean enable) {
        if (pinEventing == enable) {
            return;
        }

        pinEventing = enable;
        fireEvent(new ReportAnalogEvent(enable));

        firmata.sendMessage(new ReportAnalogPinMessage(analogPinID, enable));
    }


    @Override
    protected Boolean startup() {
        firmata.addMessageListener(analogPinID, analogListener);
        return true;
    }

    @Override
    protected void shutdown() {
        togglePinEventing(false);
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
