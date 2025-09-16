package com.babymate.clinic.model;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class ClinicRepository {
    @Autowired JdbcTemplate jdbc;

    public List<String> findDepartments() {
        String sql = """
            SELECT DISTINCT TRIM(department) AS dept
            FROM clinic
            WHERE department IS NOT NULL AND TRIM(department) <> ''
            ORDER BY dept ASC
        """;
        return jdbc.query(sql, (rs, i) -> rs.getString("dept"));
    }

    public List<ClinicDto> findNearby(
        double lat, double lng, double radiusKm, String dept,
        String sortBy, String order
    ) {
        double latDelta = radiusKm / 111.32;
        double lngDelta = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));

        String sql = """
            SELECT clinic_id, clinic_name, clinic_address, latitude, longitude, department, clinic_phone,
                   (6371 * ACOS(
                       COS(RADIANS(?)) * COS(RADIANS(latitude)) *
                       COS(RADIANS(longitude) - RADIANS(?)) +
                       SIN(RADIANS(?)) * SIN(RADIANS(latitude))
                   )) AS distanceKm
            FROM clinic
            WHERE latitude BETWEEN ? AND ?
              AND longitude BETWEEN ? AND ?
        """;
        List<Object> args = new ArrayList<>(List.of(
            lat, lng, lat,
            lat - latDelta, lat + latDelta,
            lng - lngDelta, lng + lngDelta
        ));

        if (dept != null && !dept.isBlank()) {
            sql += " AND department = ? ";
            args.add(dept);
        }

        // 只允許距離排序；名稱排序已移除
        String sortDir = "ASC";
        if ("desc".equalsIgnoreCase(order)) sortDir = "DESC";

        sql += " HAVING distanceKm <= ? ORDER BY distanceKm " + sortDir + " LIMIT 200";
        args.add(radiusKm);

        return jdbc.query(sql, args.toArray(), (rs, i) -> new ClinicDto(
            rs.getInt("clinic_id"),
            rs.getString("clinic_name"),
            rs.getString("clinic_address"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("department"),
            rs.getString("clinic_phone"),
            rs.getDouble("distanceKm")
        ));
    }
    
    public ClinicDto findDtoById(Integer id) {
        String sql = """
            SELECT clinic_id, clinic_name, clinic_address,
                   latitude, longitude, department, clinic_phone
            FROM clinic
            WHERE clinic_id = ?
        """;

        return jdbc.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new ClinicDto(
            rs.getInt("clinic_id"),
            rs.getString("clinic_name"),
            rs.getString("clinic_address"),
            rs.getDouble("latitude"),
            rs.getDouble("longitude"),
            rs.getString("department"),
            rs.getString("clinic_phone"),
            null // distanceKm 不需要時可為 null
        ));
    }
    
}

