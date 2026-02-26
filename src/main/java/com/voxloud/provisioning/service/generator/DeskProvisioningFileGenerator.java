package com.voxloud.provisioning.service.generator;

import com.voxloud.provisioning.config.ProvisioningConfiguration;
import com.voxloud.provisioning.entity.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;


@Component
@RequiredArgsConstructor
public class DeskProvisioningFileGenerator implements ProvisioningFileGenerator {

    private final ProvisioningConfiguration configuration;

    @Override
    public String generateFile(Device device) {
        // Build base configuration map preserving order
        Map<String, String> config = new LinkedHashMap<>();
        config.put("username", device.getUsername());
        config.put("password", device.getPassword());
        config.put("domain", configuration.getDomain());
        config.put("port", configuration.getPort());
        config.put("codecs", configuration.getCodecs());

        // Apply override fragment if present
        if (device.getOverrideFragment() != null && !device.getOverrideFragment().isEmpty()) {
            applyOverrideFragment(config, device.getOverrideFragment());
        }

        // Generate property file format
        return buildPropertyFile(config);
    }

    @Override
    public Device.DeviceModel getSupportedModel() {
        return Device.DeviceModel.DESK;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    private void applyOverrideFragment(Map<String, String> config, String overrideFragment) {
        Properties overrideProps = new Properties();
        try (StringReader reader = new StringReader(overrideFragment)) {
            overrideProps.load(reader);
        } catch (IOException e) {
            // Should not happen with StringReader
            throw new RuntimeException("Failed to parse override fragment", e);
        }

        // Apply overrides - replace existing keys or add new ones
        overrideProps.forEach((key, value) -> config.put((String) key, (String) value));
    }

    private String buildPropertyFile(Map<String, String> config) {
        StringWriter writer = new StringWriter();
        config.forEach((key, value) -> {
            writer.write(key);
            writer.write("=");
            writer.write(value != null ? value : "");
            writer.write("\n");
        });
        return writer.toString().trim();
    }
}
