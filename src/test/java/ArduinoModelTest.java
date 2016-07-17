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
        arduinoModel.getEventManager().removeAllListeners();
        arduinoModel.stop();
        reset();
    }

    @Test
    public void testDigitalOutputPinEventing() throws Exception {
        DigitalOutputPin pin = arduinoModel.getResourceManager().allocatePin(5, DigitalOutputPin.class);

        PinEventListener<DigitalWriteEvent> pinListener = PinEventListener.from(5, pinEvent -> {
            assertNotNull(pinEvent.getCurrentValue());
            received();
        });

        arduinoModel.getEventManager().addListener(pinListener);

        pin.write(DigitalPinValue.HIGH);
        waitForCallback();

        arduinoModel.getEventManager().removeListener(pinListener);

    }

    @Test
    public void testDigitalInputPinEventing() throws Exception {
        DigitalInputPin pin = arduinoModel.getResourceManager().allocatePin(5, DigitalInputPin.class);
        DigitalInputPin pin2 = arduinoModel.getResourceManager().allocatePin(6, DigitalInputPin.class);

        PinEventListener<DigitalValueEvent> pinListener = PinEventListener.from(5, pinEvent -> {
            assertTrue(pinEvent.getPin().getPinIdentifier() == 5);
            assertNotNull(pinEvent.getCurrentValue());
            received();
        });

        PinEventListener<PullupValueEvent> pullupListener = PinEventListener.from(pinEvent -> {
            assertNotNull(pinEvent.getNewValue());
            received();
        });

        arduinoModel.getEventManager().addListener(pinListener);
        arduinoModel.getEventManager().addListener(pullupListener);

        pin2.togglePinEventing(true);
        pin.togglePinEventing(true);

        waitForCallback();
        pin2.togglePinEventing(false);
        pin.togglePinEventing(false);

        arduinoModel.getEventManager().removeListener(pinListener);

        assertFalse(pin.pullupEnabled());
        pin.enablePullup(true);
        waitForCallback();
        assertTrue(pin.pullupEnabled());
        pin.enablePullup(false);
        waitForCallback();
        assertFalse(pin.pullupEnabled());

        arduinoModel.getEventManager().removeListener(pullupListener);
    }

    @Test
    public void testAnalogPinEventing() throws Exception {
        AnalogPin pin = arduinoModel.getResourceManager().allocatePin(17, AnalogPin.class);

        PinEventListener<AnalogValueEvent> pinListener = new PinEventListener<AnalogValueEvent>(17) {
            @Override
            public void eventReceived(AnalogValueEvent pinEvent) {
                assertTrue(pinEvent.getCurrentValueInt() >= 0);
                assertTrue(pinEvent.getPin().equals(pin));
                received();
            }
        };

        arduinoModel.getEventManager().addListener(pinListener);

        arduinoModel.getFirmata().sendMessage(new ReportAnalogPinMessage(18, true));
        pin.togglePinEventing(true);
        waitForCallback();
        arduinoModel.getFirmata().sendMessage(new ReportAnalogPinMessage(18, false));
        pin.togglePinEventing(false);

        arduinoModel.getEventManager().removeListener(pinListener);
    }

    @Test
    public void testLambdaListenerEventing() throws Exception {
        AnalogPin pin = arduinoModel.getResourceManager().allocatePin(17, AnalogPin.class);

        PinEventListener<AnalogValueEvent> pinListener = PinEventListener.from(17, pinEvent -> {
            assertTrue(pinEvent.getCurrentValueInt() >= 0);
            received();
        });

        arduinoModel.getEventManager().addListener(pinListener);

        pin.togglePinEventing(true);
        waitForCallback();
        pin.togglePinEventing(false);

        arduinoModel.getEventManager().removeListener(pinListener);
    }

}
