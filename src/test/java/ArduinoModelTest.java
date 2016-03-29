/**
 * Created by chuck on 3/20/2016.
 */

import static com.jayway.awaitility.Awaitility.*;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;

import com.bortbort.arduino.FiloFirmata.DigitalPinValue;
import com.bortbort.arduino.FiloFirmata.FirmataConfiguration;
import com.bortbort.arduino.FiloFirmata.Messages.ReportAnalogPinMessage;
import com.bortbort.arduino.FiloFirmata.Messages.SystemResetMessage;
import com.bortbort.arduino.Model.Core.ArduinoModel;
import com.bortbort.arduino.Model.Core.PinEventListener;
import com.bortbort.arduino.Model.Core.PinEvents.AnalogValueEvent;
import com.bortbort.arduino.Model.Core.PinEvents.DigitalValueEvent;
import com.bortbort.arduino.Model.Core.PinEvents.DigitalWriteEvent;
import com.bortbort.arduino.Model.Core.PinEvents.PullupValueEvent;
import com.bortbort.arduino.Model.Core.PinTypes.AnalogPin;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalInputPin;
import com.bortbort.arduino.Model.Core.PinTypes.DigitalOutputPin;
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
        assertTrue(arduinoModel.getFirmata().sendMessage(new SystemResetMessage()));
    }

    @After
    public void after() {
        arduinoModel.removeAllListeners();
        arduinoModel.stop();
        reset();
    }

    @Test
    public void testDigitalOutputPinEventing() throws Exception {
        DigitalOutputPin pin = arduinoModel.allocatePin(5, DigitalOutputPin.class);

        PinEventListener<DigitalWriteEvent> pinListener = PinEventListener.from(pinEvent -> {
            assertNotNull(pinEvent.getCurrentValue());
            received();
        });

        arduinoModel.addListener(pinListener);

        pin.write(DigitalPinValue.HIGH);
        waitForCallback();

        arduinoModel.removeListener(pinListener);

    }

    @Test
    public void testDigitalInputPinEventing() throws Exception {
        DigitalInputPin pin = arduinoModel.allocatePin(5, DigitalInputPin.class);
        //DigitalInputPin pin2 = arduinoModel.allocatePin(6, DigitalInputPin.class);

        PinEventListener<DigitalValueEvent> pinListener = PinEventListener.from(pinEvent -> {
            assertNotNull(pinEvent.getCurrentValue());
            received();
        });

        PinEventListener<PullupValueEvent> pullupListener = PinEventListener.from(pinEvent -> {
            assertNotNull(pinEvent.getNewValue());
            received();
        });

        arduinoModel.addListener(pinListener);
        arduinoModel.addListener(pullupListener);

        pin.togglePinEventing(true);
        //pin2.togglePinEventing(true);
        //pin2.togglePinEventing(false);

        waitForCallback();
        pin.togglePinEventing(false);

        arduinoModel.removeListener(pinListener);

        assertFalse(pin.pullupEnabled());
        pin.enablePullup(true);
        waitForCallback();
        assertTrue(pin.pullupEnabled());
        pin.enablePullup(false);
        waitForCallback();
        assertFalse(pin.pullupEnabled());

        arduinoModel.removeListener(pullupListener);
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
        pin.togglePinEventing(true);
        waitForCallback();
        arduinoModel.getFirmata().sendMessage(new ReportAnalogPinMessage(18, false));
        pin.togglePinEventing(false);

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

        pin.togglePinEventing(true);
        waitForCallback();
        pin.togglePinEventing(false);

        arduinoModel.removeListener(pinListener);
    }

}
