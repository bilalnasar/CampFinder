package com.campfinder.CampFinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.time.LocalDate;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class CampAvailabilityService {

    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    @Value("${twilio.to.phone.number}")
    private String toPhoneNumber;

    @Autowired
    private RestTemplate restTemplate;

    private Map<String, List<String>> previousAvailableParks = new HashMap<>();

    private static final String BASE_URL = "https://reservations.ontarioparks.ca/api/availability/map";

    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> parks;
    private boolean isCheckingAvailability = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledTask;

    public void startAvailabilityCheck(LocalDate startDate, LocalDate endDate, List<String> parks) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.parks = parks;

        if (isCheckingAvailability) {
            stopAvailabilityCheck();
        }

        isCheckingAvailability = true;
        scheduledTask = scheduler.scheduleAtFixedRate(this::checkAvailability, 0, 50, TimeUnit.SECONDS);
    }

    public void stopAvailabilityCheck() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        isCheckingAvailability = false;
    }

    private HttpEntity<String> createHttpEntityWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
        headers.set("Accept-Language", "en-US,en;q=0.5");
        headers.set("Connection", "keep-alive");

        return new HttpEntity<>(headers);
    }

    public void checkAvailability() {
        if (startDate == null || endDate == null) {
            return; // Don't run if dates are not set
        }
        String url = buildUrl(-2147483464, startDate, endDate);
        HttpEntity<String> entity = createHttpEntityWithHeaders();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> response = responseEntity.getBody();

        if (response != null) {
            processResponse(response);
        }
    }

    private String buildUrl(int mapId, LocalDate startDate, LocalDate endDate) {
        return UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("mapId", mapId)
                .queryParam("bookingCategoryId", 0)
                .queryParam("equipmentCategoryId", -32768)
                .queryParam("subEquipmentCategoryId", -32768)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .queryParam("getDailyAvailability", false)
                .queryParam("isReserving", true)
                .queryParam("partySize", 5)
                .toUriString();
    }

    private void processResponse(Map<String, Object> response) {
        Map<String, Object> mapLinkAvailabilities = (Map<String, Object>) response.get("mapLinkAvailabilities");
        Map<String, List<String>> availableParks = new HashMap<>();

        for (Map.Entry<String, Object> entry : mapLinkAvailabilities.entrySet()) {
            String parkId = entry.getKey();
            int availability = ((Integer) ((java.util.ArrayList) entry.getValue()).get(0));

            if (availability == 0) {
                // Site is available, send notification
                String parkName = ParkNames.PARK_NAMES.getOrDefault(parkId, "Unknown Park");
                if ("Near North Parks".equals(parkName)) {
                    processParks(availableParks, -2147483462, parkName);
                } else if ("Southeast Parks".equals(parkName)) {
                    processParks(availableParks, -2147483459, parkName);
                } else if ("Algonquin Park".equals(parkName)) {
                    processParks(availableParks, -2147483460, parkName);
                } else if ("Southwest & Central Parks".equals(parkName)) {
                    processParks(availableParks, -2147483461, parkName);
                } else if ("Northern Parks".equals(parkName)) {
                } else {
                    availableParks.computeIfAbsent("Other Parks", k -> new ArrayList<>()).add(parkName);
                }
            }
        }
        if (!availableParks.equals(previousAvailableParks)) {
            if (!availableParks.isEmpty()) {
                sendNotification(availableParks);
            }
            previousAvailableParks = new HashMap<>(availableParks);
        }
    }

    private void processParks(Map<String, List<String>> availableParks, int mapId, String parentPark) {
        String url = buildUrl(mapId, startDate, endDate);
        HttpEntity<String> entity = createHttpEntityWithHeaders();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map<String, Object> response = responseEntity.getBody();

        if (response != null) {
            Map<String, Object> mapLinkAvailabilities = (Map<String, Object>) response.get("mapLinkAvailabilities");

            for (Map.Entry<String, Object> entry : mapLinkAvailabilities.entrySet()) {
                String parkId = entry.getKey();
                int availability = ((Integer) ((java.util.ArrayList) entry.getValue()).get(0));

                if (availability == 0) {
                    String parkName = ParkNames.PARK_NAMES.getOrDefault(parkId, "Unknown Park");
                    if (parentPark.equals("Algonquin Park") && parkName.equals("Unknown Park")) {
                        parkName = "Backcountry";
                    }
                    if (parks != null && !parks.isEmpty() && !parks.contains(parkName)) {
                        continue;
                    }
                    availableParks.computeIfAbsent(parentPark, k -> new ArrayList<>()).add(parkName);
                }
            }
        }
    }

    private void sendNotification(Map<String, List<String>> availableParks) {
        Twilio.init(twilioAccountSid, twilioAuthToken);

        StringBuilder messageBody = new StringBuilder("The current available parks are:\n");
        for (Map.Entry<String, List<String>> entry : availableParks.entrySet()) {
            messageBody.append(entry.getKey()).append(":\n");
            for (String park : entry.getValue()) {
                messageBody.append("  - ").append(park).append("\n");
            }
        }

        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                messageBody.toString())
                .create();

        System.out.println("Sent message SID: " + message.getSid());
    }

}