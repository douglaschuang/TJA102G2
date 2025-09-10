package com.babymate.clinic.model;

public record ClinicDto(
    Integer clinicId, String clinicName, String clinicAddress,
    Double latitude, Double longitude, String department,
    String clinicPhone, Double distanceKm
) {}
