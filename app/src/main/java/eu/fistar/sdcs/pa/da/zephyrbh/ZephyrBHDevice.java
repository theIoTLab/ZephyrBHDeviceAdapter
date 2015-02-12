/**
 * Copyright (C) 2014 Consorzio Roma Ricerche
 * All rights reserved
 *
 * This file is part of the Protocol Adapter software, available at
 * https://github.com/theIoTLab/ProtocolAdapter .
 *
 * The Protocol Adapter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://opensource.org/licenses/LGPL-3.0
 *
 * Contact Consorzio Roma Ricerche (protocoladapter@gmail.com)
 */

package eu.fistar.sdcs.pa.da.zephyrbh;

import java.util.ArrayList;
import java.util.List;

import eu.fistar.sdcs.pa.common.DeviceDescription;
import eu.fistar.sdcs.pa.common.SensorDescription;
import zephyr.android.BioHarnessBT.BTClient;

/**
 * This class represents a Zephyr BioHarness device and extends DeviceDescription. In addiction to
 * the fields of the standard device object, it also holds fields representing the BTClient and the
 * listener passed to the Zephyr library.
 *
 * @author Marcello Morena
 * @author Alexandru Serbanati
 */
public class ZephyrBHDevice extends DeviceDescription {

    private String deviceID; // The unique device identifier
    private String serialNumber; // The device's serial number, empty if not automatically provided by the device
    private String address; // The MAC Address of the device
    private BTClient client;
    private ZephyrBHConnectedListener listener;
    private boolean registered;

    // Static fields for the Zephyr BioHarness 3
    private final static String modelName = ZephyrBHConstants.BH_MODEL_NAME; // The model name
    private final static String manufacturerName = ZephyrBHConstants.BH_MANUFACTURER_NAME; // The manufacturer name
    private final static List<SensorDescription> sensorList = ZephyrBHConstants.SENSOR_LIST; // The list of the sensors

    public ZephyrBHDevice(String devId, BTClient client, ZephyrBHConnectedListener listener) {
        deviceID = devId;
        serialNumber = devId;
        address = devId;
        registered = false;
        this.client = client;
        this.listener = listener;
    }

    @Override
    public String getDeviceID() {
        return deviceID;
    }

    @Override
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getManufacturerName() {
        return manufacturerName;
    }

    public BTClient getClient() {
        return client;
    }

    public ZephyrBHConnectedListener getListener() {
        return listener;
    }

    @Override
    public List<SensorDescription> getSensorList() {
        return new ArrayList<SensorDescription>(sensorList);
    }

    @Override
    public String getAddress() {
        return address;
    }

    /**
     * Set whether the device is registered in the Protocol Adapter or not
     *
     * @param mRegistered
     *      The boolean value representing registration
     */
    @Override
    public void setRegistered(boolean mRegistered) {
        registered = mRegistered;
    }

    /**
     * Check if the device is registered in the Protocol Adapter or not
     *
     * @return
     *      The boolean value representing registration
     */
    @Override
    public boolean isRegistered() {
        return registered;
    }

    /**
     * Returns a read-friendly String representing the object
     *
     * @return
     *      The String representing the object
     */
    public String toString() {
        String propStr = "\n";

        for (SensorDescription temp: sensorList) {
            propStr += temp.toString();
        }

        return "ID: "+deviceID+"\nModel Number: "+ serialNumber +"\nModel Name: "+modelName+
                "\nManufacturer: "+manufacturerName+"\nAddress: "+address+"\nProperties: "+propStr;
    }
}
