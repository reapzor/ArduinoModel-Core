package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.AnalogMessage;
import com.bortbort.arduino.FiloFirmata.Messages.ReportAnalogPinMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
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

    private MessageListener<AnalogMessage> analogListener = new MessageListener<AnalogMessage>() {
        @Override
        public void messageReceived(AnalogMessage message) {
            if (!message.getChannelInt().equals(analogPinID)) {
                log.error("Listener hit for pin {} but is really for pin {}!",
                        analogPinID, message.getChannelInt());
                throw new RuntimeException("Coding error. Listener firing wrong pin.");
            }

            currentValueInt = message.getAnalogValue();
            currentValueByte = message.getAnalogValueByte();
        }
    };


    protected AnalogPin(Firmata firmata, Integer id) {
        super(firmata, id, PinCapability.ANALOG);
        analogPinID = AnalogPinMapper.getAnalogPinIdentifier(id);
    }


    public Boolean togglePinEventing(Boolean enable) {
        return firmata.sendMessage(new ReportAnalogPinMessage(analogPinID, enable));
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
