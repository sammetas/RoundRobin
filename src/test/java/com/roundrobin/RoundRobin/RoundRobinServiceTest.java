package com.roundrobin.RoundRobin;

import com.roundrobin.RoundRobin.config.InstanceConfig;
import com.roundrobin.RoundRobin.model.Game;
import com.roundrobin.RoundRobin.model.InstanceSpeed;
import com.roundrobin.RoundRobin.service.RoundRobinService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;


public class RoundRobinServiceTest {

    public static final String HTTP_LOCALHOST_8081_API_V_1_PING = "http://localhost:8081/api/v1/ping";

    @Mock
    private InstanceConfig instanceConfig;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private RoundRobinService roundRobinService;
    private int currentIndex;
    private int size;
    private Map<String, InstanceSpeed> apiSpeedMap;
    private long thresholdLatency;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(instanceConfig.getInstances()).thenReturn(Arrays.asList("localhost:8081", "localhost:8082", "localhost:8083", "localhost:8084"));
        when(instanceConfig.getBaseUrl()).thenReturn("http://");
        when(instanceConfig.getSimpleApiUrl()).thenReturn("/api/v1/game");
        when(instanceConfig.getSimpleApiPing()).thenReturn("/ping");
        when(instanceConfig.getThresholdLatency()).thenReturn("500");
        roundRobinService.initializationOfService();

    }

    @Test
    void testGetCorrectAndWorkingInstance_Success() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok("Pong!"));
        Optional<String> instance = roundRobinService.getCorrectAndWorkingInstance();
        Assertions.assertTrue(instance.isPresent());
        verify(restTemplate, atLeastOnce()).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void test_isApiAliveAndHealthy_Success() {
        ResponseEntity<String> mockResponse = ResponseEntity.ok("Pong!");
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse);
        boolean result = roundRobinService.isApiAliveAndHealthy("localhost:8081");
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
        Assertions.assertEquals(result, true);

    }

    @Test
    void test_isApiAliveAndHealthy_ShouldNotSuccess() {

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.badRequest().build());
        boolean result = roundRobinService.isApiAliveAndHealthy("localhost:8081");
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(String.class));
        Assertions.assertEquals(result, false);

    }


    @Test
    public void testForwardRequest_shouldReturnSuccess() {
        Game game = new Game("test", "test", 10L);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok(game);
        ResponseEntity<String> mockResponse2 = ResponseEntity.ok("Pong!");
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse2);
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(mockResponse);
        Optional<String> expectedInstance = Optional.of("localhost:8081");
        when(roundRobinService.forwardRequestToSimpleService(game)).thenReturn(mockResponse);
        ResponseEntity<Object> actualResponse = roundRobinService.forwardRequestToSimpleService(game);
        Assertions.assertEquals(mockResponse, actualResponse);
    }


    @Test
    public void testForwardRequest_shouldNotReturnSuccess() {
        Game game = new Game("test", "test", 10L);
        ResponseEntity<String> mockResponse2 = ResponseEntity.ok("Pong!");
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(mockResponse2);
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(ResponseEntity.badRequest().build());
        Optional<String> expectedInstance = Optional.of("localhost:8081");
        when(roundRobinService.forwardRequestToSimpleService(game)).thenReturn(ResponseEntity.badRequest().build());
        ResponseEntity<Object> actualResponse = roundRobinService.forwardRequestToSimpleService(game);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void test_HealthCheckAndUpdateList() {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok("Pong!"));
        roundRobinService.healthCheckAndUpdateList();
        roundRobinService.isApiAliveAndHealthy("localhost:8081");
        Assertions.assertTrue(instanceConfig.getInstances().size() > 0);
    }


}
