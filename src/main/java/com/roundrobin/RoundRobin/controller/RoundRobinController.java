package com.roundrobin.RoundRobin.controller;

import com.roundrobin.RoundRobin.service.RoundRobinService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
@EnableScheduling
public class RoundRobinController {

    private final RoundRobinService roundRobinService;


    public RoundRobinController(RoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @PostMapping("forward")
    public ResponseEntity<Object> routeTheRequest(@RequestBody Object gameObject) {
        return roundRobinService.forwardRequestToSimpleService(gameObject);

    }


}
