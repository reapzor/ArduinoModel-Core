package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SysexCapabilityQueryMessage;

/**
 * Created by chuck on 2/24/2016.
 */
public class ArduinoModel {
    Firmata firmataClient;

    public ArduinoModel(FirmataConfiguration firmataConfiguration) {
        firmataClient = new Firmata(firmataConfiguration);
    }

    private void discoverDevice() {
        firmataClient.addMessageListener(new MessageListener<SysexCapabilityMessage>() {
            @Override
            public void messageReceived(SysexCapabilityMessage message) {
                //message.
            }
        });
        firmataClient.sendMessage(new SysexCapabilityQueryMessage());

        //
    }


    public Firmata getFirmataClient() {
        return firmataClient;
    }
}
