package com.babymate.adminindex.model;

public record DashboardStats(
    long newOrdersToday,
    double bounceRate,
    long newUsersToday,
    long uniqueVisitors7d
) {}
