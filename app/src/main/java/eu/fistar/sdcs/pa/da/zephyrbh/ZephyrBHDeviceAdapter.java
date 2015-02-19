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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.fistar.sdcs.pa.common.Capabilities;
import eu.fistar.sdcs.pa.common.DeviceDescription;
import eu.fistar.sdcs.pa.common.IDeviceAdapterListener;
import eu.fistar.sdcs.pa.common.Observation;
import eu.fistar.sdcs.pa.common.da.IDeviceAdapter;
import zephyr.android.BioHarnessBT.BTClient;

/**
 * This class is the implementation of the Device Adapter for the Zephyr BioHarness.
 *
 * @author Marcello Morena
 * @author Alexandru Serbanati
 */
public class ZephyrBHDeviceAdapter extends Service {

    // Generic Constants
    private final static String LOGTAG_ZEPHYRBH_SERVICE = "ZephyrBH >>>";
    private final static String DEVICE_NAME_PREFIX = "BH";

    // Default Configuration for BioHarness
    private final static Map<String, String> DEFAULT_CONFIG;
    static {
        Map<String, String> tmpConf = new HashMap<String, String>();
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_GENERAL, ZephyrBHConstants.CONFIG_DISABLE);
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_ACCELEROMETER, ZephyrBHConstants.CONFIG_DISABLE);
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_BREATHING, ZephyrBHConstants.CONFIG_DISABLE);
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_ECG, ZephyrBHConstants.CONFIG_ENABLE);
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_RTOR, ZephyrBHConstants.CONFIG_DISABLE);
        tmpConf.put(ZephyrBHConstants.CONFIG_NAME_LOGGING, ZephyrBHConstants.CONFIG_DISABLE);
        DEFAULT_CONFIG = Collections.unmodifiableMap(tmpConf);
    }

    private IDeviceAdapterListener paApi;

    private BluetoothAdapter btAdapt = BluetoothAdapter.getDefaultAdapter();

    private final Map<String, ZephyrBHDevice> connectedDevices = new HashMap<String, ZephyrBHDevice>();
    private final List<String> pairedDevices = new CopyOnWriteArrayList<String>();
    private final Map<String, Map<String, String>> devicesConfig = new HashMap<String, Map<String, String>>();

    private final List<String> blacklist = new CopyOnWriteArrayList<String>();
    private final List<String> whitelist = new CopyOnWriteArrayList<String>();

    /**
     * Implementation of the Device Adapter API (IDeviceAdapter) to pass to the Protocol Adapter
     */
    private final IDeviceAdapter.Stub paEndpoint = new IDeviceAdapter.Stub() {

        /**
         * Receive a binder from the Protocol Adapter representing its interface.
         *
         * @param pa The Protocol Adapter Binder
         */
        @Override
        public void registerDAListener(IBinder pa) {
            paApi = IDeviceAdapterListener.Stub.asInterface(pa);
        }

        /**
         * Return a list of all the devices connected at the moment with the Device Adapter.
         *
         * @return A list of the devices connected at the moment with the Device Adapter
         */
        @Override
        public List<DeviceDescription> getConnectedDevices() throws RemoteException {
            List<DeviceDescription> connDev = new ArrayList<DeviceDescription>();

            // Create a list of DeviceDescription starting from a list of ZephyrBHDevice
            for (ZephyrBHDevice dev : connectedDevices.values()) {
                connDev.add(new DeviceDescription(dev));
            }

            // Return the list
            return connDev;
        }

        /**
         * Return a list of the Device IDs of all the devices paired with the smartphone and managed
         * by the specific Device Adapter.
         *
         * @return A list of devices paired and handled by the Device Adapter
         */
        @Override
        public List<String> getPairedDevicesAddress() throws RemoteException {
            return pairedDevices;
        }

        /**
         * Return a list of devices that can be detected with a scanning.
         */
        @Override
        public List<String> detectDevices() throws RemoteException {
            throw new UnsupportedOperationException("Method not supported by " + DiscoveryResponder.CapabilitiesConstants.CAP_FRIENDLY_NAME + "!");
        }

        /**
         * Set the specific configuration of a device managed by the Device Adapter passing a data
         * structure with key-value pairs containing all possible configuration parameters and
         * their values, together with the device ID. This should be done before starting the Device
         * Adapter, otherwise standard configuration will be used. Depending on capabilities, this
         * could also be invoked when the DA is already running.
         *
         * @param config
         *		The configuration for the device in the form of a key/value set (String/String)
         *
         * @param devId
         *		The device ID (the MAC Address)
         */
        @Override
        public void setDeviceConfig(Map config, String devId) throws RemoteException {

            // Check whether the address provided is valid and the configuration is not null
            if (isValidDeviceId(devId) && config != null) {

                // Remove previous configurations for that device
                devicesConfig.remove(devId);

                // Put the new configuration
                devicesConfig.put(devId, config);

                // Check whether the device is connected
                ZephyrBHDevice device = connectedDevices.get(devId);
                if (device != null) {
                    ZephyrBHConnectedListener listener = device.getListener();

                    // Set the new configuration
                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_GENERAL))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_GENERAL);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_GENERAL);
                    }

                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_ACCELEROMETER))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_ACCELEROMETER);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_ACCELEROMETER);
                    }

                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_BREATHING))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_BREATHING);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_BREATHING);
                    }

                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_ECG))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_ECG);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_ECG);
                    }

                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_RTOR))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_RTOR);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_RTOR);
                    }

                    if (ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_LOGGING))) {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_ENABLE_LOGGING);
                    } else {
                        listener.parseCommand(ZephyrBHConstants.COMMAND_DISABLE_LOGGING);
                    }
                }
            }

        }

        /**
         * Return the object describing the capabilities of the DA. The implementation for this
         * method is mandatory.
         *
         * @return An instance of the Capabilities object containing all the capabilities of the
         * device
         */
        @Override
        public Capabilities getDACapabilities() throws RemoteException {
            return DiscoveryResponder.CAPABILITIES;
        }

        /**
         * Start the Device Adapter operations.
         */
        @Override
        public void start() throws RemoteException {
            // Register the broadcast receiver to catch device bonding at runtime
            IntentFilter filterPairedDevices = new IntentFilter();
            filterPairedDevices.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(broadcastPairedDevices, filterPairedDevices);

            // Register the broadcast receiver to catch device disconnection at runtime
            IntentFilter filterDisconnectedDevice = new IntentFilter();
            filterDisconnectedDevice.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            registerReceiver(broadcastDeviceDisconnection, filterDisconnectedDevice);
        }

        /**
         * Stop the Device Adapter operations. This will not close or disconnect the service.
         */
        @Override
        public void stop() throws RemoteException {
            // Unregister the broadcast receivers
            unregisterReceiver(broadcastPairedDevices);
            unregisterReceiver(broadcastDeviceDisconnection);

            // Close all connections
            for (String tmpDev : connectedDevices.keySet()) {
                disconnectDev(tmpDev);
            }

            // Perform a general clean
        }

        /**
         * Connect to the device whose MAC Address is passed as an argument.
         *
         * @param devId The device ID (the MAC Address)
         */
        @Override
        public void connectDev(String devId) throws RemoteException {

            // Check if devId is a well formed BT MAC Address
            if (!isValidDeviceId(devId)) return;

            // Check if the device is paired and supported
            if (!deviceIsInTheList(devId, pairedDevices)) throw new IllegalArgumentException("The device " + devId + " is not paired or not supported by Device Adapter!");

            // Connect to the device using the forceConnectDev
            forceConnectDev(devId);

        }

        /**
         * Force connection to the device whose devID is passed as an argument. This method can be
         * used to connect a supported device that, for some reasons, is not recognised by the DA.
         *
         * @param devId The device ID (the MAC Address)
         */
        @Override
        public void forceConnectDev(String devId) throws RemoteException {

            // Check if devId is a well formed BT MAC Address
            if (!isValidDeviceId(devId)) return;

            // Check whether the device is allowed to connect based on blacklist/whitelist
            if (!(whitelist.isEmpty() || deviceIsInTheList(devId, whitelist)) || deviceIsInTheList(devId, blacklist)) {
                throw new IllegalArgumentException("Connection to device " + devId + " failed because of blacklist/whitelist settings!");
            }

            // Check if there is a configuration entry inside the devicesConfig field, otherwise use the default configuration
            Map<String, String> config = devicesConfig.get(devId);
            if (config == null) config = DEFAULT_CONFIG;

            // Perform the connection using the Zephyr SDK
            BTClient bt = new BTClient(btAdapt, devId);
            bt.addConnectedEventListener(new ZephyrBHConnectedListener(ZephyrBHDeviceAdapter.this, devId, config));

            if (bt.IsConnected()) {
                bt.start();
            }
        }

        /**
         * Disconnect from the device whose DevID is passed as an argument.
         */
        @Override
        public void disconnectDev(String devId) throws RemoteException {

            // Check if devId is a well formed BT MAC Address
            if (!isValidDeviceId(devId)) return;

            ZephyrBHDevice device = connectedDevices.get(devId);

            // Check if the device is in the connected device Map
            if (device != null) {
                // Perform disconnection using the Zephyr SDK
                device.getClient().removeConnectedEventListener(device.getListener());
                device.getClient().Close();

                // Notify the Protocol Adapter about device disconnection
                try {
                    paApi.deviceDisconnected(new DeviceDescription(device));
                } catch (RemoteException e) {
                    Log.e(LOGTAG_ZEPHYRBH_SERVICE, "Failed notify device disconnection:\n" + device.toString());
                }

                // Remove the given device from the connected device Map
                connectedDevices.remove(device.getDeviceID());
            }

        }

        /**
         * Add a device to the Device Adapter whitelist, passing its device ID as an argument.
         * Note that this insertion will persist, even through Device Adapter reboots, until
         * the device it's removed from the list. Every device adapter should check the format
         * of the address passed as an argument and, if it does not support that kind of
         * address, it can safely ignore that address.
         *
         * @param devId The Device ID
         */
        @Override
        public void addDeviceToWhitelist(String devId) throws RemoteException {
            // If the device is already in the list, just do nothing
            if (!isValidDeviceId(devId) || deviceIsInTheList(devId, whitelist)) return;

            // Othwerwise add it to the list
            whitelist.add(devId);

            // Refresh the connection status to disconnect every connected device that is not in
            // the whitelist
            refreshConnections();
        }

        /**
         * Remove from the whitelist the device whose device ID is passed as an argument.
         * If the device is not in the list, the request can be ignored.
         *
         * @param devId The Device ID
         */
        @Override
        public void removeDeviceFromWhitelist(String devId) throws RemoteException {
            // If the device is not in the list, just do nothing
            if (!isValidDeviceId(devId) || !deviceIsInTheList(devId, whitelist)) return;

            // Othwerwise remove it from the list
            whitelist.remove(devId);
        }

        /**
         * Retrieve all the devices in the whitelist of the DA. If there's no devices, an
         * empty list is returned.
         *
         * @return The list containing all the devices ID in the whitelist
         */
        @Override
        public List<String> getWhitelist() throws RemoteException {
            // Just return the whitelist
            return whitelist;
        }

        /**
         * Set a list of devices in the whitelist all together, passing their device IDs as an
         * argument. Note that this insertion will persist, even through Device Adapter reboots,
         * until the devices are removed from the list. Every device adapter should check the format
         * of the address passed as an argument one by one and, if it does not support that kind of
         * address, it can safely ignore that address.
         *
         * @param devicesId A list containing all the devices ID to set in the whitelist
         */
        @Override
        public void setWhitelist(List<String> devicesId) throws RemoteException {

            // Empty the list
            emptyList(whitelist);

            if (devicesId != null) {
                // Add to the whitelist every element of the list passed as argument
                for (String dev : devicesId) {
                    if (isValidDeviceId(dev)) whitelist.add(dev);
                }

                // Refresh the connection status to disconnect every connected device that is not in
                // the whitelist
                refreshConnections();
            }

        }

        /**
         * Add a device to the Device Adapter blacklist, passing its device ID as an argument.
         * Note that this insertion will persist, even through Device Adapter reboots, until
         * the device it's removed from the list. Every device adapter should check the format
         * of the address passed as an argument and, if it does not support that kind of
         * address, it can safely ignore that address.
         *
         * @param devId The Device ID
         */
        @Override
        public void addDeviceToBlackList(String devId) throws RemoteException {
            // If the device is already in the list, just do nothing
            if (!isValidDeviceId(devId) || deviceIsInTheList(devId, blacklist)) return;

            // Othwerwise add it to the list
            blacklist.add(devId);

            // If the device is connected, disconnect it
            if (connectedDevices.get(devId) != null) {
                disconnectDev(devId);
            }
        }

        /**
         * Remove from the blacklist the device whose device ID is passed as an argument.
         * If the device is not in the list, the request can be ignored.
         *
         * @param devId The Device ID
         */
        @Override
        public void removeDeviceFromBlacklist(String devId) throws RemoteException {
            // If the device is not in the list, just do nothing
            if (!isValidDeviceId(devId) || !deviceIsInTheList(devId, blacklist)) return;

            // Othwerwise remove it from the list
            blacklist.remove(devId);

        }

        /**
         * Retrieve all the devices in the blacklist of the DA. If there's no devices, an
         * empty list is returned.
         *
         * @return A list containing all the devices ID in the blacklist
         */
        @Override
        public List<String> getBlacklist() throws RemoteException {
            // Just return the blacklist
            return blacklist;
        }

        /**
         * Set a list of devices in the blacklist all together, passing their device IDs as an
         * argument. Note that this insertion will persist, even through Device Adapter reboots,
         * until the devices are removed from the list. Every device adapter should check the format
         * of the address passed as an argument one by one and, if it does not support that kind of
         * address, it can safely ignore that address.
         *
         * @param devicesId A list containing all the devices ID to set in the blacklist
         */
        @Override
        public void setBlackList(List<String> devicesId) throws RemoteException {
            // Empty the list
            emptyList(blacklist);

            if (devicesId != null) {
                // Add to the blacklist every element of the list passed as argument
                for (String dev : devicesId) {
                    if (isValidDeviceId(dev)) {
                        blacklist.add(dev);

                        // If the device is connected, disconnect it
                        if (connectedDevices.get(dev) != null) {
                            disconnectDev(dev);
                        }
                    }
                }
            }

        }

        /**
         * Return all the commands supported by the Device Adapter for its devices.
         *
         * @return A list of commands supported by the Device Adapter
         */
        @Override
        public List<String> getCommandList() throws RemoteException {
            return ZephyrBHConstants.COMMAND_LIST;
        }

        /**
         * Execute a command supported by the device. You can also specify a parameter, if the command
         * requires or allows it.
         *
         * @param command The command to execute on the device
         * @param parameter The optional parameter to pass to the device together with the command
         * @param devId The Device ID
         */
        @Override
        public void execCommand(String command, String parameter, String devId) throws RemoteException {
            ZephyrBHDevice dev = connectedDevices.get(devId);
            if (dev != null) {
                dev.getListener().parseCommand(command);
            } else {
                throw new IllegalArgumentException("The device " + devId + " is not valid or not connected to Device Adapter at the moment!");
            }
        }
    };

    private BroadcastReceiver broadcastPairedDevices = new BroadcastReceiver() {

        /**
         * Handle the pairing or the unpairing of a supported devices at runtime
         *
         * @param context The context provided by the OS
         * @param intent The Intent received from the OS
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {

                // Retrieve the bond state and the device involved
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // If the device has been paired...
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    // ...check whether is supported and add it to the list
                    if (isSupportedDevice(dev.getAddress())) {
                        pairedDevices.add(dev.getAddress());
                    }
                }

                // If the device has been unpaired...
                else if (bondState == BluetoothDevice.BOND_NONE) {
                    // ...remove it from the list
                    if (pairedDevices.contains(dev.getAddress())) {
                        pairedDevices.remove(dev.getAddress());
                    }
                }
            }
        }

    };

    private BroadcastReceiver broadcastDeviceDisconnection = new BroadcastReceiver() {

        /**
         * Handle the disconnection of a supported devices at runtime
         *
         * @param context The context provided by the OS
         * @param intent The Intent received from the OS
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {

                // Retrieve the device involved
                String devAddr = ((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getAddress();

                // If the disconnected device was one of the device handled by this DA, close the connection properly and notify Protocol Adapter
                if (connectedDevices.get(devAddr) != null) {
                    try {
                        paEndpoint.disconnectDev(devAddr);
                    } catch (RemoteException e) {
                        Log.e(LOGTAG_ZEPHYRBH_SERVICE, "Failed notify device disconnection:\n" + devAddr);
                    }
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {

        // Retrieve devices that are both paired and supported
        populatePairedDevices();

        // Return the API endpoint
        return paEndpoint;
    }

    /**
     * Callback used by listener to notify that a device has connected
     *
     * @param device The device involved in the event
     */
    public synchronized void deviceConnected(ZephyrBHDevice device) {

        // Register the newly connected device with the Protocol Adapter
        try {
            paApi.registerDevice(new DeviceDescription(device), DiscoveryResponder.CapabilitiesConstants.DA_ID);
        } catch (RemoteException e) {
            Log.e(LOGTAG_ZEPHYRBH_SERVICE, "Failed registering new device:\n" + device.toString());
        }

        // Insert the newly connected device in the connected device Map
        connectedDevices.put(device.getDeviceID(), device);
    }

    /**
     * Callback used by listener to deliver a new observation generated by the device
     *
     * @param observation The object containing the data provided by the device
     * @param device The device involved in the event
     */
    public synchronized void receivedMeasurement(Observation observation, ZephyrBHDevice device) {

        // Create a list of observations
        List<Observation> obsList = new ArrayList<Observation>();
        obsList.add(observation);

        // Send the received measurement to the Protocol Adapter
        try {
            paApi.pushData(obsList, new DeviceDescription(device));
        } catch (RemoteException e) {
            Log.e(LOGTAG_ZEPHYRBH_SERVICE, "Failed pushing device measurement:\n" + device.toString() + "\nFor device:\n" + device.toString());
        }
    }

    /**
     * Check if the given device is in the provided device list
     *
     * @param devId The device to check
     * @param list The list of devices
     * @return True if the device is in the list, false otherwise
     */
    private boolean deviceIsInTheList(String devId, List<String> list) {
        if (devId == null || "".equals(devId)) return false;
        for (String dev:list) {
            if (devId.equals(dev)) return true;
        }
        return false;
    }

    /**
     * Remove every element on this list
     *
     * @param list The list of devices
     */
    private void emptyList(List<String> list) {

        if (list == null || list.isEmpty()) return;

        for (String dev:list) {
            list.remove(dev);
        }

    }

    /**
     * Check the whitelist for connected device not in the list and if any disconnects them
     */
    private void refreshConnections() {
        if (whitelist.isEmpty()) return;

        for (String dev:connectedDevices.keySet()) {
            if (!whitelist.contains(dev)) {
                try {
                    paEndpoint.disconnectDev(dev);
                } catch (RemoteException e) {
                    return;
                }
            }
        }
    }

    /**
     * Validate the Device ID, which in the Zephry's case is the BT MAC Address
     *
     * @param devId The device ID (the MAC Address)
     * @return True is the ID is valid, false otherwise
     */
    private boolean isValidDeviceId(String devId) {
        if (devId == null || "".equals(devId)) return false;
        return BluetoothAdapter.checkBluetoothAddress(devId.toUpperCase());
    }

    /**
     * Check whether the device is both supported and paired
     *
     * @param devId The device ID (the MAC Address)
     * @return True if the device is supported, false otherwise
     */
    private boolean isSupportedDevice(String devId) {

        // Check if the Device ID is valid
        if (!isValidDeviceId(devId)) return false;

        // Retrieve the paired device in the system
        Set<BluetoothDevice> devs = btAdapt.getBondedDevices();

        // Search for the given device and check if it's supported
        for (BluetoothDevice tmpDev : devs) {
            if (devId.equals(tmpDev.getAddress()) && tmpDev.getName().startsWith(DEVICE_NAME_PREFIX)) return true;
        }

        // If the given device is not paired or not supported return false
        return false;
    }

    /**
     * Populate the pairedDevice list with Device IDs of supported devices that are also paired in
     * the system
     */
    private void populatePairedDevices() {
        // Retrieve the paired device in the system
        Set<BluetoothDevice> devs = btAdapt.getBondedDevices();

        // Search in the device list for devices that are both paired and supported and add them to the list
        for (BluetoothDevice tmpDev : devs) {
            if (tmpDev.getName().startsWith(DEVICE_NAME_PREFIX))
                pairedDevices.add(tmpDev.getAddress());
        }
    }

}
