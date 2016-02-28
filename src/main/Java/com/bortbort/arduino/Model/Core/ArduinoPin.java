package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.DigitalChannel;
import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.SetPinModeMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.FiloFirmata.Messages.SysexPinStateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by chuck on 2/27/2016.
 */
public class ArduinoPin {
    private static final Logger log = LoggerFactory.getLogger(ArduinoPin.class);
    private Firmata firmataClient;
    private Integer pinID;
    private DigitalChannel channelID;
    private ArrayList<PinCapability> pinCapabilities;
    private Integer pinValue;
    private PinCapability currentState;
    private Class<? extends ArduinoPin> currentAssignedClass = null;
    private ArduinoPin currentAssignedObject = null;

    protected ArduinoPin(ArduinoPin arduinoPin) {
        this.firmataClient = arduinoPin.firmataClient;
        this.pinID = arduinoPin.pinID;
        this.channelID = arduinoPin.channelID;
        this.pinCapabilities = arduinoPin.pinCapabilities;
        this.pinValue = arduinoPin.pinValue;
        this.currentState = arduinoPin.currentState;
        this.currentAssignedClass = arduinoPin.currentAssignedClass;
        this.currentAssignedObject = arduinoPin.currentAssignedObject;
    }

    protected ArduinoPin(Firmata firmataClient, Integer pinID, ArrayList<PinCapability> pinCapabilities) {
        this.firmataClient = firmataClient;
        this.pinID = pinID;
        this.pinCapabilities = pinCapabilities;
        this.channelID = DigitalChannel.getChannelForPin(pinID);
    }

    public Boolean updateCurrentState() {
        SysexPinStateMessage message = firmataClient.sendMessageSynchronous(
                SysexPinStateMessage.class,
                new SysexPinStateQueryMessage(pinID));

        if (message == null) {
            log.error("Unable to retrieve current state for pin {}. Pin state may be stale.", pinID);
            return false;
        }

        if (!message.getPinIdentifier().equals(pinID)) {
            log.error("Pin ID mismatch in synchronized response!");
            throw new RuntimeException("Programming Error. If this is hit, then switch back to ASync Chuck!");
        }

        pinValue = message.getPinValue();
        currentState = message.getCurrentPinMode();

        return true;
    }

    public ArrayList<Class<? extends ArduinoPin>> getSupportedClasses() {
        ArrayList<Class<? extends ArduinoPin>> supportedClasses = new ArrayList<>();

        supportedClasses.add(DigitalPin.class);

        switch(currentState) {
            case ANALOG:
                supportedClasses.add(AnalogPin.class);
                break;
            case PWM:
                supportedClasses.add(PWMPin.class);
                break;
            default:
                break;
        }

        return supportedClasses;
    }

    public <T extends ArduinoPin> T castTo(Class<T> castClass) {
        if (getSupportedClasses().contains(castClass)) {
            dropListeners();
            currentAssignedClass = castClass;

            Constructor<T> constructor;
            try {
                constructor = castClass.getConstructor(ArduinoPin.class);
            } catch (NoSuchMethodException e) {
                log.error("Desired class {} does not have ArduinoPin constructor!", castClass.getSimpleName());
                e.printStackTrace();
                throw new RuntimeException("Cannot instantiate object. Programming error.");
            }

            try {
                currentAssignedObject = constructor.newInstance(this);
                return castClass.cast(currentAssignedObject);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Desired class {} will not construct!", castClass.getSimpleName());
                e.printStackTrace();
                throw new RuntimeException("Cannot instantiate object. Programming error.");
            }
        }

        log.error("Desired class is not within the supported classes list! Cannot cast to {} while in state {}",
                castClass.getSimpleName(), currentState);

        throw new ClassCastException("Desired class cannot be cast to current pin state.");
    }

    public Boolean setState(PinCapability desiredState) {
        if (!supportsCapability(desiredState)) {
            log.error("Desired state {} is not supported on pin {}.", desiredState, pinID);
            return false;
        }

        return sendStateChangeRequest(desiredState);
    }

    public <T extends ArduinoPin> T setState(Class<T> castClass, PinCapability desiredState) {
        if (setState(desiredState)) {
            return castTo(castClass);
        }

        return null;
    }

    private Boolean sendStateChangeRequest(PinCapability pinState) {
        if (firmataClient.sendMessage(new SetPinModeMessage(pinID, pinState))) {
            return updateCurrentState();
        }

        log.error("Unable to transmit pin state change request from {} to {} for pin {}",
                currentState, pinState, pinID);
        return false;
    }


    public Integer getPinID() {
        return pinID;
    }

    public DigitalChannel getChannelID() {
        return channelID;
    }

    public ArrayList<PinCapability> getPinCapabilities() {
        return pinCapabilities;
    }

    public Integer getPinValue() {
        return pinValue;
    }

    public PinCapability getCurrentState() {
        return currentState;
    }

    public Boolean supportsCapability(PinCapability pinCapability) {
        return pinCapabilities.contains(pinCapability);
    }

    protected void dropListeners() {
        if (currentAssignedClass != null) {
            currentAssignedClass.cast(currentAssignedObject).dropListeners();
        }
        currentAssignedClass = null;
        currentAssignedObject = null;
    }

}
