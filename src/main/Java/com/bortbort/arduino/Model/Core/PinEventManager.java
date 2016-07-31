package com.bortbort.arduino.Model.Core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chuck on 3/7/2016.
 */
public class PinEventManager {
    private static final Logger log = LoggerFactory.getLogger(PinEventManager.class);
    private ExecutorService executor;
    private ArrayList<PinEventListener> pinEventListeners = new ArrayList<>();

    protected void start() {
        if (executor != null) {
            log.info("Already connected!");
            return;
        }
        executor = Executors.newSingleThreadExecutor();
    }

    protected void stop() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public void addListener(PinEventListener listener) {
        if (!pinEventListeners.contains(listener)) {
            pinEventListeners.add(listener);
        }
    }

    public void removeListener(PinEventListener listener) {
        pinEventListeners.remove(listener);
    }

    public void removeAllListeners() {
        pinEventListeners.clear();
    }

    @SuppressWarnings("unchecked")
    public void fireEvent(PinEvent pinEvent) {
        if (executor == null) {
            log.warn("Dispatcher is not running!");
            return;
        }

        executor.submit(() ->
            pinEventListeners.stream()
                    .filter(aListener ->
                            (aListener.getPinIdentifier() == null ||
                            aListener.getPinIdentifier().equals(pinEvent.getPinIdentifier()))
                            && (aListener.getEventType().equals(pinEvent.getClass()) ||
                            aListener.getEventType().equals(PinEvent.class))
                    ).forEach(filteredListener -> filteredListener.eventReceived(pinEvent))
        );
    }


}
