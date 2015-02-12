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

import java.util.Map;

import eu.fistar.sdcs.pa.common.Observation;
import eu.fistar.sdcs.pa.da.zephyrbh.utils.TimeConverter;
import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ConnectListenerImpl;
import zephyr.android.BioHarnessBT.ConnectedEvent;
import zephyr.android.BioHarnessBT.PacketTypeRequest;
import zephyr.android.BioHarnessBT.ZephyrPacketArgs;
import zephyr.android.BioHarnessBT.ZephyrPacketEvent;
import zephyr.android.BioHarnessBT.ZephyrPacketListener;
import zephyr.android.BioHarnessBT.ZephyrProtocol;

/**
 * This class represents the listener that has to be passed to the Zephyr library at connection time.
 * It holds all the business logic to handle the reception of data packets as well as configuration
 * changes and packaging of received data that has to be pushed to Protocol Adapter.
 *
 * @author Marcello Morena
 * @author Alexandru Serbanati
 */
public class ZephyrBHConnectedListener extends ConnectListenerImpl {

    ZephyrBHDeviceAdapter deviceAdapter;

    private GeneralPacketInfo gpInfo = new GeneralPacketInfo();
    private ECGPacketInfo ecgInfoPacket = new ECGPacketInfo();
    private BreathingPacketInfo breathingInfoPacket = new  BreathingPacketInfo();
    private RtoRPacketInfo rToRInfoPacket = new RtoRPacketInfo();
    private AccelerometerPacketInfo accInfoPacket = new AccelerometerPacketInfo();

    private PacketTypeRequest rqPacketType = new PacketTypeRequest();

    private Map<String, String> config;
    private ZephyrProtocol protocol;
    private String devId;
    ZephyrBHDevice device;

    public ZephyrBHConnectedListener(ZephyrBHDeviceAdapter deviceAdapter, String devId, Map<String, String> config) {
        super(null, null);
        this.deviceAdapter = deviceAdapter;
        this.devId = devId;
        this.config = config;
    }

