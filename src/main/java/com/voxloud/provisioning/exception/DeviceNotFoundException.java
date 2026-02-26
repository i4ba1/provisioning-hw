package com.voxloud.provisioning.exception;


public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(String macAddress) {
        super("Device not found with MAC address: " + macAddress);
    }

    public DeviceNotFoundException(String macAddress, Throwable cause) {
        super("Device not found with MAC address: " + macAddress, cause);
    }
}
