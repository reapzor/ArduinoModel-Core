package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.PinCapability;

import java.util.ArrayList;

/**
 * Created by chuck on 2/28/2016.
 */
public class DigitalPin extends ArduinoPin {

    public DigitalPin(ArduinoPin arduinoPin) {
        super(arduinoPin);
    }

    @Override
    protected void dropListeners() {

    }

}