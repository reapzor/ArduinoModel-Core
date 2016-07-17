package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by chuck on 2/28/2016.
 * Temporary class
 */
public class AnalogPinMapper {
    private static final Logger log = LoggerFactory.getLogger(AnalogPinMapper.class);
    private static ArrayList<ArrayList<PinCapability>> pinCapabilities = null;
    private static Integer firstAnalogPinIdentifier = null;

    public static Integer getAnalogPinIdentifier(Integer digitalPinIdentifier) {
        if (pinCapabilities == null) {
            throw new RuntimeException("pinCapabilities must be populated first!");
        }

        return digitalPinIdentifier - firstAnalogPinIdentifier;
    }

    public static Integer getDigitalPinIdentifier(Integer analogPinIdentifier) {
        if (pinCapabilities == null) {
            throw new RuntimeException("pinCapabilities must be populated first!");
        }

        return firstAnalogPinIdentifier + analogPinIdentifier;
    }

    static void setPinCapabilities(ArrayList<ArrayList<PinCapability>> pinCapabilities) {
        AnalogPinMapper.pinCapabilities = pinCapabilities;

        for (int x = 0; x < pinCapabilities.size(); x++) {
            if (pinCapabilities.get(x).contains(PinCapability.ANALOG)) {
                firstAnalogPinIdentifier = x;
                break;
            }
        }

        if (firstAnalogPinIdentifier == null) {
            log.error("There are no analog pins!?");
        }
    }
}
