package com.campfinder.CampFinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class CampFinderController {

    @Autowired
    private CampAvailabilityService campAvailabilityService;

    @GetMapping("/check-availability")
    public String checkAvailability(
            @RequestParam int mapId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
                return "Checking availability for mapId: " + mapId + 
                       ", from " + startDate + " to " + endDate;
            }
}