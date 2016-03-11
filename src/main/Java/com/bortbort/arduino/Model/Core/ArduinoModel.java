package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
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
    Firmata firmata;
    List<PinResource> pinResources;
    ArrayList<ArrayList<PinCapability>> pinCapabilities = null;


    public ArduinoModel(FirmataConfiguration firmataConfiguration) {
        firmata = new Firmata(firmataConfiguration);
    }



    public Boolean start() {
        if (firmata.getStarted()) {
            return true;
        }

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
    }


    protected Boolean discoverPins() {
        SysexCapabilityMessage clientCapabilitiesMessage = firmata.sendMessageSynchronous(
                SysexCapabilityMessage.class,
                new SysexCapabilityQueryMessage());

        if (clientCapabilitiesMessage == null) {
            return false;
        }

        deallocateResources();

        AnalogPinMapper.setPinCapabilities(clientCapabilitiesMessage.getPinCapabilities());

        pinCapabilities = clientCapabilitiesMessage.getPinCapabilities();

        PinResource[] pinResourceArray = new PinResource[pinCapabilities.size()];

        for (int x = 0; x < pinCapabilities.size(); x++) {
            PinResource pin = new PinResource(firmata, x, pinCapabilities.get(x));
            pinResourceArray[x] = pin;
        }

        pinResources = Arrays.asList(pinResourceArray);

        return true;
    }

    public Boolean isResourceAllocated(Integer resourceID) {
        return pinResources.get(resourceID).isAllocated();
    }

    public <T extends Pin> T allocateResource(Integer pinID, Class<T> pinClass) {
        return pinResources.get(pinID).allocate(pinClass);
    }

    public <T extends MultiStatePin> T allocateResource(Integer pinID, Class<T> pinClass, PinCapability defaultState) {
        return pinResources.get(pinID).allocate(pinClass, defaultState);
    }

    public void deallocateResource(Integer pinID) {
        pinResources.get(pinID).deallocate();
    }

    protected void deallocateResources() {
        //pinResources.stream().forEach(PinResource::deallocate);
        for (PinResource pinResource : pinResources) {
            pinResource.deallocate();
        }
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