    @Override
    public void Connected(ConnectedEvent<BTClient> eventArgs) {

        // Enable all the interesting info in the config object
        rqPacketType.GP_ENABLE = ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_GENERAL));
        rqPacketType.ECG_ENABLE = ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_ECG));
        rqPacketType.BREATHING_ENABLE = ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_BREATHING));
        rqPacketType.ACCELEROMETER_ENABLE = ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_ACCELEROMETER));
        rqPacketType.RtoR_ENABLE = ZephyrBHConstants.CONFIG_ENABLE.equals(config.get(ZephyrBHConstants.CONFIG_NAME_RTOR));

        // Create a new protocol instance passing it the BTComms object and the configuration
        protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), rqPacketType);

        // Notify the Device Adapter's main class of the device connection (to let it register the new device with the Protocol Adapter)
        device = new ZephyrBHDevice(devId, eventArgs.getSource(), this);
        deviceAdapter.deviceConnected(device);

        // Add a listener for the packet receiving
        protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {

            /**
             * Callback invoked by BioHarness library when a packet is received from the device
             *
             * @param zephyrPacketEvent The object containing the received packet
             */
            @Override
            public void ReceivedPacket(ZephyrPacketEvent zephyrPacketEvent) {

                // Extract the received packet from the event and parse the attached data
                ZephyrPacketArgs msg = zephyrPacketEvent.getPacket();
                // byte crcStatus = msg.getCRCStatus();
                // byte rcvdBytes = msg.getNumRvcdBytes();
                int msgId = msg.getMsgID();
                byte [] dataArray = msg.getBytes();

                // Find out the packet type and do the right action
                switch (msgId) {
                    case ZephyrBHConstants.PACKET_TYPE_GENERAL:
                        processPacketGeneral(dataArray);
                        break;

                    case ZephyrBHConstants.PACKET_TYPE_ID_BREATHING:
                        processPacketBreath(dataArray);
                        break;

                    case ZephyrBHConstants.PACKET_TYPE_ECG:
                        processPacketEcg(dataArray);
                        break;

                    case ZephyrBHConstants.PACKET_TYPE_ID_R_TO_R:
                        processPacketRtor(dataArray);
                        break;

                    case ZephyrBHConstants.PACKET_TYPE_ID_ACCEL:
                        processPacketAccel(dataArray);
                        break;
                }
            }
        });
    }

    /**
     * Process all the info retrieved with the General Packet and send them individually to the DA
     *
     * @param dataArray
     *      The General Packet binary representation
     */
    private void processPacketGeneral(byte[] dataArray) {

        Observation tmpObs;

        // Extract timestamp
        long timestamp = TimeConverter.timeToEpoch(
                gpInfo.GetTSYear(dataArray),
                gpInfo.GetTSMonth(dataArray),
                gpInfo.GetTSDay(dataArray),
                gpInfo.GetMsofDay(dataArray)
        );

        // Extract and send Hearth Rate
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_HEART, new String[] {Double.toString(gpInfo.GetHeartRate(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Respiration Rate
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_RESPIRATION, new String[] {Double.toString(gpInfo.GetRespirationRate(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Skin Temperature
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_SKIN_TEMP, new String[] {Double.toString(gpInfo.GetSkinTemperature(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Posture
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_POSTURE, new String[] {Integer.toString(gpInfo.GetPosture(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send VMU
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_VMU, new String[] {Double.toString(gpInfo.GetVMU(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Peak Acceleration
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_PEAK_ACCELERATION, new String[] {Double.toString(gpInfo.GetPeakAcceleration(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Battery Voltage
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BATTERY_VOLTAGE, new String[] {Double.toString(gpInfo.GetBatteryVoltage(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Breathing Wave Amplitude
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BREATHING_WAVE_AMPLITUDE, new String[] {Double.toString(gpInfo.GetBreathingWaveAmplitude(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send ECG Amplitude
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ECG_AMPLITUDE, new String[] {Double.toString(gpInfo.GetECGAmplitude(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send ECG Noise
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ECG_NOISE, new String[] {Double.toString(gpInfo.GetECGNoise(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send X Axis Acc Min
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_XAXIS_ACC_MIN, new String[] {Double.toString(gpInfo.GetX_AxisAccnMin(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send X Axis Acc Peak
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_XAXIS_ACC_PEAK, new String[] {Double.toString(gpInfo.GetX_AxisAccnPeak(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Y Axis Acc Min
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_YAXIS_ACC_MIN, new String[] {Double.toString(gpInfo.GetY_AxisAccnMin(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Y Axis Acc Peak
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_YAXIS_ACC_PEAK, new String[] {Double.toString(gpInfo.GetY_AxisAccnPeak(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Z Axis Acc Min
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ZAXIS_ACC_MIN, new String[] {Double.toString(gpInfo.GetZ_AxisAccnMin(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Z Axis Acc Peak
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ZAXIS_ACC_PEAK, new String[] {Double.toString(gpInfo.GetZ_AxisAccnPeak(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Zephyr Sys Chan
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ZEPHYR_SYS_CHAN, new String[] {Integer.toString(gpInfo.GetZephyrSysChan(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send GSR
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_GSR, new String[] {Integer.toString(gpInfo.GetGSR(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send ROG Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ROG_STATUS, new String[] {Integer.toString((int) gpInfo.GetROGStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Alarm STS
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ALARM_STS, new String[] {Integer.toString((int) gpInfo.GetAlarmStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Worn Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_WORN_STATUS, new String[] {Integer.toString((int) gpInfo.GetWornStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send User Intf Button Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_USER_INTF_BTN_STATUS, new String[] {Integer.toString((int) gpInfo.GetUserIntfBtnStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send BH Sig Low Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BH_SIG_LOW_STATUS, new String[] {Integer.toString((int) gpInfo._GetBHSigLowStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send BH Sens Conn Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BH_SENS_CONN_STATUS, new String[] {Integer.toString((int) gpInfo.GetBHSensConnStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Extract and send Battery Status
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BATTERY_STATUS, new String[] {Integer.toString((int) gpInfo.GetBatteryStatus(dataArray))});
        tmpObs.setPhenomenonTime(timestamp);
        deviceAdapter.receivedMeasurement(tmpObs, device);

    }

    /**
     * Process all the info retrieved with the ECG Packet and send them to the DA
     *
     * @param dataArray
     *      The ECG Packet binary representation
     */
    private void processPacketEcg(byte[] dataArray) {

        Observation tmpObs;
        short[] samples;

        // Extract timestamp
        long timestamp = TimeConverter.timeToEpoch(
                ecgInfoPacket.GetTSYear(dataArray),
                ecgInfoPacket.GetTSMonth(dataArray),
                ecgInfoPacket.GetTSDay(dataArray),
                ecgInfoPacket.GetMsofDay(dataArray)
        );

        // Extract ECG Data
        samples = ecgInfoPacket.GetECGSamples(dataArray);
        String[] strSamples = new String[samples.length];

        // Convert short values to String
        for (int i = 0; i < samples.length; i++) {
            strSamples[i] = Short.toString(samples[i]);
        }

        // Create the Observation object
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ECG, strSamples);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_ECG_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);
    }

    /**
     * Process all the info retrieved with the Breathing Packet and send them to the DA
     *
     * @param dataArray
     *      The Breathing Packet binary representation
     */
    private void processPacketBreath(byte[] dataArray) {

        Observation tmpObs;
        short[] samples;

        // Extract timestamp
        long timestamp = TimeConverter.timeToEpoch(
                breathingInfoPacket.GetTSYear(dataArray),
                breathingInfoPacket.GetTSMonth(dataArray),
                breathingInfoPacket.GetTSDay(dataArray),
                breathingInfoPacket.GetMsofDay(dataArray)
        );

        // Extract Breathing Data
        samples = breathingInfoPacket.GetBreathingSamples(dataArray);
        String[] strSamples = new String[samples.length];

        // Convert short values to String
        for (int i = 0; i < samples.length; i++) {
            strSamples[i] = Short.toString(samples[i]);
        }

        // Create the Observation object
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_BREATHING, strSamples);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_BREATHING_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);
    }

    /**
     * Process all the info retrieved with the RtoR Packet and send them to the DA
     *
     * @param dataArray
     *      The RtoR Packet binary representation
     */
    private void processPacketRtor(byte[] dataArray) {

        Observation tmpObs;
        int[] samples;

        // Extract timestamp
        long timestamp = TimeConverter.timeToEpoch(
                rToRInfoPacket.GetTSYear(dataArray),
                rToRInfoPacket.GetTSMonth(dataArray),
                rToRInfoPacket.GetTSDay(dataArray),
                rToRInfoPacket.GetMsofDay(dataArray)
        );

        // Extract RtoR Data
        samples = rToRInfoPacket.GetRtoRSamples(dataArray);
        String[] strSamples = new String[samples.length];

        // Convert short values to String
        for (int i = 0; i < samples.length; i++) {
            strSamples[i] = Integer.toString(samples[i]);
        }

        // Create the Observation object
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_R_TO_R, strSamples);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_R_TO_R_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);
    }

    /**
     * Process all the info retrieved with the Acceleration Packet and send them to the DA
     *
     * @param dataArray
     *      The Acceleration Packet binary representation
     */
    private void processPacketAccel(byte[] dataArray) {

        Observation tmpObs;
        double[] samplesX;
        double[] samplesY;
        double[] samplesZ;

        // Extract timestamp
        long timestamp = TimeConverter.timeToEpoch(
                accInfoPacket.GetTSYear(dataArray),
                accInfoPacket.GetTSMonth(dataArray),
                accInfoPacket.GetTSDay(dataArray),
                accInfoPacket.GetMsofDay(dataArray)
        );

        // Extract Acceleration Data
        accInfoPacket.UnpackAccelerationData(dataArray);
        samplesX = accInfoPacket.GetX_axisAccnData();
        String[] strSamplesX = new String[samplesX.length];
        samplesY = accInfoPacket.GetY_axisAccnData();
        String[] strSamplesY = new String[samplesY.length];
        samplesZ = accInfoPacket.GetZ_axisAccnData();
        String[] strSamplesZ = new String[samplesZ.length];

        // Convert double values to String
        for (int i = 0; i < samplesX.length; i++) {
            strSamplesX[i] = Double.toString(samplesX[i]);
        }

        for (int i = 0; i < samplesY.length; i++) {
            strSamplesY[i] = Double.toString(samplesY[i]);
        }

        for (int i = 0; i < samplesZ.length; i++) {
            strSamplesZ[i] = Double.toString(samplesZ[i]);
        }

        // Create the Observation object for X Axis
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ACCELEROMETER_X, strSamplesX);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_ACCELEROMETER_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Create the Observation object for X Axis
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ACCELEROMETER_Y, strSamplesY);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_ACCELEROMETER_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);

        // Create the Observation object for X Axis
        tmpObs = new Observation(ZephyrBHConstants.SENSOR_ACCELEROMETER_Z, strSamplesZ);
        tmpObs.setPhenomenonTime(timestamp);
        tmpObs.setDuration(ZephyrBHConstants.SAMPLES_ACCELEROMETER_DURATION);

        // Send data to Device Adapter
        deviceAdapter.receivedMeasurement(tmpObs, device);
    }

    /**
     * Parse the command passed by Device Adapter and perform the desired operation
     *
     * @param command The command passed by Device Adapter
     */
    public void parseCommand(String command) {
        if (ZephyrBHConstants.COMMAND_ENABLE_GENERAL.equals(command)) {
            protocol.SetGeneralPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_GENERAL.equals(command)) {
            protocol.SetGeneralPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_ENABLE_ACCELEROMETER.equals(command)) {
            protocol.SetAccelerometerPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_ACCELEROMETER.equals(command)) {
            protocol.SetAccelerometerPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_ENABLE_BREATHING.equals(command)) {
            protocol.SetBreathingPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_BREATHING.equals(command)) {
            protocol.SetBreathingPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_ENABLE_ECG.equals(command)) {
            protocol.SetECGPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_ECG.equals(command)) {
            protocol.SetECGPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_ENABLE_RTOR.equals(command)) {
            protocol.SetRtoRPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_RTOR.equals(command)) {
            protocol.SetRtoRPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_ENABLE_LOGGING.equals(command)) {
            protocol.SetLoggingDataPacket(true);
        }
        else if (ZephyrBHConstants.COMMAND_DISABLE_LOGGING.equals(command)) {
            protocol.SetLoggingDataPacket(false);
        }
        else if (ZephyrBHConstants.COMMAND_SEND_LIFE_SIGN.equals(command)) {
            protocol.SendLifeSign();
        }
        else {
            throw new IllegalArgumentException("Command not supported by " + DiscoveryResponder.CapabilitiesConstants.CAP_FRIENDLY_NAME + "!");
        }
    }

}
