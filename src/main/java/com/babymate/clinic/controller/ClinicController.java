package com.babymate.clinic.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.babymate.clinic.model.ClinicDto;
import com.babymate.clinic.model.ClinicRepository;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    @Autowired ClinicRepository repo;

    @GetMapping("/departments")
    public List<String> departments() {
        return repo.findDepartments();
    }

    @GetMapping("/nearby")
    public List<ClinicDto> nearby(
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(defaultValue = "3") double radiusKm,
        @RequestParam(required = false) String department,
        @RequestParam(defaultValue = "distance") String sortBy,
        @RequestParam(defaultValue = "asc") String order
    ) {
        return repo.findNearby(lat, lng, radiusKm, department, sortBy, order);
    }
}
