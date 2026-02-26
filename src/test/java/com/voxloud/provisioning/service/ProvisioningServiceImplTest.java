package com.voxloud.provisioning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.config.ProvisioningConfiguration;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.exception.DeviceNotFoundException;
import com.voxloud.provisioning.repository.DeviceRepository;
import com.voxloud.provisioning.service.generator.ConferenceProvisioningFileGenerator;
import com.voxloud.provisioning.service.generator.DeskProvisioningFileGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private ProvisioningConfiguration configuration;

    private ProvisioningServiceImpl provisioningService;

    @BeforeEach
    void setUp() {
        // Create generators
        DeskProvisioningFileGenerator deskGenerator = new DeskProvisioningFileGenerator(configuration);
        ConferenceProvisioningFileGenerator conferenceGenerator = new ConferenceProvisioningFileGenerator(configuration, new ObjectMapper());

        // Create service with both generators
        provisioningService = new ProvisioningServiceImpl(deviceRepository,
                Arrays.asList(deskGenerator, conferenceGenerator));
        provisioningService.init();
    }

    @Test
    void getProvisioningFile_DeskDevice_ReturnsPropertyFormat() {
        // Given
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-dd-ee-ff");
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername("john");
        device.setPassword("doe");
        device.setOverrideFragment(null);

        when(deviceRepository.findById("aa-bb-cc-dd-ee-ff")).thenReturn(Optional.of(device));
        when(configuration.getDomain()).thenReturn("sip.voxloud.com");
        when(configuration.getPort()).thenReturn("5060");
        when(configuration.getCodecs()).thenReturn("G711,G729,OPUS");

        // When
        String result = provisioningService.getProvisioningFile("aa-bb-cc-dd-ee-ff");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("username=john"));
        assertTrue(result.contains("password=doe"));
        assertTrue(result.contains("domain=sip.voxloud.com"));
        assertTrue(result.contains("port=5060"));
        assertTrue(result.contains("codecs=G711,G729,OPUS"));
    }

    @Test
    void getProvisioningFile_ConferenceDevice_ReturnsJsonFormat() {
        // Given
        Device device = new Device();
        device.setMacAddress("f1-e2-d3-c4-b5-a6");
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setUsername("sofia");
        device.setPassword("red");
        device.setOverrideFragment(null);

        when(deviceRepository.findById("f1-e2-d3-c4-b5-a6")).thenReturn(Optional.of(device));
        when(configuration.getDomain()).thenReturn("sip.voxloud.com");
        when(configuration.getPort()).thenReturn("5060");
        when(configuration.getCodecsAsList()).thenReturn(Arrays.asList("G711", "G729", "OPUS"));

        // When
        String result = provisioningService.getProvisioningFile("f1-e2-d3-c4-b5-a6");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"username\":\"sofia\""));
        assertTrue(result.contains("\"password\":\"red\""));
        assertTrue(result.contains("\"domain\":\"sip.voxloud.com\""));
        assertTrue(result.contains("\"port\":\"5060\""));
        assertTrue(result.contains("\"codecs\":[\"G711\",\"G729\",\"OPUS\"]"));
    }

    @Test
    void getProvisioningFile_DeskDeviceWithOverride_AppliesOverride() {
        // Given
        Device device = new Device();
        device.setMacAddress("a1-b2-c3-d4-e5-f6");
        device.setModel(Device.DeviceModel.DESK);
        device.setUsername("walter");
        device.setPassword("white");
        device.setOverrideFragment("domain=sip.anotherdomain.com\nport=5161\ntimeout=10");

        when(deviceRepository.findById("a1-b2-c3-d4-e5-f6")).thenReturn(Optional.of(device));
        when(configuration.getDomain()).thenReturn("sip.voxloud.com");
        when(configuration.getPort()).thenReturn("5060");
        when(configuration.getCodecs()).thenReturn("G711,G729,OPUS");

        // When
        String result = provisioningService.getProvisioningFile("a1-b2-c3-d4-e5-f6");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("username=walter"));
        assertTrue(result.contains("password=white"));
        assertTrue(result.contains("domain=sip.anotherdomain.com")); // overridden
        assertTrue(result.contains("port=5161")); // overridden
        assertTrue(result.contains("timeout=10")); // added from override
        assertTrue(result.contains("codecs=G711,G729,OPUS")); // from config
    }

    @Test
    void getProvisioningFile_ConferenceDeviceWithOverride_AppliesOverride() {
        // Given
        Device device = new Device();
        device.setMacAddress("1a-2b-3c-4d-5e-6f");
        device.setModel(Device.DeviceModel.CONFERENCE);
        device.setUsername("eric");
        device.setPassword("blue");
        device.setOverrideFragment("{\"domain\":\"sip.anotherdomain.com\",\"port\":\"5161\",\"timeout\":10}");

        when(deviceRepository.findById("1a-2b-3c-4d-5e-6f")).thenReturn(Optional.of(device));
        when(configuration.getDomain()).thenReturn("sip.voxloud.com");
        when(configuration.getPort()).thenReturn("5060");
        when(configuration.getCodecsAsList()).thenReturn(Arrays.asList("G711", "G729", "OPUS"));

        // When
        String result = provisioningService.getProvisioningFile("1a-2b-3c-4d-5e-6f");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("\"username\":\"eric\""));
        assertTrue(result.contains("\"password\":\"blue\""));
        assertTrue(result.contains("\"domain\":\"sip.anotherdomain.com\"")); // overridden
        assertTrue(result.contains("\"port\":\"5161\"")); // overridden
        assertTrue(result.contains("\"timeout\":10")); // added from override
        assertTrue(result.contains("\"codecs\":[\"G711\",\"G729\",\"OPUS\"]")); // from config
    }

    @Test
    void getProvisioningFile_DeviceNotFound_ThrowsException() {
        // Given
        when(deviceRepository.findById("00-00-00-00-00-00")).thenReturn(Optional.empty());

        // Then
        assertThrows(DeviceNotFoundException.class, () -> {
            provisioningService.getProvisioningFile("00-00-00-00-00-00");
        });
    }

    @Test
    void getContentType_DeskDevice_ReturnsTextPlain() {
        // Given
        Device device = new Device();
        device.setMacAddress("aa-bb-cc-dd-ee-ff");
        device.setModel(Device.DeviceModel.DESK);

        when(deviceRepository.findById("aa-bb-cc-dd-ee-ff")).thenReturn(Optional.of(device));

        // When
        String contentType = provisioningService.getContentType("aa-bb-cc-dd-ee-ff");

        // Then
        assertEquals("text/plain", contentType);
    }

    @Test
    void getContentType_ConferenceDevice_ReturnsApplicationJson() {
        // Given
        Device device = new Device();
        device.setMacAddress("f1-e2-d3-c4-b5-a6");
        device.setModel(Device.DeviceModel.CONFERENCE);

        when(deviceRepository.findById("f1-e2-d3-c4-b5-a6")).thenReturn(Optional.of(device));

        // When
        String contentType = provisioningService.getContentType("f1-e2-d3-c4-b5-a6");

        // Then
        assertEquals("application/json", contentType);
    }
}
