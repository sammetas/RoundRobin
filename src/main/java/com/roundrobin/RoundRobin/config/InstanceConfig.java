package com.roundrobin.RoundRobin.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
@Setter
@Getter
@ConfigurationProperties(prefix = "round.apis")
public class InstanceConfig {

    private LinkedList<String> instances;
    private String baseUrl;
    private String simpleApiUrl;
    private String simpleApiPing;
    private String thresholdLatency;
    private String sleepTime;


}
