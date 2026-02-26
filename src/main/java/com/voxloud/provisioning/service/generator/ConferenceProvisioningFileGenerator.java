package com.voxloud.provisioning.service.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.voxloud.provisioning.config.ProvisioningConfiguration;
import com.voxloud.provisioning.entity.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class ConferenceProvisioningFileGenerator implements ProvisioningFileGenerator {

    private final ProvisioningConfiguration configuration;
    private final ObjectMapper objectMapper;

    @Override
    public String generateFile(Device device) {
        // Build base configuration map preserving order
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("username", device.getUsername());
        config.put("password", device.getPassword());
        config.put("domain", configuration.getDomain());
        config.put("port", configuration.getPort());
        config.put("codecs", configuration.getCodecsAsList());

        // Apply override fragment if present
        if (device.getOverrideFragment() != null && !device.getOverrideFragment().isEmpty()) {
            applyOverrideFragment(config, device.getOverrideFragment());
        }

        // Generate JSON format
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate JSON configuration", e);
        }
    }

    @Override
    public Device.DeviceModel getSupportedModel() {
        return Device.DeviceModel.CONFERENCE;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    private void applyOverrideFragment(Map<String, Object> config, String overrideFragment) {
        try {
            ObjectNode overrideNode = (ObjectNode) objectMapper.readTree(overrideFragment);
            overrideNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // Convert JsonNode to appropriate Java type
                if (value instanceof com.fasterxml.jackson.databind.node.TextNode) {
                    config.put(key, ((com.fasterxml.jackson.databind.node.TextNode) value).asText());
                } else if (value instanceof com.fasterxml.jackson.databind.node.IntNode) {
                    config.put(key, ((com.fasterxml.jackson.databind.node.IntNode) value).asInt());
                } else if (value instanceof com.fasterxml.jackson.databind.node.ArrayNode) {
                    config.put(key, objectMapper.convertValue(value, java.util.List.class));
                } else {
                    config.put(key, value);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON override fragment", e);
        }
    }
}
