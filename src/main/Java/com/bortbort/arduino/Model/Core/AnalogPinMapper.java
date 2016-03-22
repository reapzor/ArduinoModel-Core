package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by chuck on 2/28/2016.
 */
public class AnalogPinMapper {
    private static final Logger log = LoggerFactory.getLogger(AnalogPinMapper.class);
    private static ArrayList<ArrayList<PinCapability>> pinCapabilities = null;

    public static Integer getAnalogPinIdentifier(Integer digitalPinIdentifier) {
        if (pinCapabilities == null) {
            throw new RuntimeException("pinCapabilities must be populated first!");
        }

        Integer firstAnalogPinIdentifier = null;
        for (int x = 0; x < pinCapabilities.size(); x++) {
            if (pinCapabilities.get(x).contains(PinCapability.ANALOG)) {
                firstAnalogPinIdentifier = x;
                break;
            }
        }

        if (firstAnalogPinIdentifier == null) {
            log.error("There are no analog pins!?");
            return null;
        }

        if (digitalPinIdentifier < firstAnalogPinIdentifier) {
            log.warn("Digital pin {} does not have analog support!", digitalPinIdentifier);
            return null;
        }

        return digitalPinIdentifier - firstAnalogPinIdentifier;
    }

    static void setPinCapabilities(ArrayList<ArrayList<PinCapability>> pinCapabilities) {
        AnalogPinMapper.pinCapabilities = pinCapabilities;
    }
}
