/**
 * Created by chuck on 3/20/2016.
 */

import static com.jayway.awaitility.Awaitility.*;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.Messages.ReportAnalogPinMessage;
import com.bortbort.arduino.Model.Core.ArduinoModel;
import com.bortbort.arduino.Model.Core.PinEventListener;
import com.bortbort.arduino.Model.Core.PinEvents.AnalogValueEvent;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ArduinoModelTest {
    ArduinoModel arduinoModel;
    private Boolean receievedCallback = false;

    private void received() {
        receievedCallback = true;
    }

    private void reset() {
        receievedCallback = false;
    }

    private void waitForCallback() {
        await().atMost(1, SECONDS).until(() -> receievedCallback);
        reset();
    }

    @Before
    public void before() throws Exception {
        arduinoModel = new ArduinoModel(new FirmataConfiguration());
        assertTrue(arduinoModel.start());
    }

    @After
    public void after() {
        arduinoModel.removeAllListeners();
        arduinoModel.stop();
        reset();
    }


    @Test
    public void testAnalogPinEventing() throws Exception {
        AnalogPin pin = arduinoModel.allocatePin(17, AnalogPin.class);

        PinEventListener<AnalogValueEvent> pinListener = new PinEventListener<AnalogValueEvent>() {
            @Override
            public void eventReceived(AnalogValueEvent pinEvent) {
                assertTrue(pinEvent.getCurrentValueInt() >= 0);
                assertTrue(pinEvent.getPin().equals(pin));
                received();
            }
        };

        arduinoModel.addListener(pinListener);

        arduinoModel.getFirmata().sendMessage(new ReportAnalogPinMessage(18, true));
        assertTrue(pin.togglePinEventing(true));

        waitForCallback();

        arduinoModel.getFirmata().sendMessage(new ReportAnalogPinMessage(18, false));
        assertTrue(pin.togglePinEventing(false));

        arduinoModel.removeListener(pinListener);
    }

    @Test
    public void testLambdaListenerEventing() throws Exception {
        AnalogPin pin = arduinoModel.allocatePin(17, AnalogPin.class);

        PinEventListener<AnalogValueEvent> pinListener = PinEventListener.from(pinEvent -> {
            assertTrue(pinEvent.getCurrentValueInt() >= 0);
            received();
        });

        arduinoModel.addListener(pinListener);

        assertTrue(pin.togglePinEventing(true));

        waitForCallback();

        assertTrue(pin.togglePinEventing(false));

        arduinoModel.removeListener(pinListener);
    }

}
