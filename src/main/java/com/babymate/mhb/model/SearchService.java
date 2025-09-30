package com.babymate.mhb.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.babymate.album.model.AlbumPhotoRepository;
import com.babymate.diary.model.DiaryEntryRepository;
import com.babymate.todo.model.MhbTodoRepository;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final DiaryEntryRepository diaryRepo;
    private final AlbumPhotoRepository photoRepo;
    private final MhbTodoRepository todoRepo;

    public SearchService(DiaryEntryRepository diaryRepo,
                         AlbumPhotoRepository photoRepo,
                         MhbTodoRepository todoRepo) {
        this.diaryRepo = diaryRepo;
        this.photoRepo = photoRepo;
        this.todoRepo  = todoRepo;
    }

    /** 對外穩定版入口：n 預設 5 */
    public Map<String, Object> quickSearchStable(Integer memberId, Integer mhbId, String kw, int n) {
    	// ★ 修正：用 char, char，或改成 replace("\u3000", " ")
        String q = (kw == null) ? null : kw.strip().replace('\u3000', ' ');
        if (q != null && q.isBlank()) q = null;

        int topN = Math.max(1, n);
        var top = PageRequest.of(0, topN);

        List<?> diaries = List.of();
        List<?> photos  = List.of();
        List<?> todos   = List.of();

        try {
            if (memberId != null && q != null) {
                // 這兩個 Repository 預設就是以 memberId 搜的 TopN
                diaries = diaryRepo.searchTopN(memberId, q, top);
                photos  = photoRepo.searchTopN(memberId, q, top);

                // ★ 待辦：優先用指定 mhbId；沒有就跨所有手冊（nativeQuery）
                if (mhbId != null) {
                    todos = todoRepo.searchTopN(mhbId, q, top);
                } else {
                    // native: LIMIT 交由 repo 方法（或 topN 以參數傳入）
                    todos = todoRepo.searchTopNByMemberNative(memberId, q, topN);
                }
            }
        } catch (Exception ex) {
            // 防呆：任何搜尋失敗都不讓頁面中斷
            log.error("[search][stable] query failed: memberId={}, mhbId={}, q='{}'", memberId, mhbId, q, ex);
            diaries = List.of();
            photos  = List.of();
            todos   = List.of();
        }

        log.info("[search][stable] q='{}' memberId={} mhbId={} -> diaries={} photos={} todos={}",
                 q, memberId, mhbId, diaries.size(), photos.size(), todos.size());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("diaries", diaries);
        m.put("photos",  photos);
        m.put("todos",   todos);
        return m;
    }
}
