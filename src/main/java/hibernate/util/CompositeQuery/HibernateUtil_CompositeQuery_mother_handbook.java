package hibernate.util.CompositeQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import com.babymate.mhb.model.MhbVO;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * 母子手冊的複合查詢（配合 Thymeleaf 表單的欄位名稱）
 *
 * 表單參數鍵（snake_case）：
 * - mother_handbook_id
 * - mother_name
 * - mother_birthday
 * - last_mc_date
 * - expected_birth_date
 * - weight
 *
 * 注意：Criteria API 取的是「Entity 屬性名」（camelCase）：
 * - motherHandbookId, motherName, motherBirthday, lastMcDate, expectedBirthDate, weight
 *
 * 交易/Session 交給 Spring 管（@Transactional + getCurrentSession），這裡不開關交易、不關 session。
 */
public class HibernateUtil_CompositeQuery_mother_handbook {

    public static List<MhbVO> getAllC(Map<String, String[]> map, Session session) {
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MhbVO> cq = cb.createQuery(MhbVO.class);
        Root<MhbVO> root = cq.from(MhbVO.class);

        List<Predicate> preds = new ArrayList<>();

        // 1) 媽媽手冊編號（等於）
        String idStr = first(map, "mother_handbook_id");
        if (hasText(idStr)) {
            try {
                preds.add(cb.equal(root.get("motherHandbookId"), Integer.valueOf(idStr.trim())));
            } catch (NumberFormatException ignore) {}
        }

        // 2) 媽媽姓名（LIKE）
        String name = first(map, "mother_name");
        if (hasText(name)) {
            preds.add(cb.like(root.get("motherName"), "%" + name.trim() + "%"));
        }

        // 3) 媽媽生日（= LocalDate）
        LocalDate mb = parseLocalDate(first(map, "mother_birthday"));
        if (mb != null) {
            preds.add(cb.equal(root.get("motherBirthday"), mb));
        }

        // 4) 最後一次月經日（= LocalDate）
        LocalDate last = parseLocalDate(first(map, "last_mc_date"));
        if (last != null) {
            preds.add(cb.equal(root.get("lastMcDate"), last));
        }

        // 5) 預計生產日（= LocalDate）
        LocalDate exp = parseLocalDate(first(map, "expected_birth_date"));
        if (exp != null) {
            preds.add(cb.equal(root.get("expectedBirthDate"), exp));
        }

        // 6) 體重（等於；若你的 Entity 是 Float 就改成 Float.valueOf）
        String wStr = first(map, "weight");
        if (hasText(wStr)) {
            try {
                preds.add(cb.equal(root.get("weight"), new BigDecimal(wStr.trim())));
            } catch (NumberFormatException ignore) {}
        }

        if (!preds.isEmpty()) {
            cq.where(cb.and(preds.toArray(new Predicate[0])));
        }

        // 依主鍵排序
        cq.orderBy(cb.asc(root.get("motherHandbookId")));

        return session.createQuery(cq).getResultList();
    }

    // ===== 小工具 =====
    private static String first(Map<String, String[]> map, String key) {
        if (map == null) return null;
        String[] arr = map.get(key);
        return (arr == null || arr.length == 0) ? null : arr[0];
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static LocalDate parseLocalDate(String s) {
        if (!hasText(s)) return null;
        try {
            // <input type="date"> 送上來是 ISO-8601（yyyy-MM-dd），直接 parse
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
