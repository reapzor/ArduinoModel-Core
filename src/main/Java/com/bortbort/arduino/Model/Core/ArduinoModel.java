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
    private PinResourceManager resourceManager;
    private PinEventManager eventManager = new PinEventManager();


    public ArduinoModel(FirmataConfiguration firmataConfiguration) {
        firmata = new Firmata(firmataConfiguration);
        resourceManager = new PinResourceManager(firmata, eventManager);
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

        if (!resourceManager.discoverPins()) {
            log.error("Failure populating pin details for board.");
            stop();
            return false;
        }

        return true;
    }

    public void stop() {
        firmata.stop();
        eventManager.stop();
        resourceManager.deallocatePins();
    }

    public Boolean isStarted() {
        return firmata.getStarted();
    }


    public Firmata getFirmata() {
        return firmata;
    }

    public PinResourceManager getResourceManager() {
        return resourceManager;
    }

    public PinEventManager getEventManager() {
        return eventManager;
    }
}
