package com.voxloud.provisioning.service;

import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.service.generator.ProvisioningFileGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProvisioningServiceImpl implements ProvisioningService {

    private final DeviceRepository deviceRepository;
    private final List<ProvisioningFileGenerator> generators;
    private Map<Device.DeviceModel, ProvisioningFileGenerator> generatorMap;

    @PostConstruct
    public void init() {
        generatorMap = generators.stream()
                .collect(Collectors.toMap(
                        ProvisioningFileGenerator::getSupportedModel,
                        Function.identity()
                ));
    }

    @Override
    public String getProvisioningFile(String macAddress) {
        // Find device in inventory
        Device device = deviceRepository.findById(macAddress)
                .orElseThrow(() -> new DeviceNotFoundException(macAddress));

        // Get appropriate generator for device model
        ProvisioningFileGenerator generator = generatorMap.get(device.getModel());
        if (generator == null) {
            throw new RuntimeException(
                    "No generator found for device model: " + device.getModel());
        }

        // Generate and return the configuration file
        return generator.generateFile(device);
    }

    /**
     * Returns the content type for the given MAC address.
     * Used by controller to set proper HTTP headers.
     *
     * @param macAddress the device MAC address
     * @return the content type string
     */
    public String getContentType(String macAddress) {
        Device device = deviceRepository.findById(macAddress)
                .orElseThrow(() -> new DeviceNotFoundException(macAddress));

        ProvisioningFileGenerator generator = generatorMap.get(device.getModel());
        if (generator == null) {
            return "text/plain";
        }

        return generator.getContentType();
    }
}
