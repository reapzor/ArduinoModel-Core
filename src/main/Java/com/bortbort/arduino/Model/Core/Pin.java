package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.DigitalChannel;
import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SetPinModeMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.PinEvents.PullupValueEvent;
import com.bortbort.arduino.Model.Core.PinEvents.StateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chuck on 3/4/2016.
 */
public abstract class Pin {
    private static final Logger log = LoggerFactory.getLogger(Pin.class);
    protected Firmata firmata;
    protected Integer pinIdentifier;
    protected DigitalChannel digitalChannel;
    protected PinCapability defaultState;
    protected PinCapability state = null;
    protected DigitalPinValue outputValue = null;
    protected Integer outputIntegerValue = null;
    protected Boolean allocated = null;
    private PinEventManager eventManager;


    public Pin(Firmata firmata, PinEventManager eventManager, Integer pinIdentifier, PinCapability defaultState) {
        this.firmata = firmata;
        this.eventManager = eventManager;
        this.pinIdentifier = pinIdentifier;
        this.defaultState = defaultState;
        digitalChannel = DigitalChannel.getChannelForPin(pinIdentifier);
    }


    protected Boolean allocate() {
        log.info("Allocating pin {} to type {}", pinIdentifier, getClass().getSimpleName());
        allocated = enterDefaultState() && startup();
        return allocated;
    }

    protected void deallocate() {
        log.info("Deallocating pin {} from type {}", pinIdentifier, getClass().getSimpleName());
        allocated = false;
        shutdown();
    }

    protected abstract Boolean startup();

    protected abstract void shutdown();

    protected Boolean enterDefaultState() {
        if (firmata.sendMessage(new SetPinModeMessage(pinIdentifier, defaultState))) {
            return updateState();
        }

        log.error("Unable to transmit pin state change request from {} to {} for pin {}",
                state, defaultState, pinIdentifier);
        return false;
    }

    protected Boolean updateState() {
        SysexPinStateMessage message = firmata.sendMessageSynchronous(
                SysexPinStateMessage.class,
                new SysexPinStateQueryMessage(pinIdentifier));

        if (message == null) {
            log.error("Unable to retrieve current state for pin {}. Pin state may be stale.", pinIdentifier);
            return false;
        }

        if (!message.getPinIdentifier().equals(pinIdentifier)) {
            log.error("Pin ID mismatch in synchronized response!");
            throw new RuntimeException("Programming Error. If this is hit, then switch back to ASync Chuck!");
        }

        PinCapability previousState = state;
        Integer previousIntegerValue = outputIntegerValue;
        DigitalPinValue previousValue = outputValue;

        outputIntegerValue = message.getPinValue();
        outputValue = message.getDigitalPinValue();
        state = message.getCurrentPinMode();

        if (previousState != null && previousState != state) {
            fireEvent(new StateEvent(previousState, state));
        }

        if (previousIntegerValue != null && !previousIntegerValue.equals(outputIntegerValue)) {
            fireEvent(new PullupValueEvent(previousIntegerValue, outputIntegerValue, previousValue, outputValue));
        }

        return true;
    }


    protected <K extends Pin> void fireEvent(PinEvent<K> pinEvent) {
        if (!pinEvent.getPinType().equals(getClass()) && !pinEvent.getPinType().equals(Pin.class)) {
            log.error("Unable to fireEvent event {} for pin of type {}. This event is expecting a type of {}!",
                    pinEvent.getClass().getSimpleName(), getClass().getSimpleName(),
                    pinEvent.getPinType().getSimpleName());
            throw new RuntimeException("Invalid PinEvent for given pinType!");
        }

        pinEvent.setPin(pinEvent.getPinType().cast(this));
        eventManager.fireEvent(pinEvent);
    }





    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

    public DigitalChannel getDigitalChannel() {
        return digitalChannel;
    }

    public DigitalPinValue getOutputValue() {
        return outputValue;
    }

    public Integer getOutputIntegerValue() {
        return outputIntegerValue;
    }

    public PinCapability getState() {
        return state;
    }

    protected PinCapability getDefaultState() {
        return defaultState;
    }

    public Boolean getAllocated() {
        return allocated;
    }

}
