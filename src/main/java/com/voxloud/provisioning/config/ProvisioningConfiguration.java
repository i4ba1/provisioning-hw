package com.voxloud.provisioning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@Data
@ConfigurationProperties(prefix = "provisioning")
public class ProvisioningConfiguration {

    private String domain;
    private String port;
    private String codecs;

    public List<String> getCodecsAsList() {
        if (codecs == null || codecs.isEmpty()) {
            return List.of();
        }
        return List.of(codecs.split(","));
    }
}
