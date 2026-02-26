package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProvisioningController.class)
class ProvisioningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProvisioningServiceImpl provisioningService;

    @Test
    void getProvisioningFile_DeskDevice_ReturnsPlainText() throws Exception {
        String macAddress = "aa-bb-cc-dd-ee-ff";
        String expectedConfig = "username=john\npassword=doe\ndomain=sip.voxloud.com";

        when(provisioningService.getProvisioningFile(macAddress)).thenReturn(expectedConfig);
        when(provisioningService.getContentType(macAddress)).thenReturn("text/plain");

        mockMvc.perform(get("/api/v1/provisioning/{macAddress}", macAddress))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string(expectedConfig));
    }

    @Test
    void getProvisioningFile_ConferenceDevice_ReturnsJson() throws Exception {
        String macAddress = "f1-e2-d3-c4-b5-a6";
        String expectedConfig = "{\"username\":\"sofia\",\"password\":\"red\"}";

        when(provisioningService.getProvisioningFile(macAddress)).thenReturn(expectedConfig);
        when(provisioningService.getContentType(macAddress)).thenReturn("application/json");

        mockMvc.perform(get("/api/v1/provisioning/{macAddress}", macAddress))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedConfig));
    }

    @Test
    void getProvisioningFile_DeviceNotFound_Returns404() throws Exception {
        String macAddress = "00-00-00-00-00-00";

        when(provisioningService.getProvisioningFile(macAddress))
                .thenThrow(new com.voxloud.provisioning.exception.DeviceNotFoundException(macAddress));

        mockMvc.perform(get("/api/v1/provisioning/{macAddress}", macAddress))
                .andExpect(status().isNotFound());
    }
}
