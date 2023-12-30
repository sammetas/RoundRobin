package com.roundrobin.RoundRobin.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "round.apis")
public class InstanceConfig {

    private List<String> instances;
    private String baseUrl;
    private String simpleApiUrl;
    private String simpleApiPing;
    private String thresholdLatency;
    private String sleepTime;
    private String fixedDelay;


}
