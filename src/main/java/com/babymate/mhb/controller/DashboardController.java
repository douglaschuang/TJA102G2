package com.babymate.mhb.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.album.model.AlbumPhotoService;
import com.babymate.diary.model.DiaryEntryService;
import com.babymate.member.model.MemberVO;
import com.babymate.mhb.model.MhbService;
import com.babymate.mhb.model.MhbVO;
import com.babymate.mhb.model.SearchService;
import com.babymate.preg.model.PregnancyRecord;
import com.babymate.preg.model.PregnancyRecordService;
import com.babymate.todo.model.MhbTodoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/blog")
@Transactional(readOnly = true) // ★ 渲染期間保持 Session，避免 Lazy/Lob 在模板讀炸掉
public class DashboardController {

    private final MhbService mhbService;
    private final PregnancyRecordService prService;
    private final AlbumPhotoService albumPhotoService;
    private final DiaryEntryService diaryEntryService;
    private final MhbTodoService mhbTodoService;
    private final SearchService searchService;

    public DashboardController(MhbService mhbService,
                               PregnancyRecordService prService,
                               AlbumPhotoService albumPhotoService,
                               DiaryEntryService diaryEntryService,
                               MhbTodoService mhbTodoService,
                               SearchService searchService) {
        this.mhbService = mhbService;
        this.prService = prService;
        this.albumPhotoService = albumPhotoService;
        this.diaryEntryService = diaryEntryService;
        this.mhbTodoService = mhbTodoService;
        this.searchService = searchService;
    }

