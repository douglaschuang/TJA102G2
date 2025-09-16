package com.babymate.adminindex.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import com.babymate.member.model.MemberRepository;
import com.babymate.orders.model.OrdersRepository;

@Service
public class DashboardService {

    public record DashboardStats(long newOrdersToday, double bounceRate, long newUsersToday, long uniqueVisitors7d) {}
    public record OrderRow(long id, String memberName, BigDecimal total, String status) {}
    public record MemberRow(String name, String email, LocalDateTime joinedAt) {}
    public record HotRow(Integer productId, String productName, long qty) {}

    private final OrdersRepository orderRepo;
    private final MemberRepository memberRepo;

    public DashboardService(OrdersRepository orderRepo, MemberRepository memberRepo) {
        this.orderRepo = orderRepo;
        this.memberRepo = memberRepo;
    }

    public DashboardStats fetchStats() {
        ZoneId tz = ZoneId.of("Asia/Taipei");
        LocalDate today = LocalDate.now(tz);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end   = today.plusDays(1).atStartOfDay();

        long ordersToday   = orderRepo.countByOrderTimeBetween(start, end);
        long newUsersToday = memberRepo.countByRegisterDateBetween(start, end);

        // 你目前沒有分析資料，先暫填
        double bounce = 53.0;
        long uv7d = 65;
        return new DashboardStats(ordersToday, bounce, newUsersToday, uv7d);
    }

    public List<OrderRow> latestOrders() {
        return orderRepo.findTop5ByOrderByOrderTimeDesc().stream()
            .map(o -> new OrderRow(
                o.getOrderId(),
                (o.getMemberVO()!=null ? o.getMemberVO().getName() : "未知會員"),
                // amount 若還是 Double 就先 new BigDecimal(o.getAmount().toString())
                (o.getAmount() instanceof BigDecimal bd ? bd : new BigDecimal(o.getAmount().toString())),
                o.getStatus()!=null && o.getStatus()==1 ? "已付款" : "待付款"
            ))
            .toList();
    }

    public List<MemberRow> latestMembers() {
        return memberRepo.findTop5ByOrderByRegisterDateDesc().stream()
            .map(m -> new MemberRow(
                m.getName(),
                m.getAccount(),          // ← 用 account 當 email
                m.getRegisterDate()
            ))
            .toList();
    }

    /** 以「熱銷 TOP5（近 30 天）」暫代「低庫存告警」 */
    public List<HotRow> hotProducts30d() {
        ZoneId tz = ZoneId.of("Asia/Taipei");
        LocalDateTime since = LocalDate.now(tz).minusDays(30).atStartOfDay();
        return orderRepo.topSellingProductsSince(since).stream()
            .map(r -> new HotRow((Integer) r[0], (String) r[1], ((Number) r[2]).longValue()))
            .toList();
    }
}
