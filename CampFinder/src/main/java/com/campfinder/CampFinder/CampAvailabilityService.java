package com.campfinder.CampFinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.List;
import java.util.HashMap;

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

    private List<String> previousAvailableParks = new ArrayList<>();
   
    private static final String BASE_URL = "https://reservations.ontarioparks.ca/api/availability/map";

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void checkAvailability() {
        String url = buildUrl(-2147483464, LocalDate.of(2024, 8, 31), LocalDate.of(2024, 9, 2));
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
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
        List<String> availableParks = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : mapLinkAvailabilities.entrySet()) {
            String parkId = entry.getKey();
            int availability = ((Integer) ((java.util.ArrayList) entry.getValue()).get(0));
           // System.out.println(mapLinkAvailabilities);
            
            if (availability == 0) {
                // Site is available, send notification
                String parkName = ParkNames.PARK_NAMES.getOrDefault(parkId, "Unknown Park");
                availableParks.add(parkName + " (ID: " + parkId + ")");
            }
        }
        if (!availableParks.equals(previousAvailableParks)) {
            sendNotification(availableParks);
            previousAvailableParks = new ArrayList<>(availableParks);
        }
    }

    public void checkCampsiteAvailability(int mapId, LocalDate startDate, LocalDate endDate) {
        String url = buildUrl(mapId, startDate, endDate);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        
        if (response != null) {
            Map<String, Object> resourceAvailabilities = (Map<String, Object>) response.get("resourceAvailabilities");
            
            for (Map.Entry<String, Object> entry : resourceAvailabilities.entrySet()) {
                String siteId = entry.getKey();
                Map<String, Object> availabilityInfo = (Map<String, Object>) ((java.util.ArrayList) entry.getValue()).get(0);
                int availability = (int) availabilityInfo.get("availability");
                
                if (availability == 0) {
                    // Site is available, send notification
                    //sendNotification(String.valueOf(mapId), siteId);
                }
            }
        }
    }

    private void sendNotification(List<String> availableParks) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        
        String messageBody = "The current available parks are:\n" + String.join("\n", availableParks);
        
        // TODO: Replace with actual recipient phone number

        Message message = Message.creator(
            new PhoneNumber(toPhoneNumber),
            new PhoneNumber(twilioPhoneNumber),
            messageBody)
        .create();
    
        System.out.println("Sent message SID: " + message.getSid());
    }

}