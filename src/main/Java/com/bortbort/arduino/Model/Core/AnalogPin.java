package com.bortbort.arduino.Model.Core;


import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.AnalogMessage;
import com.bortbort.arduino.FiloFirmata.Messages.ReportAnalogPinMessage;

/**
 * Created by chuck on 2/28/2016.
 */
public class AnalogPin extends ArduinoPin {
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

    public AnalogPin(ArduinoPin arduinoPin) {
        super(arduinoPin);
        analogPinID = AnalogPinMapper.getAnalogPinIdentifier(getPinID());
        firmataClient.addMessageListener(analogPinID, analogListener);
    }


    public Boolean togglePinEventing(Boolean enable) {
        return firmataClient.sendMessage(new ReportAnalogPinMessage(analogPinID, enable));
    }

    public Integer getCurrentValueInt() {
        return currentValueInt;
    }

    public Byte getCurrentValueByte() {
        return currentValueByte;
    }

    @Override
    protected void dropListeners() {
        firmataClient.removeMessageListener(analogPinID, analogListener);
    }

}
