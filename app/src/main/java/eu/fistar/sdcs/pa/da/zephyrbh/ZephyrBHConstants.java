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

import eu.fistar.sdcs.pa.common.SensorDescription;

/**
 * This class groups together all the constants used inside the Zephyr BioHarness Device Adapter.
 * It can also be used to change sensor names or command names.
 *
 * @author Marcello Morena
 * @author Alexandru Serbanati
 */
public class ZephyrBHConstants {

    // BioHarness Info
    public static final String BH_MODEL_NAME = "BioHarness 3";
    public static final String BH_MANUFACTURER_NAME = "Zephyr";

    // IDs of various packets
    public static final int PACKET_TYPE_GENERAL = 0x20;
    public static final int PACKET_TYPE_ID_BREATHING = 0x21;
    public static final int PACKET_TYPE_ECG = 0x22;
    public static final int PACKET_TYPE_ID_R_TO_R = 0x24;
    public static final int PACKET_TYPE_ID_ACCEL = 0x2A;

    // BioHarness Sensor Names (sensorName, measurementUnit, propertyName)
    public static final SensorDescription SENSOR_HEART = new SensorDescription("pulsimeter", "bpm", "heart rate");
    public static final SensorDescription SENSOR_RESPIRATION = new SensorDescription("chest expansion and contraction sensor", "bpm", "respiration rate");
    public static final SensorDescription SENSOR_SKIN_TEMP = new SensorDescription("infrared thermometer", "celsius degrees", "temperature");
    public static final SensorDescription SENSOR_POSTURE = new SensorDescription("inclinometer", "angular degrees", "posture");
    public static final SensorDescription SENSOR_VMU = new SensorDescription("triaxial accelerometer", "g", "vmu");
    public static final SensorDescription SENSOR_PEAK_ACCELERATION = new SensorDescription("triaxial accelerometer", "g", "peak acceleration");
    public static final SensorDescription SENSOR_BATTERY_VOLTAGE = new SensorDescription("battery sensor", "V", "battery voltage");
    public static final SensorDescription SENSOR_BREATHING_WAVE_AMPLITUDE = new SensorDescription("chest expansion and contraction sensor", "ml", "breathing wave amplitude");
    public static final SensorDescription SENSOR_ECG_AMPLITUDE = new SensorDescription("ecg sensor", "mV", "ecg amplitude");
    public static final SensorDescription SENSOR_ECG_NOISE = new SensorDescription("ecg sensor", "mV", "ecg noise");
    public static final SensorDescription SENSOR_XAXIS_ACC_MIN = new SensorDescription("triaxial accelerometer", "g", "x-axis acceleration min");
    public static final SensorDescription SENSOR_XAXIS_ACC_PEAK = new SensorDescription("triaxial accelerometer", "g", "x-axis acceleration peak");
    public static final SensorDescription SENSOR_YAXIS_ACC_MIN = new SensorDescription("triaxial accelerometer", "g", "y-axis accelerometer");
    public static final SensorDescription SENSOR_YAXIS_ACC_PEAK = new SensorDescription("triaxial accelerometer", "g", "y-axis acceleration peak");
    public static final SensorDescription SENSOR_ZAXIS_ACC_MIN = new SensorDescription("triaxial accelerometer", "g", "z-axis accelerometer");
    public static final SensorDescription SENSOR_ZAXIS_ACC_PEAK = new SensorDescription("triaxial accelerometer", "g", "z-axis acceleration peak");
    public static final SensorDescription SENSOR_ZEPHYR_SYS_CHAN = new SensorDescription("unknown sensor", "unknown unit", "zephyr sys chan");
    public static final SensorDescription SENSOR_GSR = new SensorDescription("GSR sensor", "microSiemens", "galvanic skin response");
    public static final SensorDescription SENSOR_ROG_STATUS = new SensorDescription("rog sensor", "unknown unit", "rog status");
    public static final SensorDescription SENSOR_ALARM_STS = new SensorDescription("unknown sensor", "unknown unit", "alarm sts");
    public static final SensorDescription SENSOR_WORN_STATUS = new SensorDescription("unknown sensor", "unknown unit", "worn status");
    public static final SensorDescription SENSOR_USER_INTF_BTN_STATUS = new SensorDescription("unknown sensor", "unknown unit", "user intf btn status");
    public static final SensorDescription SENSOR_BH_SIG_LOW_STATUS = new SensorDescription("unknown sensor", "unknown unit", "bh sig low status");
    public static final SensorDescription SENSOR_BH_SENS_CONN_STATUS = new SensorDescription("unknown sensor", "unknown unit", "bh sens conn status");
    public static final SensorDescription SENSOR_BATTERY_STATUS = new SensorDescription("battery sensor", "%", "battery status");
    public static final SensorDescription SENSOR_ECG = new SensorDescription("ecg sensor", "mV", "ecg");
    public static final SensorDescription SENSOR_BREATHING = new SensorDescription("unknown sensor", "unknown unit", "breathing");
    public static final SensorDescription SENSOR_ACCELEROMETER_X = new SensorDescription("triaxial accelerometer", "g", "accelerometer x");
    public static final SensorDescription SENSOR_ACCELEROMETER_Y = new SensorDescription("triaxial accelerometer", "g", "accelerometer y");
    public static final SensorDescription SENSOR_ACCELEROMETER_Z = new SensorDescription("triaxial accelerometer", "g", "accelerometer z");
    public static final SensorDescription SENSOR_R_TO_R = new SensorDescription("ecg sensor", "s", "r to r");
    public static final List<SensorDescription> SENSOR_LIST;
    static {
        List<SensorDescription> tmpSensList = new ArrayList<SensorDescription>();
        tmpSensList.add(ZephyrBHConstants.SENSOR_HEART);
        tmpSensList.add(ZephyrBHConstants.SENSOR_RESPIRATION);
        tmpSensList.add(ZephyrBHConstants.SENSOR_SKIN_TEMP);
        tmpSensList.add(ZephyrBHConstants.SENSOR_POSTURE);
        tmpSensList.add(ZephyrBHConstants.SENSOR_VMU);
        tmpSensList.add(ZephyrBHConstants.SENSOR_PEAK_ACCELERATION);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BATTERY_VOLTAGE);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BREATHING_WAVE_AMPLITUDE);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ECG_AMPLITUDE);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ECG_NOISE);
        tmpSensList.add(ZephyrBHConstants.SENSOR_XAXIS_ACC_MIN);
        tmpSensList.add(ZephyrBHConstants.SENSOR_XAXIS_ACC_PEAK);
        tmpSensList.add(ZephyrBHConstants.SENSOR_YAXIS_ACC_MIN);
        tmpSensList.add(ZephyrBHConstants.SENSOR_YAXIS_ACC_PEAK);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ZAXIS_ACC_MIN);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ZAXIS_ACC_PEAK);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ZEPHYR_SYS_CHAN);
        tmpSensList.add(ZephyrBHConstants.SENSOR_GSR);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ROG_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ALARM_STS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_WORN_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_USER_INTF_BTN_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BH_SIG_LOW_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BH_SENS_CONN_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BATTERY_STATUS);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ECG);
        tmpSensList.add(ZephyrBHConstants.SENSOR_BREATHING);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ACCELEROMETER_X);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ACCELEROMETER_Y);
        tmpSensList.add(ZephyrBHConstants.SENSOR_ACCELEROMETER_Z);
        tmpSensList.add(ZephyrBHConstants.SENSOR_R_TO_R);
        SENSOR_LIST = tmpSensList;
    }

    // Duration in milliseconds of sample acquisition
    public static final long SAMPLES_ECG_DURATION = 252;
    public static final long SAMPLES_BREATHING_DURATION = 1008;
    public static final long SAMPLES_R_TO_R_DURATION = 1008;
    public static final long SAMPLES_ACCELEROMETER_DURATION = 400;

    // Zephyr Device Adapter Commands
    public static final String COMMAND_ENABLE_GENERAL = "enableGeneralData";
    public static final String COMMAND_DISABLE_GENERAL = "disableGeneralData";
    public static final String COMMAND_ENABLE_ACCELEROMETER = "enableAccelerometerData";
    public static final String COMMAND_DISABLE_ACCELEROMETER = "disableAccelerometerData";
    public static final String COMMAND_ENABLE_BREATHING = "enableBreathingData";
    public static final String COMMAND_DISABLE_BREATHING = "disableBreathingData";
    public static final String COMMAND_ENABLE_ECG = "enableEcgData";
    public static final String COMMAND_DISABLE_ECG = "disableEcgData";
    public static final String COMMAND_ENABLE_RTOR = "enableRtoRData";
    public static final String COMMAND_DISABLE_RTOR = "disableRtoRData";
    public static final String COMMAND_ENABLE_LOGGING = "enableLoggingData";
    public static final String COMMAND_DISABLE_LOGGING = "disableLoggingData";
    public static final String COMMAND_SEND_LIFE_SIGN = "sendLifeSign";
    public static final List<String> COMMAND_LIST;
    static {
        List<String> tmpComm = new ArrayList<String>();
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_GENERAL);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_GENERAL);
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_ACCELEROMETER);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_ACCELEROMETER);
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_BREATHING);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_BREATHING);
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_ECG);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_ECG);
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_RTOR);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_RTOR);
        tmpComm.add(ZephyrBHConstants.COMMAND_ENABLE_LOGGING);
        tmpComm.add(ZephyrBHConstants.COMMAND_DISABLE_LOGGING);
        tmpComm.add(ZephyrBHConstants.COMMAND_SEND_LIFE_SIGN);
        COMMAND_LIST = tmpComm;
    }

    // Configuration related constants
    public static final String CONFIG_NAME_GENERAL = "GeneralPacket";
    public static final String CONFIG_NAME_ACCELEROMETER = "AccelerometerPacket";
    public static final String CONFIG_NAME_BREATHING = "BreathingPacket";
    public static final String CONFIG_NAME_ECG = "ECGPacket";
    public static final String CONFIG_NAME_RTOR = "RtoRPacket";
    public static final String CONFIG_NAME_LOGGING = "LoggingPacket";
    public static final String CONFIG_ENABLE = "enable";
    public static final String CONFIG_DISABLE = "disable";
}
