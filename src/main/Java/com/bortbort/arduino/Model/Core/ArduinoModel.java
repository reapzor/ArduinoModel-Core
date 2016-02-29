package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityQueryMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by chuck on 2/24/2016.
 */
public class ArduinoModel {
    private static final Logger log = LoggerFactory.getLogger(ArduinoModel.class);
    Firmata firmataClient;
    ArrayList<ArrayList<PinCapability>> pinCapabilities = null;
    ArrayList<ArduinoPin> arduinoPins = new ArrayList<>();



    public ArduinoModel(FirmataConfiguration firmataConfiguration) {
        firmataClient = new Firmata(firmataConfiguration);
    }



    public Boolean discoverPins() {
        SysexCapabilityMessage clientCapabilitiesMessage = firmataClient.sendMessageSynchronous(
                SysexCapabilityMessage.class,
                new SysexCapabilityQueryMessage());

        if (clientCapabilitiesMessage == null) {
            return false;
        }

        AnalogPinMapper.setPinCapabilities(clientCapabilitiesMessage.getPinCapabilities());

        clearPins();
        pinCapabilities = clientCapabilitiesMessage.getPinCapabilities();

        Boolean allPinsUpdated = true;

        for (int x = 0; x < pinCapabilities.size(); x++) {
            ArduinoPin pin = new ArduinoPin(firmataClient, x, pinCapabilities.get(x));
            if (!pin.updateCurrentState()) {
                allPinsUpdated = false;
            }
            arduinoPins.add(pin);
        }

        return allPinsUpdated;
    }

    private void clearPins() {
        for (ArduinoPin pin : arduinoPins) {
            pin.dropListeners();
        }
        arduinoPins.clear();
    }

    public void stop() {
        clearPins();
        pinCapabilities.clear();

        if (firmataClient.getStarted()) {
            firmataClient.stop();
        }
    }


    public ArrayList<ArrayList<PinCapability>> getPinCapabilities() {
        return pinCapabilities;
    }

    public ArrayList<ArduinoPin> getArduinoPins() {
        return arduinoPins;
    }

    public Firmata getFirmataClient() {
        return firmataClient;
    }
}
