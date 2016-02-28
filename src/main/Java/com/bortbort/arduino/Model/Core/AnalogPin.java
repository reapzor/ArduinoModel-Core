package com.bortbort.arduino.Model.Core;


/**
 * Created by chuck on 2/28/2016.
 */
public class AnalogPin extends ArduinoPin {

    public AnalogPin(ArduinoPin arduinoPin) {
        super(arduinoPin);
    }

    @Override
    protected void dropListeners() {

    }
}
