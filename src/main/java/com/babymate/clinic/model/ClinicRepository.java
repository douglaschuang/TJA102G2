package com.babymate.clinic.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ClinicRepository {
    @Autowired JdbcTemplate jdbc;
    
    private static final RowMapper<ClinicDto> CLINIC_DTO_MAPPER = new RowMapper<ClinicDto>() {
        @Override
        public ClinicDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ClinicDto(
                rs.getInt("clinic_id"),
                rs.getString("clinic_name"),
                rs.getString("clinic_address"),
                (Double) rs.getObject("latitude") ,   // 允許為 null
                (Double) rs.getObject("longitude"),
                rs.getString("department"),
                rs.getString("clinic_phone"),
                (Double) rs.getObject("distanceKm")   // 單筆查詢時可為 null
            );
        }
    };

    
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
    
    
    /** 依 clinic_id 取單筆 DTO；查不到回傳 null */
    public ClinicDto findDtoById(Integer clinicId) {
        String sql = """
            SELECT clinic_id, clinic_name, clinic_address, latitude, longitude,
                   department, clinic_phone, NULL AS distanceKm
            FROM clinic
            WHERE clinic_id = ?
            LIMIT 1
        """;
        var list = jdbc.query(sql, CLINIC_DTO_MAPPER, clinicId);
        return list.isEmpty() ? null : list.get(0);
    }
}