    @GetMapping("/full-grid-left")
    public String dashboard(@RequestParam(value = "tab", required = false) String tab,
                            @RequestParam(value = "mhbId", required = false) Integer mhbId,
                            @RequestParam(value = "q", required = false) String q,
                            HttpSession session,
                            Model model) {

        // --- 基本參數 ---
        model.addAttribute("tab", tab);
        model.addAttribute("activeTab", tab);

        // 取得登入資訊（兩種 session key 皆相容）
        MemberVO login = (MemberVO) session.getAttribute("member");
        if (login == null) login = (MemberVO) session.getAttribute("loginMember");

        // 規範化關鍵字；空白視為 null
        String query = (q == null) ? null : q.strip().replace('\u3000', ' ');
        if (query != null && query.isBlank()) query = null;
        model.addAttribute("q", query);

        boolean isSearch = "search".equals(tab);
        boolean needLogin = isSearch && (login == null);
        model.addAttribute("needLogin", needLogin);

        // 側欄：最新日記（無登入給空清單，避免模板 NPE）
        if (login != null) {
            model.addAttribute("latestDiary", diaryEntryService.latest3(login.getMemberId()));
        } else {
            model.addAttribute("latestDiary", Collections.emptyList());
        }

        // --- 是否需要載入 MHB 相關資料 ---
        boolean needMhb = "mhb".equals(tab)
                || "mhb-records".equals(tab)
                || "mhb-charts".equals(tab)
                || "todos".equals(tab);

        // 選定/取得目前手冊（僅在需要時）
        MhbVO mhb = null;
        if (needMhb) {
            if (login != null) {
                List<MhbVO> mhbs = mhbService.findByMemberIdOrderByUpdateTimeDesc(login.getMemberId());
                model.addAttribute("mhbs", mhbs);

                if (mhbId != null) {
                    for (MhbVO x : mhbs) {
                        if (x.getMotherHandbookId() != null && x.getMotherHandbookId().equals(mhbId)) {
                            mhb = x; break;
                        }
                    }
                }
                if (mhb == null && !mhbs.isEmpty()) {
                    mhb = mhbs.get(0);
                }
            } else if (mhbId != null) {
                // 未登入但指定 mhbId（若有公開內容）
                mhb = mhbService.getOneMhb(mhbId);
            }
        }

        model.addAttribute("mhb", mhb);
        if (mhb != null) {
            model.addAttribute("pregnancyWeek", calcPregnancyWeek(mhb));
        }

        // --- 懷孕紀錄列表 ---
        if ("mhb-records".equals(tab) && mhb != null) {
            List<PregnancyRecord> records = prService.findByMhbId(mhb.getMotherHandbookId());
            model.addAttribute("records", records);
        }

        // --- 圖表資料 ---
        if ("mhb-charts".equals(tab) && mhb != null) {
            List<PregnancyRecord> records = prService.findByMhbId(mhb.getMotherHandbookId());
            records.sort(Comparator.comparing(PregnancyRecord::getVisitDate));

            List<String> labels = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            List<Integer> sps = new ArrayList<>();
            List<Integer> dps = new ArrayList<>();
            List<Integer> fhs = new ArrayList<>();

            for (PregnancyRecord r : records) {
                labels.add(r.getVisitDate() != null ? r.getVisitDate().toString() : "");
                weights.add(r.getWeight() != null ? r.getWeight().doubleValue() : null);
                sps.add(r.getSp());
                dps.add(r.getDp());
                fhs.add(parseFhsToInt(r.getFhs()));
            }

            model.addAttribute("chartLabels", labels);
            model.addAttribute("chartWeights", weights);
            model.addAttribute("chartSp", sps);
            model.addAttribute("chartDp", dps);
            model.addAttribute("chartFhs", fhs);
        }

        // --- 待辦（頁籤）支援 q ---
        if ("todos".equals(tab) && mhb != null) {
            if (query != null) {
                model.addAttribute("todos",
                        mhbTodoService.searchAll(mhb.getMotherHandbookId(), query));
            } else {
                model.addAttribute("todos",
                        mhbTodoService.listByMhb(mhb.getMotherHandbookId()));
            }
        }

        // --- 相簿分頁（支援 q） ---
        if ("album".equals(tab) && login != null) {
            if (query != null) {
                model.addAttribute("photos", albumPhotoService.searchByMember(login.getMemberId(), query));
            } else {
                model.addAttribute("photos", albumPhotoService.findByMember(login.getMemberId()));
            }
        }

        // --- 日記分頁（支援 q） ---
        if ("diary".equals(tab) && login != null) {
            if (query != null) {
                model.addAttribute("entries", diaryEntryService.searchByMember(login.getMemberId(), query));
            } else {
                model.addAttribute("entries", diaryEntryService.findByMember(login.getMemberId()));
            }
        }

        // --- 搜尋聚合分頁（跨來源 Top N）---
        if (isSearch) {
            if (!needLogin && query != null && login != null) {
                Integer mhbIdForTodo = (mhb != null) ? mhb.getMotherHandbookId() : mhbId; // 若頁面指定 mhbId 也尊重
                model.addAllAttributes(
                        searchService.quickSearchStable(login.getMemberId(), mhbIdForTodo, query, 5)
                ); // 放入 diaries / photos / todos
            } else {
                // 未登入或無關鍵字 → 回空集合，避免模板 NPE
                model.addAttribute("diaries", Collections.emptyList());
                model.addAttribute("photos", Collections.emptyList());
                model.addAttribute("todos", Collections.emptyList());
            }
        }

        return "frontend/blog-full-then-grid-left-sidebar";
    }

    private Integer calcPregnancyWeek(MhbVO mhb) {
        if (mhb == null) return null;
        LocalDate lmp = mhb.getLastMcDate();
        LocalDate edd = mhb.getExpectedBirthDate();
        if (lmp == null && edd != null) lmp = edd.minusWeeks(40);
        if (lmp == null) return null;
        long days = java.time.temporal.ChronoUnit.DAYS.between(lmp, LocalDate.now());
        return days < 0 ? null : (int) (days / 7) + 1;
    }

    private Integer parseFhsToInt(String f) {
        if (f == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{2,3})").matcher(f);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
