import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.MessageListener;
import com.bortbort.arduino.FiloFirmata.Messages.AnalogMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SystemResetMessage;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import com.bortbort.arduino.Model.Core.AnalogPin;
import com.bortbort.arduino.Model.Core.ArduinoModel;
import static org.junit.Assert.*;

import com.bortbort.arduino.Model.Core.ArduinoPin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by chuck on 2/28/2016.
 */
public class ArduinoModelTest {
    ArduinoModel model;

    @Before
    public void before() {
        model = new ArduinoModel(new FirmataConfiguration("COM3"));
        assertTrue(model.getFirmataClient().sendMessage(new SystemResetMessage()));
        assertTrue(model.discoverPins());
    }

    @After
    public void after() {
        model.getFirmataClient().removeAllListeners();
        model.stop();
    }

    @Test
    public void testAnalogPinAssignment() throws Exception {
        assertTrue(model.getArduinoPins().size() >= 19);

        ArduinoPin arduinoPin = model.getArduinoPins().get(19);
        assertTrue(arduinoPin.supportsCapability(PinCapability.ANALOG));

        arduinoPin.setState(PinCapability.INPUT);

        try {
            arduinoPin.castTo(AnalogPin.class);
            fail();
        }
        catch (ClassCastException e) {}

        AnalogPin analogPin = arduinoPin.setState(AnalogPin.class, PinCapability.ANALOG);

        assertNotNull(analogPin);
        analogPin = analogPin.castTo(AnalogPin.class);
        assertNotNull(analogPin);

        Thread.sleep(50);

        analogPin.togglePinEventing(false);

        MessageListener<AnalogMessage> analogNoListener = new MessageListener<AnalogMessage>() {
            @Override
            public void messageReceived(AnalogMessage message) {
                fail();
            }
        };

        model.getFirmataClient().addMessageListener(analogNoListener);

        Thread.sleep(100);

        model.getFirmataClient().removeMessageListener(analogNoListener);

        arduinoPin.setState(PinCapability.INPUT);
    }





}


