package com.roundrobin.RoundRobin;

import com.roundrobin.RoundRobin.controller.RoundRobinController;
import com.roundrobin.RoundRobin.model.Game;
import com.roundrobin.RoundRobin.service.RoundRobinService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RoundRobinControllerTest {

    RoundRobinController controller;
    RoundRobinService service;


    @BeforeEach
    public void initSetup() {
        service = Mockito.mock(RoundRobinService.class);
        controller = new RoundRobinController(service);
    }

    @Test
    public void shouldReturnHttpStatusCode200_forwardRequest() {
        Game game = new Game("Test1", "Test2", 10L);
        Mockito.when(controller.routeTheRequest(Mockito.any())).thenReturn(ResponseEntity.ok(Mockito.any()));
        ResponseEntity<Object> response = controller.routeTheRequest(game);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Mockito.verify(service, Mockito.times(1)).forwardRequestToSimpleService(ArgumentMatchers.any());
    }

    @Test
    public void shouldReturnHttpStatus500_notSuccessfulRequest() {
        Game game = new Game("Test1", "Test2", 10L);
        Mockito.when(controller.routeTheRequest(Mockito.any())).thenReturn(ResponseEntity.badRequest().build());
        ResponseEntity<Object> response = controller.routeTheRequest(game);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

}
