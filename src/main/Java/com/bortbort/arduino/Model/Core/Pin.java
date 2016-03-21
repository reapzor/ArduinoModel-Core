package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SetPinModeMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by chuck on 3/4/2016.
 */
public abstract class Pin {
    private static final Logger log = LoggerFactory.getLogger(Pin.class);
    protected Firmata firmata;
    protected Integer id;
    protected PinCapability defaultState;
    protected PinCapability state = null;
    protected Integer value = null;
    protected Boolean allocated = null;
    private PinEventManager eventManager;


    public Pin(Firmata firmata, PinEventManager eventManager, Integer id, PinCapability defaultState) {
        this.firmata = firmata;
        this.eventManager = eventManager;
        this.id = id;
        this.defaultState = defaultState;
    }


    protected Boolean allocate() {
        log.info("Allocating pin {} to type {}", id, getClass().getSimpleName());
        allocated = enterDefaultState() && startup();
        return allocated;
    }

    protected void deallocate() {
        log.info("Deallocating pin {} from type {}", id, getClass().getSimpleName());
        allocated = false;
        shutdown();
    }

    protected abstract Boolean startup();

    protected abstract void shutdown();

    protected Boolean enterDefaultState() {
        if (firmata.sendMessage(new SetPinModeMessage(id, defaultState))) {
            return updateState();
        }

        log.error("Unable to transmit pin state change request from {} to {} for pin {}",
                state, defaultState, id);
        return false;
    }

    protected Boolean updateState() {
        SysexPinStateMessage message = firmata.sendMessageSynchronous(
                SysexPinStateMessage.class,
                new SysexPinStateQueryMessage(id));

        if (message == null) {
            log.error("Unable to retrieve current state for pin {}. Pin state may be stale.", id);
            return false;
        }

        if (!message.getPinIdentifier().equals(id)) {
            log.error("Pin ID mismatch in synchronized response!");
            throw new RuntimeException("Programming Error. If this is hit, then switch back to ASync Chuck!");
        }

        value = message.getPinValue();
        state = message.getCurrentPinMode();

        return true;
    }


    protected <K extends Pin> void dispatch(PinEvent<K> pinEvent) {
        if (!pinEvent.getPinType().equals(getClass())) {
            log.error("Unable to dispatch event {} for pin of type {}. This event is expecting a type of {}!",
                    pinEvent.getClass().getSimpleName(), getClass().getSimpleName(),
                    pinEvent.getPinType().getSimpleName());
            throw new RuntimeException("Invalid PinEvent for given pinType!");
        }

        pinEvent.setPin(pinEvent.getPinType().cast(this));
        eventManager.dispatchEvent(pinEvent);
    }





    public Integer getId() {
        return id;
    }

    public PinCapability getState() {
        return state;
    }

    public Integer getValue() {
        return value;
    }

    protected PinCapability getDefaultState() {
        return defaultState;
    }

    public Boolean getAllocated() {
        return allocated;
    }

}
