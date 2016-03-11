package com.bortbort.arduino.Model.Core;

import java.util.HashMap;

/**
 * Created by chuck on 3/7/2016.
 */
public class PinEventManager {
    private class Key implements Comparable<Key> {
        private Integer pinID = null;
        private Class<? extends PinEvent> pinEventType = null;

        public Key() {
        }

        public Key(Integer pinID) {
            this.pinID = pinID;
        }

        public Key(Class<? extends PinEvent> pinEventType) {
            this.pinEventType = pinEventType;
        }

        public Key(Integer pinID, Class<? extends PinEvent> pinEventType) {
            this.pinID = pinID;
            this.pinEventType = pinEventType;
        }

        public Integer getPinID() {
            return pinID;
        }

        public Class<? extends PinEvent> getPinEventType() {
            return pinEventType;
        }

        @Override
        public int compareTo(Key o) {
            return Integer.compare(pinID, o.getPinID());
        }


//        @Override
//        public int hashCode() {
//            //return new HashCodeB;
//        }

        @Override
        public boolean equals(Object obj) {

            return super.equals(obj);
        }
    }



}
