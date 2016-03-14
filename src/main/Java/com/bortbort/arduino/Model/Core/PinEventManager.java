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
    ArrayList<PinListener> genericListeners = new ArrayList<>();
    ArrayList<PinListener> pinListeners = new ArrayList<>();

    protected void startup() {
        if (executor != null) {
            log.info("Already connected!");
            return;
        }
        executor = Executors.newSingleThreadExecutor();
    }

    protected void shutdown() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    public void addListener(PinListener listener) {
        if (listener.getEventType().equals(PinEvent.class)) {
            if (!genericListeners.contains(listener)) {
                genericListeners.add(listener);
            }
            return;
        }

        if (!pinListeners.contains(listener)) {
            pinListeners.add(listener);
        }
    }

    public void removeListener(PinListener listener) {
        genericListeners.remove(listener);
        pinListeners.remove(listener);
    }

    public void removeAllListeners() {
        genericListeners.clear();
        pinListeners.clear();
    }

    @SuppressWarnings("unchecked")
    public void dispatchEvent(PinEvent pinEvent) {
        if (executor == null) {
            log.warn("Dispatcher is not running!");
            return;
        }

        executor.submit(() -> {
            pinListeners.stream()
                    .filter(aListener -> aListener.getPinIdentifier().equals(pinEvent.getPinIdentifier()))
                    .filter(pinListener -> pinListener.getEventType().equals(pinEvent.getClass()))
                    .forEach(filteredListener -> filteredListener.eventReceived(pinEvent));

            genericListeners.stream()
                    .filter(aListener -> aListener.getPinIdentifier().equals(pinEvent.getPinIdentifier()))
                    .forEach(filteredListener -> filteredListener.eventReceived(pinEvent));
        });
    }


}
