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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        campAvailabilityService.startAvailabilityCheck(startDate, endDate);
        return "Checking availability for mapId: " +
                ", from " + startDate + " to " + endDate;
    }

    @GetMapping("/stop-availability-check")
    public String stopAvailabilityCheck() {
        campAvailabilityService.stopAvailabilityCheck();
        return "Availability check stopped";
    }
}