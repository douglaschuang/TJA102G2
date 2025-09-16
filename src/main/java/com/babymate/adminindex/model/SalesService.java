package com.babymate.adminindex.model;

import com.babymate.orders.model.OrdersRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class SalesService {

    private final OrdersRepository orderRepo;
    public SalesService(OrdersRepository orderRepo){ this.orderRepo = orderRepo; }

    public List<String> monthLabels() {
        ZoneId tz = ZoneId.of("Asia/Taipei");
        LocalDate start = LocalDate.now(tz).withDayOfMonth(1).minusMonths(6);
        List<String> labels = new ArrayList<>();
        for (int i=0; i<7; i++) {
            labels.add(start.plusMonths(i).toString().substring(0,7) + "-01");
        }
        return labels;
    }

    public List<Map<String,Object>> series() {
        ZoneId tz = ZoneId.of("Asia/Taipei");
        LocalDate firstOfThisMonth = LocalDate.now(tz).withDayOfMonth(1);
        LocalDate start = firstOfThisMonth.minusMonths(6);
        LocalDate end   = firstOfThisMonth.plusMonths(1);

        var rows = orderRepo.sumPaidAmountByMonth(start.atStartOfDay(), end.atStartOfDay());
        Map<String, Number> monthTotal = new LinkedHashMap<>();
        for (String label : monthLabels()) monthTotal.put(label, 0);

        for (Object[] r : rows) {
            String monthKey = (String) r[0];
            Number total    = (Number) r[1];
            monthTotal.put(monthKey, total);
        }
        return List.of(Map.of("name","已付款營收","data", new ArrayList<>(monthTotal.values())));
    }
}
