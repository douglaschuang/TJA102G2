package com.babymate.growth;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class GrowthStandardService {

    public Map<String, List<Double>> getHeightPercentiles(String gender) {
        // gender = "male" 或 "female"
        if ("male".equalsIgnoreCase(gender)) {
            return Map.of(
                "P3", List.of(46.3, 63.6, 71.3, 77.2),
                "P15", List.of(47.9, 65.4, 73.3, 79.5),
                "P50", List.of(49.9, 67.6, 75.7, 82.3),
                "P85", List.of(51.8, 69.8, 78.2, 85.1),
                "P97", List.of(53.4, 71.6, 80.2, 87.3)
            );
        } else {
            return Map.of(
                "P3", List.of(45.6, 61.5, 69.2, 75.2),
                "P15", List.of(47.2, 63.4, 71.3, 77.7),
                "P50", List.of(49.1, 65.7, 74.0, 80.7),
                "P85", List.of(51.1, 68.1, 76.7, 83.7),
                "P97", List.of(52.7, 70.0, 78.9, 86.2)
            );
        }
    }

    public Map<String, List<Double>> getWeightPercentiles(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return Map.of(
                "P3", List.of(2.5, 6.4, 7.8, 8.9),
                "P15", List.of(2.9, 7.1, 8.6, 9.7),
                "P50", List.of(3.3, 7.9, 9.6, 10.9),
                "P85", List.of(3.9, 8.9, 10.8, 12.3),
                "P97", List.of(4.3, 9.7, 11.8, 13.5)
            );
        } else {
            return Map.of(
                "P3", List.of(2.4, 5.8, 7.1, 8.2),
                "P15", List.of(2.8, 6.4, 7.9, 9.0),
                "P50", List.of(3.2, 7.3, 8.9, 10.2),
                "P85", List.of(3.7, 8.3, 10.2, 11.6),
                "P97", List.of(4.2, 9.2, 11.3, 13.0)
            );
        }
    }

    public Map<String, List<Double>> getHcPercentiles(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return Map.of(
                "P3", List.of(32.0, 41.0, 43.5, 44.7),
                "P15", List.of(33.0, 42.0, 44.7, 46.0),
                "P50", List.of(34.5, 43.0, 46.0, 47.4),
                "P85", List.of(36.0, 44.5, 47.5, 48.7),
                "P97", List.of(37.0, 45.5, 48.5, 50.0)
            );
        } else {
            return Map.of(
                "P3", List.of(31.8, 39.8, 42.2, 43.5),
                "P15", List.of(32.8, 40.8, 43.5, 44.8),
                "P50", List.of(34.0, 42.1, 45.0, 46.2),
                "P85", List.of(35.0, 43.5, 46.3, 47.8),
                "P97", List.of(36.0, 44.5, 47.5, 48.9)
            );
        }
    }
}

