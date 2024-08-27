package com.campfinder.CampFinder;

import java.util.HashMap;
import java.util.Map;

public class ParkNames {
    public static final Map<String, String> PARK_NAMES = new HashMap<>();
    
    static {
        PARK_NAMES.put("-2147483460", "Algonquin Park");
        PARK_NAMES.put("-2147483459", "Southeast Parks");
        PARK_NAMES.put("-2147483461", "Southwest & Central Parks");
        PARK_NAMES.put("-2147483462", "Near North Parks");
        PARK_NAMES.put("-2147483463", "Northern Parks");
    }
}