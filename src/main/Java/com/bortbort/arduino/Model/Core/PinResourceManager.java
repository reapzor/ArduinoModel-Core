package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalInputPin;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalOutputPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chuck on 4/14/2016.
 */
public class PinResourceManager {
    private static final Logger log = LoggerFactory.getLogger(PinResourceManager.class);
    private Firmata firmata;
    private PinEventManager pinEventManager;
    private List<PinResource> pinResources;
    private ArrayList<ArrayList<PinCapability>> pinCapabilities = null;


    public PinResourceManager(Firmata firmata, PinEventManager pinEventManager) {
        this.firmata = firmata;
        this.pinEventManager = pinEventManager;
    }

    protected Boolean discoverPins() {
        SysexCapabilityMessage clientCapabilitiesMessage = firmata.sendMessageSynchronous(
                SysexCapabilityMessage.class,
                new SysexCapabilityQueryMessage());

        if (clientCapabilitiesMessage == null) {
            return false;
        }

        deallocatePins();

        AnalogPinMapper.setPinCapabilities(clientCapabilitiesMessage.getPinCapabilities());

        pinCapabilities = clientCapabilitiesMessage.getPinCapabilities();

        PinResource[] pinResourceArray = new PinResource[pinCapabilities.size()];

        for (int x = 0; x < pinCapabilities.size(); x++) {
            PinResource pin = new PinResource(firmata, pinEventManager, this, x, pinCapabilities.get(x));
            pinResourceArray[x] = pin;
        }

        pinResources = Arrays.asList(pinResourceArray);

        return true;
    }

    public Pin getPin(Integer pinID) {
        return getPin(pinID, Pin.class);
    }

    public AnalogPin getAnalogPin(Integer analogPinID) {
        return getAnalogPin(analogPinID, AnalogPin.class);
    }

    public <T extends AnalogPin> T getAnalogPin(Integer analogPinID, Class<T> analogPinWrapper) {
        return getPin(AnalogPinMapper.getDigitalPinIdentifier(analogPinID), analogPinWrapper);
    }

    public DigitalInputPin getDigitalInputPin(Integer digitalPinID) {
        return getDigitalInputPin(digitalPinID, DigitalInputPin.class);
    }

    public <T extends DigitalInputPin> T getDigitalInputPin(Integer digitalPinID, Class<T> digitalPinWrapper) {
        return getPin(digitalPinID, digitalPinWrapper);
    }

    public DigitalOutputPin getDigitalOutputPin(Integer digitalPinID) {
        return getDigitalOutputPin(digitalPinID, DigitalOutputPin.class);
    }

    public <T extends DigitalOutputPin> T getDigitalOutputPin(Integer digitalPinID, Class<T> digitalPinWrapper) {
        return getPin(digitalPinID, digitalPinWrapper);
    }

    public <T extends Pin> T getPin(Integer pinID, Class<T> pinClass) {
        if (!pinResources.get(pinID).isAllocated()) {
            log.error("Pin {} is unallocated. Cannot cast to {}!", pinID, pinClass);
            throw new RuntimeException("Attempt to access unallocated pin.");
        }
        if (!pinResources.get(pinID).getAllocatedType().equals(pinClass) &&
                !pinClass.getSimpleName().equals("Pin")) {
            log.error("Pin {} is allocated as {}. Cannot cast to {}!",
                    pinID, pinResources.get(pinID).getAllocatedType(), pinClass);
            throw new RuntimeException("Attempt to access pin of wrong allocated type.");
        }

        return pinClass.cast(pinResources.get(pinID).getAllocatedInstance());
    }




    public <T extends Pin> T allocatePin(Integer pinID, Class<T> pinClass) {
        return pinResources.get(pinID).allocate(pinClass);
    }

    public <T extends MultiStatePin> T allocatePin(Integer pinID, Class<T> pinClass, PinCapability defaultState) {
        return pinResources.get(pinID).allocate(pinClass, defaultState);
    }




    public void deallocatePin(Integer pinID) {
        pinResources.get(pinID).deallocate();
    }

    public void deallocatePin(Pin pin) {
        pinResources.get(pin.getPinIdentifier()).deallocate();
    }



    protected void deallocatePins() {
        if (pinResources == null) {
            return;
        }

        pinResources.stream().forEach(PinResource::deallocate);
    }



    public Boolean isPinAllocated(Integer resourceID) {
        return pinResources.get(resourceID).isAllocated();
    }

    public List<PinResource> getPinResources() {
        return pinResources;
    }

    public ArrayList<ArrayList<PinCapability>> getPinCapabilities() {
        return pinCapabilities;
    }
}
