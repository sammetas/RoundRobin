package com.roundrobin.RoundRobin;

import com.roundrobin.RoundRobin.config.InstanceConfig;
import com.roundrobin.RoundRobin.model.Game;
import com.roundrobin.RoundRobin.model.InstanceSpeed;
import com.roundrobin.RoundRobin.service.RoundRobinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.mockito.Mockito.*;


public class RoundRobinServiceTest {

    @Mock
    List<String> instances;
    @Mock
    private InstanceConfig instanceConfig;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RoundRobinService roundRobinService;
    private int currentIndex;
    private int size;
    private Map<String, InstanceSpeed> apiSpeedMap;
    private long thresholdLatency;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
      //  roundRobinService = new RoundRobinService(instanceConfig, restTemplate);
        instances = new LinkedList<>(List.of("localhost:8081", "localhost:8082", "localhost:8083", "localhost:8084"));
        currentIndex = 0;
        thresholdLatency = 500;

    }

    @Test
    public void testForwardRequest_shouldReturnSuccess() {
        Game game = new Game("test", "test", 10L);
        ResponseEntity<Object> mockResponse = ResponseEntity.ok(game);
        //when(restTemplate.postForEntity(any(), any(), eq(Object.class))).thenReturn(mockResponse);
        verify(mock(RoundRobinService.class) ,times(1)).getCorrectAndWorkingInstance();
        when(roundRobinService.forwardRequestToSimpleService(game)).thenReturn(mockResponse);
        //  ResponseEntity<Object> actual = roundRobinService.forwardRequestToSimpleService(new Game("test","test",10L));

        //   Assertions.assertEquals(actual.getStatusCode(), HttpStatus.OK);
        //  Assertions.assertEquals(actual.getBody(),mockResponse.getBody());


    }


}
