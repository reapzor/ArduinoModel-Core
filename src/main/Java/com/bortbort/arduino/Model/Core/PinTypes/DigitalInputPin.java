package com.bortbort.arduino.Model.Core.PinTypes;

import com.bortbort.arduino.FiloFirmata.*;
import com.bortbort.arduino.FiloFirmata.Messages.DigitalPortMessage;
import com.bortbort.arduino.FiloFirmata.Messages.ReportDigitalPortMessage;
import com.bortbort.arduino.Model.Core.MultiStatePin;
import com.bortbort.arduino.Model.Core.PinEventManager;
import com.bortbort.arduino.Model.Core.PinEvents.DigitalValueEvent;
import com.bortbort.arduino.Model.Core.PinEvents.ReportDigitalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

/**
 * Created by chuck on 3/21/2016.
 */
public class DigitalInputPin extends MultiStatePin {
    private static ArrayList<DigitalInputPin> pinEventingList = new ArrayList<>();

    private static void addEventingPin(DigitalInputPin digitalInputPin) {
        if (!pinEventingList.contains(digitalInputPin)) {
            pinEventingList.add(digitalInputPin);
        }
    }

    private static Boolean containsEventingChannel(DigitalChannel digitalChannel) {
        return pinEventingList.stream().
                anyMatch(eventingPin -> eventingPin.getDigitalChannel().equals(digitalChannel));
    }

    private static Boolean isLastEventingPinForChannel(DigitalInputPin digitalInputPin) {
        return !pinEventingList.stream()
                .anyMatch(eventingPin -> !eventingPin.equals(digitalInputPin)
                        && !eventingPin.getDigitalChannel().equals(digitalInputPin.getDigitalChannel()));
    }

    private static void removeEventingPin(DigitalInputPin digitalInputPin) {
        pinEventingList.remove(digitalInputPin);
    }




    private static final Logger log = LoggerFactory.getLogger(DigitalInputPin.class);
    private Integer inputIntegerValue = null;
    private DigitalPinValue inputValue = null;
    private Boolean pinEventing = false;

    MessageListener<DigitalPortMessage> digitalPortListener = MessageListener.from(message -> {
        if (pinEventing) {
            Integer previousInputIntegerValue = inputIntegerValue;
            DigitalPinValue previousInputValue = inputValue;
            inputIntegerValue = message.getPinValues().get(pinIdentifier);
            inputValue = message.getDigitalPinValues().get(pinIdentifier);

            fireEvent(new DigitalValueEvent(previousInputIntegerValue, previousInputValue,
                    inputIntegerValue, inputValue));
        }
    });

    public DigitalInputPin(Firmata firmata, PinEventManager eventManager, Integer pinIdentifier) {
        super(firmata, eventManager, pinIdentifier, PinCapability.INPUT, PinCapability.INPUT, PinCapability.INPUT_PULLUP);
    }

    public void togglePinEventing(Boolean enable) {
        if (pinEventing == enable) {
            return;
        }

        Boolean shouldSendMessage = false;
        if (enable) {
            if (!DigitalInputPin.containsEventingChannel(getDigitalChannel())) {
                shouldSendMessage = true;
            }
            DigitalInputPin.addEventingPin(this);
        }
        else {
            if (DigitalInputPin.isLastEventingPinForChannel(this)) {
                shouldSendMessage = true;
            }
            DigitalInputPin.removeEventingPin(this);
        }

        pinEventing = enable;

        if (shouldSendMessage) {
            fireEvent(new ReportDigitalEvent(enable));
            firmata.sendMessage(new ReportDigitalPortMessage(digitalChannel, enable));
        }
    }

    public Boolean pullupEnabled() {
        return state == PinCapability.INPUT_PULLUP && outputValue == DigitalPinValue.HIGH;
    }

    public void enablePullup(Boolean enable) {
        enterState(enable ? PinCapability.INPUT_PULLUP : PinCapability.INPUT);
    }


    public Integer getInputIntegerValue() {
        return inputIntegerValue;
    }

    public DigitalPinValue getInputValue() {
        return inputValue;
    }

    @Override
    protected Boolean startup() {
        firmata.addMessageListener(digitalChannel, digitalPortListener);
        return true;
    }

    @Override
    protected void shutdown() {
        togglePinEventing(false);
        firmata.removeMessageListener(digitalChannel, digitalPortListener);
    }
}
