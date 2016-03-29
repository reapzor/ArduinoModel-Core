package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chuck on 2/24/2016.
 */
public class ArduinoModel {
    private static final Logger log = LoggerFactory.getLogger(ArduinoModel.class);
    private Firmata firmata;
    private List<PinResource> pinResources;
    private ArrayList<ArrayList<PinCapability>> pinCapabilities = null;
    private PinEventManager eventManager = new PinEventManager();


    public ArduinoModel(FirmataConfiguration firmataConfiguration) {
        firmata = new Firmata(firmataConfiguration);
    }


    public Boolean start() {
        if (firmata.getStarted()) {
            return true;
        }

        eventManager.start();

        if (!firmata.start()) {
            log.error("Unable to start Firmata library.");
            return false;
        }

        if (!discoverPins()) {
            log.error("Failure populating pin details for board.");
            stop();
            return false;
        }

        return true;
    }

    public void stop() {
        firmata.stop();
        eventManager.stop();
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
            PinResource pin = new PinResource(firmata, eventManager, x, pinCapabilities.get(x));
            pinResourceArray[x] = pin;
        }

        pinResources = Arrays.asList(pinResourceArray);

        return true;
    }

    public <T extends Pin> T getAllocatedPin(Integer pinID, Class<T> pinClass) {
        if (!pinResources.get(pinID).isAllocated()) {
            log.error("Pin {} is unallocated. Cannot cast to {}!", pinID, pinClass);
            throw new RuntimeException("Attempt to access unallocated pin.");
        }
        if (!pinResources.get(pinID).getAllocatedType().equals(pinClass)) {
            log.error("Pin {} is allocated as {}. Cannot cast to {}!",
                    pinID, pinResources.get(pinID).getAllocatedType(), pinClass);
            throw new RuntimeException("Attempt to access pin of wrong allocated type.");
        }

        return pinClass.cast(pinResources.get(pinID).getAllocatedInstance());
    }

    public Boolean isPinAllocated(Integer resourceID) {
        return pinResources.get(resourceID).isAllocated();
    }

    public <T extends Pin> T allocatePin(Integer pinID, Class<T> pinClass) {
        return pinResources.get(pinID).allocate(pinClass);
    }

    public <T extends MultiStatePin> T allocatePin(Integer pinID, Class<T> pinClass, PinCapability defaultState) {
        return pinResources.get(pinID).allocate(pinClass, defaultState);
    }

//    public AnalogPin allocateAnalogPin(Integer analogPinIdentifier) {
//        return pinResources.get(0).allocate(AnalogPin.class);
//    }

    public void deallocatePin(Integer pinID) {
        pinResources.get(pinID).deallocate();
    }

    protected void deallocatePins() {
        if (pinResources == null) {
            return;
        }

        pinResources.stream().forEach(PinResource::deallocate);
    }


    public void addListener(PinEventListener listener) {
        eventManager.addListener(listener);
    }

    public void removeListener(PinEventListener listener) {
        eventManager.removeListener(listener);
    }

    public void removeAllListeners() {
        eventManager.removeAllListeners();
    }


    public Boolean isStarted() {
        return firmata.getStarted();
    }


    public List<PinResource> getPinResources() {
        return pinResources;
    }

    public PinResource getPinResource(Integer pinID) {
        return pinResources.get(pinID);
    }

    public ArrayList<ArrayList<PinCapability>> getPinCapabilities() {
        return pinCapabilities;
    }

    public Firmata getFirmata() {
        return firmata;
    }
}
