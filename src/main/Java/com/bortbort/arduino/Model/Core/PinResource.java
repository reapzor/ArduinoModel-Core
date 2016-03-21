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
    private Integer id;
    private ArrayList<PinCapability> capabilities;
    private Class<? extends Pin> allocatedType = null;
    private Pin allocatedInstance = null;


    protected PinResource(Firmata firmata, PinEventManager eventManager,
                          Integer id, ArrayList<PinCapability> capabilities) {
        this.firmata = firmata;
        this.eventManager = eventManager;
        this.id = id;
        this.capabilities = capabilities;
    }



    public <T extends Pin> T allocate(Class<T> pinClass) {
        if (isAllocated()) {
            log.error("Trying to allocate {} to {} but it has not yet been freed from {}! deallocate() it first!",
                    id, pinClass.getSimpleName(), allocatedType.getSimpleName());
            throw new RuntimeException("Tried to allocate a resource that has not yet been freed.");
        }

        Constructor<T> constructor;

        try {
            constructor = pinClass.getDeclaredConstructor(Firmata.class, PinEventManager.class, Integer.class);
        } catch (NoSuchMethodException e) {
            log.error("Supplied an invalid class {} to bind pin {} to.", pinClass.getSimpleName(), id);
            e.printStackTrace();
            throw new RuntimeException("Cannot find constructor for Pin class. Programmer error.");
        }

        T pinInstance;

        try {
            pinInstance = constructor.newInstance(firmata, eventManager, id);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to instantiate pin {} as class {}.", id, pinClass.getSimpleName());
            e.printStackTrace();
            throw new RuntimeException("Cannot instantiate Pin object. Programmer error.");
        }

        if (pinInstance instanceof  MultiStatePin) {
            if (((MultiStatePin) pinInstance).getSupportedStates().stream()
                    .filter(capability -> capabilities.contains(capability))
                    .limit(1).count() != 1) {
                log.error("Pin types {} are unsupported for pin id {}. Supported types: {}.",
                        ((MultiStatePin) pinInstance).getSupportedStates(), id, capabilities);
                throw new RuntimeException("Cannot allocate MultiStatePin with unsupported types!");
            }
        }

        if (!capabilities.contains(pinInstance.getDefaultState())) {
            log.error("Pin type {} is unsupported for pin id {}. Supported types: {}.",
                    pinInstance.getDefaultState(), id, capabilities);
            throw new RuntimeException("Cannot allocate Pin with unsupported type!");
        }

        allocatedType = pinClass;
        allocatedInstance = pinInstance;

        if (!allocatedInstance.allocate()) {
            log.error("Failed to allocate pin {} as {}", id, allocatedType.getSimpleName());
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

    public Integer getId() {
        return id;
    }

    public ArrayList<PinCapability> getCapabilities() {
        return capabilities;
    }


}
