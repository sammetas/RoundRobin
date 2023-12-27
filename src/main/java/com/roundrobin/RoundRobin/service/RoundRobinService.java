package com.roundrobin.RoundRobin.service;

import com.roundrobin.RoundRobin.config.InstanceConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class RoundRobinService {


    private final InstanceConfig config;
    private final RestTemplate restTemplate;
    private int currentIndex;
    private int size;
    private LinkedList<String> instances;


    @Autowired
    public RoundRobinService(InstanceConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Object> forwardRequestToSimpleService(Object gameObject) {

        Optional<String> instanceName = getCorrectAndWorkingInstance();
        if (instanceName.isPresent()) {
            String url = generateTheSimpleApiURL(instanceName.get());
            return restTemplate.postForEntity(url, gameObject, Object.class);
        }
        return ResponseEntity.badRequest().build();

    }

    private Optional<String> getCorrectAndWorkingInstance() {
        List<String> instancesTobeRemoved = new ArrayList<>();

        for (int i = 0; i < instances.size(); i++) {
            int currentInstance = Math.abs(currentIndex++ % size);
            if (isApiAliveAndHealthy(instances.get(currentInstance))) {
                return Optional.of(instances.get(currentInstance));
            } else {
                instancesTobeRemoved.add(instances.get(currentInstance));
            }

        }

        if (instancesTobeRemoved.size() > 0) {
            size -= instancesTobeRemoved.size();
            instances.removeAll(instancesTobeRemoved);
        }
        return Optional.of(null);
    }

    private boolean isApiAliveAndHealthy(String instance) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(generateTheSimpleApiPingURL(instance), String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return false;
        }

    }

    @PostConstruct
    public void updateInstancesWithPriorities() {
        System.out.println("--->" + config.getInstances());
        currentIndex = 0;
        instances = config.getInstances();
        size = instances.size();
    }

    private String generateTheSimpleApiURL(String instanceName) {
        return config.getBaseUrl() + instanceName + config.getSimpleApiUrl();
    }

    private String generateTheSimpleApiPingURL(String instanceName) {
        return config.getBaseUrl() + instanceName + config.getSimpleApiPing();
    }
}
