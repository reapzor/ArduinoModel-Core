package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SetPinModeMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Created by chuck on 3/4/2016.
 */
public abstract class Pin<T extends PinListener> {
    private static final Logger log = LoggerFactory.getLogger(Pin.class);
    protected Firmata firmata;
    protected Integer id;
    protected PinCapability defaultState;
    protected PinCapability state = null;
    protected Integer value = null;
    protected Boolean allocated = null;
    protected ArrayList<T> pinListeners = new ArrayList<>();



    protected Pin(Firmata firmata, Integer id, PinCapability defaultState) {
        this.firmata = firmata;
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

    public void addListener(T pinListener) {
        if (!pinListeners.contains(pinListener)) {
            pinListeners.add(pinListener);
        }
    }

    public void removeListener(T pinListener) {
        pinListeners.remove(pinListener);
    }

    public <K extends PinEvent> void notify(K event) {
        pinListeners.stream().forEach(listener -> listener.notify(event));
    }



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
