package com.roundrobin.RoundRobin.service;

import com.roundrobin.RoundRobin.config.InstanceConfig;
import com.roundrobin.RoundRobin.model.InstanceSpeed;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class RoundRobinService {


    private static final long FIXED_HEALTH_CHECK_DELAY = 5000L;
    private final InstanceConfig config;
    private final RestTemplate restTemplate;
    private int currentIndex;
    private int size;
    private List<String> instances;
    private Map<String, InstanceSpeed> apiSpeedMap;
    private long thresholdLatency;


    @Autowired
    public RoundRobinService(InstanceConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Object> forwardRequestToSimpleService(Object gameObject) {

        Optional<String> instanceName = getCorrectAndWorkingInstance();
        if (instanceName.isPresent()) {
            String url = generateTheSimpleApiURL(instanceName.get());
            ResponseEntity<Object> response = restTemplate.postForEntity(url, gameObject, Object.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("The request failed to service::" + instanceName);
            }
            log.info("This request is serviced by:" + instanceName.get());
            return response;
        }
        return ResponseEntity.badRequest().build();

    }

    /*
         RR Logic:
         1.Loops through the available instances (presumed initially that all configured all available)
         2.If any of the instances health is not proper then we skip and go to next available healthy instance.
         3. We do step 2 until we find a healthy instance.
         4.currentIndex is the key to round-robin the instances.
                i. we derive next instances based on the instance health and mod operator with size .
                ii. due to math.abs() usage even though int reaches it's limit, it will keep round-robin
     */
    public Optional<String> getCorrectAndWorkingInstance() {
        List<String> instancesTobeRemoved = new ArrayList<>();
        Optional<String> workingInstance = Optional.empty();

        for (int i = 0; i < instances.size(); i++) {
            int currentInstanceIndex = Math.abs(currentIndex++ % size);
            String currentInstance = instances.get(currentInstanceIndex);
            if (isApiAliveAndHealthy(currentInstance) && !InstanceSpeed.SLOWER.equals(apiSpeedMap.get(currentInstance))) {
                workingInstance = Optional.of(currentInstance);
                break;
            } else {
                instancesTobeRemoved.add(currentInstance);
            }
        }
        if (instancesTobeRemoved.size() > 0) {
            size -= instancesTobeRemoved.size();
            instances.removeAll(instancesTobeRemoved);
        }
        return workingInstance;
    }

    /* This method will just check ping and if status code is 2xx then returns true in all other cases returns false*/
    public boolean isApiAliveAndHealthy(String instance) {
        try {
            Instant startTime = Instant.now();
            ResponseEntity<String> response = restTemplate.getForEntity(generateTheSimpleApiPingURL(instance), String.class);
            long duration = Duration.between(startTime, Instant.now()).toMillis();
            if (duration >= thresholdLatency && duration < 2 * thresholdLatency) {
                updateApiSpeedMap(instance, InstanceSpeed.SLOW);
            } else if (duration >= 2 * thresholdLatency) {
                updateApiSpeedMap(instance, InstanceSpeed.SLOWER);
            }

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return false;
        }

    }

    private void updateApiSpeedMap(String instance, InstanceSpeed speed) {
        apiSpeedMap.put(instance, speed);
    }

    @PostConstruct
    public void initializationOfService() {
        currentIndex = 0;
        instances = Collections.synchronizedList(new LinkedList<>(config.getInstances()));
        instances.addAll(config.getInstances());
        size = instances.size();
        apiSpeedMap = new HashMap<>();
        instances.forEach(instance -> {
            apiSpeedMap.put(instance, InstanceSpeed.NORMAL); //Presume all instances are normal initially
        });
        thresholdLatency = Long.parseLong(this.config.getThresholdLatency());
    }

    private String generateTheSimpleApiURL(String instanceName) {
        return config.getBaseUrl() + instanceName + config.getSimpleApiUrl();
    }

    private String generateTheSimpleApiPingURL(String instanceName) {
        return config.getBaseUrl() + instanceName + config.getSimpleApiPing();
    }

    @Scheduled(fixedDelay = FIXED_HEALTH_CHECK_DELAY)
    public void healthCheckAndUpdateList() {
        try {
            List<String> originalConfiguredInstances = config.getInstances();
            List<String> instancesTobeAdded = originalConfiguredInstances
                    .stream().filter(instance ->
                            !instances.contains(instance) && isApiAliveAndHealthy(instance)).toList();
            addInstancesAndUpdateSpeedMap(instancesTobeAdded);
        } catch (Exception e) {
            log.error("Exception during the Schedule run:" + e);
        }

    }

    public void addInstancesAndUpdateSpeedMap(List<String> instancesTobeAdded) {
        if (!instancesTobeAdded.isEmpty()) {
            instances.addAll(instancesTobeAdded);
            size += instancesTobeAdded.size();
            instancesTobeAdded.forEach(instance -> apiSpeedMap.put(instance, InstanceSpeed.NORMAL));
        }
    }
}
