package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class ProvisioningController {

    private final ProvisioningServiceImpl provisioningService;

    public ProvisioningController(ProvisioningServiceImpl provisioningService) {
        this.provisioningService = provisioningService;
    }

    @GetMapping("/provisioning/{macAddress}")
    public ResponseEntity<String> getProvisioningFile(@PathVariable String macAddress) {
        String configContent = provisioningService.getProvisioningFile(macAddress);
        String contentType = provisioningService.getContentType(macAddress);

        MediaType mediaType = contentType.equals("application/json")
                ? MediaType.APPLICATION_JSON
                : MediaType.TEXT_PLAIN;

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(mediaType)
                .body(configContent);
    }
}
