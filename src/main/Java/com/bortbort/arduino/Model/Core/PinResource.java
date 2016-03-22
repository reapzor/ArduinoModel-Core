package com.bortbort.arduino.Model.Core;

import com.bortbort.arduino.FiloFirmata.Firmata;
import com.bortbort.arduino.FiloFirmata.PinCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Created by chuck on 3/4/2016.
 */
public class PinResource {
    private static final Logger log = LoggerFactory.getLogger(PinResource.class);
    private Firmata firmata;
    private PinEventManager eventManager;
    private Integer pinIdentifier;
    private ArrayList<PinCapability> capabilities;
    private Class<? extends Pin> allocatedType = null;
    private Pin allocatedInstance = null;


    protected PinResource(Firmata firmata, PinEventManager eventManager,
                          Integer pinIdentifier, ArrayList<PinCapability> capabilities) {
        this.firmata = firmata;
        this.eventManager = eventManager;
        this.pinIdentifier = pinIdentifier;
        this.capabilities = capabilities;
    }



    public <T extends Pin> T allocate(Class<T> pinClass) {
        if (isAllocated()) {
            log.error("Trying to allocate {} to {} but it has not yet been freed from {}! deallocate() it first!",
                    pinIdentifier, pinClass.getSimpleName(), allocatedType.getSimpleName());
            throw new RuntimeException("Tried to allocate a resource that has not yet been freed.");
        }

        Constructor<T> constructor;

        try {
            constructor = pinClass.getDeclaredConstructor(Firmata.class, PinEventManager.class, Integer.class);
        } catch (NoSuchMethodException e) {
            log.error("Supplied an invalid class {} to bind pin {} to.", pinClass.getSimpleName(), pinIdentifier);
            e.printStackTrace();
            throw new RuntimeException("Cannot find constructor for Pin class. Programmer error.");
        }

        T pinInstance;

        try {
            pinInstance = constructor.newInstance(firmata, eventManager, pinIdentifier);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to instantiate pin {} as class {}.", pinIdentifier, pinClass.getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Cannot instantiate Pin object. Programmer error.");
        }

        if (pinInstance instanceof MultiStatePin) {
            long aCapabilityMatch = ((MultiStatePin) pinInstance).getSupportedStates().stream()
                    .filter(capability -> capabilities.contains(capability))
                    .limit(1).count();
            if (aCapabilityMatch != 1) {
                log.error("Pin types {} are unsupported for pin pinIdentifier {}. Supported types: {}.",
                        ((MultiStatePin) pinInstance).getSupportedStates(), pinIdentifier, capabilities);
                throw new RuntimeException("Cannot allocate MultiStatePin with unsupported types!");
            }
        }
        else if (!capabilities.contains(pinInstance.getDefaultState())) {
            log.error("Pin type {} is unsupported for pin pinIdentifier {}. Supported types: {}.",
                    pinInstance.getDefaultState(), pinIdentifier, capabilities);
            throw new RuntimeException("Cannot allocate Pin with unsupported type!");
        }

        allocatedType = pinClass;
        allocatedInstance = pinInstance;

        if (!allocatedInstance.allocate()) {
            log.error("Failed to allocate pin {} as {}", pinIdentifier, allocatedType.getSimpleName());
            deallocate();
            return null;
        }

        return pinInstance;
    }

    protected <T extends MultiStatePin> T allocate(Class<T> pinClass, PinCapability defaultState) {
        T pinInstance = allocate(pinClass);
        pinInstance.setDefaultState(defaultState);
        return pinInstance;
    }

    public void deallocate() {
        if (allocatedInstance != null && !allocatedInstance.getAllocated()) {
            allocatedInstance.deallocate();
        }
        allocatedInstance = null;
        allocatedType = null;
    }


    public <T extends Pin> T getAllocatedInstance(Class<T> pinClass) {
        if (allocatedType == null) {
            log.warn("No allocated instance to return!");
            return null;
        }

        if (pinClass == allocatedType) {
            return pinClass.cast(allocatedInstance);
        }

        log.error("Use getAllocatedInstance(getAllocatedType()). The supplied class {} is not the allocatedType {}.",
                pinClass, allocatedType);

        throw new RuntimeException("Cannot cast allocatedInstance to anything other than allocatedType!");
    }

    public Class<? extends Pin> getAllocatedType() {
        return allocatedType;
    }

    public Pin getAllocatedInstance() {
        return allocatedInstance;
    }

    public Boolean isAllocated() {
        return allocatedInstance != null;
    }

    public Integer getPinIdentifier() {
        return pinIdentifier;
    }

    public ArrayList<PinCapability> getCapabilities() {
        return capabilities;
    }


}
