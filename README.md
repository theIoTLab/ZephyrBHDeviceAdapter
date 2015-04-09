#Zephyr BioHarness Device Adapter

FI-STAR Phase: Beta  
Document version: 1.0 (draft)
Date: 05/03/2015

##What is the Zephyr BioHarness Device Adapter
The Zephyr BioHarness Device Adapter is a Device Adapter, a component of the Protocol Adapter (more about the Protocol Adapter [here](https://github.com/theIoTLab/ProtocolAdapterManager/blob/master/Protocol%20Adapter%20Guide.md)). It handles the BioHarness 3 devices manufactured by Zephyr. The BioHarness 3 is a compact sensor-packed device with focus on monitoring the activity and vital signs of a person. This Device Adapter uses the library BioHarnessBT.jar provided by Zephyr inside their SDK "BioHarness Bluetooth Developer Kit". You can have more info about the SDK and buy it visiting [their website](http://zephyranywhere.com/zephyr-labs/development-tools/).

**N.B.**: The Zephyr's library is **NOT** included in the project's sources, but it is **required** for it to compile and work correctly.

##Initial operations
Before you can start using the Device Adapter, there are some initial operations that you are required to perform.

###Installation of the Protocol Adapter
The first step for using the Device Adapter is installing the Protocol Adapter on the Android device where you are going to use it. To do this, just manually install the Protocol Adapter APK in the system.
The Protocol Adapter by itself is pretty useless, so you should install at least a Device Adapter, specifically the one (or ones) that handles the devices you want to work with.

###Installation of the Zephyr BioHarness Device Adapter
To install the Device Adapter in your system, you must first compile it from sources using Android Studio and include the required Zephyr's library BioHarnessBT.jar (more about this in the previous section). Then you can just manually install in the system the generated APK.

###First start
On startup, the Protocol Adapter performs a probe scan to discover all the Device Adapters on the system. Due to Android security policies, Device Adapters will not respond to Protocol Adapter’s probing (and therefore are not usable) unless they are manually started once after installation. To do this, just go to your Drawer (the list of installed applications), find the icon of the Device Adapter you just installed and tap on it. After this first time, there is no need to manually start the Device Adapters again.

###Devices
The Zephyr BioHarness Device Adapter works only with Bluetooth devices. However, due to Android restrictions, pairing is not provided by the Device Adapter. So, for you to be able to use a particular device, you have to manually pair the device and the smartphone before trying to use them together.

##Supported Features
The Zephyr BioHarness Device Adapter has, just like other Device Adapters, a list of Capabilities. Here you have them summarized:

* **Support for Blacklist**: YES - You **CAN** put some devices in blacklist to avoid unwanted connections
* **Support for Whitelist**: YES - You **CAN** put some devices in whitelist to connect only to trusted devices
* **Support for configuration via a GUI**: NO - You **CANNOT** configure this Device Adapter via the Protocol Adapter GUI configuration facility
* **Support for devices configuration**: YES STARTUP AND RUNTIME - You **CAN** set the configuration parameters for the devices at startup and at runtime
* **Support for sending commands to devices**: YES - You **CAN** send commands to devices
* **Support for devices detection and discovery**: NO - You **CANNOT** perform a discovery for nearby devices
* **Previous pairing needed**: YES - You **HAVE TO** perform the pairing between the smartphone and the device you are going to use, before you can use it
* **Can monitor device disconnection**: YES - You **CAN** be notified of device's disconnection
* **Is connection initiator**: YES - The Device Adapter **INITIATE** the connection with the devices, so you **MUST** manually perform a connection with the device when you want to use it
* **Support for available devices**: YES - You **CAN** retrieve the list of devices paired and managed by the Device Adapter

##Supported Commands
* **enableGeneralData** - Enable sending of the General Data Packet
* **disableGeneralData** - Disable sending of the General Data Packet
* **enableAccelerometerData** - Enable sending of the Acceleration Data Packet
* **disableAccelerometerData** - Disable sending of the Acceleration Data Packet
* **enableBreathingData** - Enable sending of the Breathing Data Packet
* **disableBreathingData** - Disable sending of the Breathing Data Packet
* **enableEcgData** - Enable sending of the ECG Data Packet
* **disableEcgData** - Disable sending of the ECG Data Packet
* **enableRtoRData** - Enable sending of the R to R Data Packet
* **disableRtoRData** - Disable sending of the R to R Data Packet
* **enableLoggingData** - Enable Logging on device
* **disableLoggingData** - Disable Logging on device
* **sendLifeSign** - Sends a Life Sign Packet to device

##Configuration Parameters
Each of this parameters can have the value of `enable` or `disable`:

* **GeneralPacket** - Toggle the sending of General Data Packet
* **AccelerometerPacket** - Toggle the sending of Accelerometer Data Packet
* **BreathingPacket** - Toggle the sending of Breathing Data Packet
* **ECGPacket** - Toggle the sending of ECG Data Packet
* **RtoRPacket** - Toggle the sending of R to R Data Packet
* **LoggingPacket** - Toggle the logging on device

## Authors, Contact and Contributions
As the licence reads, this is free software released by Consorzio Roma Ricerche. The authors (Marcello Morena and Alexandru Serbanati) will continuously add support for even more medical devices, but external contributions are welcome. Please have a look at the TODO file on what we are working on and contact us (protocoladapter[at]gmail[dot]com) if you plan on contributing.

## Acknowledgement
This work was carried out with the support of the FI-STAR project (“Future Internet Social and Technological Alignment Research”), an integrated project funded by the European Commission through the 7th ICT - Framework Programme (318389).