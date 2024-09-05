package com.campfinder.CampFinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CampFinderController {

    @Autowired
    private CampAvailabilityService campAvailabilityService;

    @GetMapping("/check-availability")
    public String checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<String> parks) {
        campAvailabilityService.startAvailabilityCheck(startDate, endDate, parks);
        return "Checking availability for: " + parks +
                ", from " + startDate + " to " + endDate;
    }

    @GetMapping("/stop-availability-check")
    public String stopAvailabilityCheck() {
        campAvailabilityService.stopAvailabilityCheck();
        return "Availability check stopped";
    }
}
