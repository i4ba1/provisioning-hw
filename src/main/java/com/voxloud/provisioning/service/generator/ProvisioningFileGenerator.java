package com.voxloud.provisioning.service.generator;

import com.voxloud.provisioning.entity.Device;

public interface ProvisioningFileGenerator {
    String generateFile(Device device);
    Device.DeviceModel getSupportedModel();
    String getContentType();
}
