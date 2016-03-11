package com.bortbort.arduino.Model.Core;

import java.util.EventListener;

/**
 * Created by chuck on 3/6/2016.
 */
public abstract class PinListener implements EventListener {

    protected abstract <T extends PinEvent> void notify(T pinEvent);

}
