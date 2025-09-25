package com.babymate.mhb.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

@Service("mhbService")
@Transactional
public class MhbService {

    private final MhbRepository repository;

    public MhbService(MhbRepository repository) {
        this.repository = repository;
    }

    /* ========= C / U ========= */
    public MhbVO addMhb(MhbVO vo) { return repository.saveAndFlush(vo); }
    public MhbVO updateMhb(MhbVO vo) { return repository.saveAndFlush(vo); }

    /* ========= 軟刪 ========= */
    public void deleteMhb(Integer id) {
        repository.softDelete(id);
        repository.flush();
    }
    
    // 垃圾桶數量計數
    @Transactional(readOnly = true)
    public long countDeleted() {
        return repository.countDeletedNative();
    }


    /* ========= R ========= */
    @Transactional(readOnly = true)
    public MhbVO getOneMhb(Integer id) {
        return repository.findById(id).orElse(null); // 受 @Where 過濾
    }
    
    @Transactional(readOnly = true)
    public List<MhbVO> findByMemberIdOrderByUpdateTimeDesc(Integer memberId) {
        return repository.findByMemberIdOrderByUpdateTimeDesc(memberId);
    }


    // ★ 取會員最新的一本手冊（當作 Active）
    @Transactional(readOnly = true)
    public MhbVO findActiveByMemberId(Integer memberId) {
        if (memberId == null) return null;
        return repository.findTopByMemberIdAndDeletedFalseOrderByUpdateTimeDesc(memberId);
    }

    @Transactional(readOnly = true)
    public boolean existsActiveById(Integer mhbId) {
        return mhbId != null && repository.existsByMotherHandbookIdAndDeletedFalse(mhbId);
    }

    @Transactional(readOnly = true)
    public MhbVO findActiveById(Integer mhbId) {
        if (mhbId == null) return null;
        return repository.findByMotherHandbookIdAndDeletedFalse(mhbId);
    }

    /* ========= Admin 用清單 ========= */
    @Transactional(readOnly = true)
    public List<MhbVO> findAllActive() {
        return repository.findByDeletedFalseOrderByMotherHandbookIdAsc();
    }

    @Transactional(readOnly = true)
    public List<MhbVO> findAllDeleted() { return repository.findAllDeletedNative(); }

    /* ========= 復原 / 取圖（忽略 @Where） ========= */
    public int restoreMhb(Integer id) {
        int n = repository.restoreById(id);
        repository.flush();
        return n;
    }

    @Transactional(readOnly = true)
    public byte[] getPhotoBytesRaw(Integer id) {
        return repository.findPhotoBytesByIdNative(id);
    }

    public boolean hasAnyForMember(Integer memberId) {
        return repository.countByMemberIdAndDeletedFalse(memberId) > 0;
    }

    /* ========= ★ 複合查詢主角：Specification ========= */
    @Transactional(readOnly = true)
    public List<MhbVO> search(MhbFilter f) {
        // 沒帶條件就回既有清單（保留原行為）
        if (f == null || f.isEmpty()) {
            return repository.findByDeletedFalseOrderByMotherHandbookIdAsc();
        }

        Specification<MhbVO> spec = (root, cq, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // 雖然 @Where 已篩掉 deleted=true，但保險起見再加一次條件不會壞事
            ps.add(cb.isFalse(root.get("deleted")));

            // 單值條件
            if (f.getId() != null)        ps.add(cb.equal(root.get("motherHandbookId"), f.getId()));
            if (f.getMemberId() != null)  ps.add(cb.equal(root.get("memberId"), f.getMemberId()));
            if (f.getName() != null && !f.getName().isBlank())
                ps.add(cb.like(root.get("motherName"), "%" + f.getName().trim() + "%"));

            // 日期區間：生日
            if (f.getBirthdayFrom() != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("motherBirthday"), f.getBirthdayFrom()));
            if (f.getBirthdayTo() != null)
                ps.add(cb.lessThanOrEqualTo(root.get("motherBirthday"), f.getBirthdayTo()));

            // 日期區間：LMP
            if (f.getLmpFrom() != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("lastMcDate"), f.getLmpFrom()));
            if (f.getLmpTo() != null)
                ps.add(cb.lessThanOrEqualTo(root.get("lastMcDate"), f.getLmpTo()));

            // 日期區間：EDD
            if (f.getEddFrom() != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("expectedBirthDate"), f.getEddFrom()));
            if (f.getEddTo() != null)
                ps.add(cb.lessThanOrEqualTo(root.get("expectedBirthDate"), f.getEddTo()));

            // 體重區間（weight 是 BigDecimal，直接用 >= / <=）
            if (f.getwMin() != null)
                ps.add(cb.greaterThanOrEqualTo(root.get("weight"), f.getwMin()));
            if (f.getwMax() != null)
                ps.add(cb.lessThanOrEqualTo(root.get("weight"), f.getwMax()));

            // 是否有照片（BLOB 欄位判空）
            if (f.getHasPhoto() != null) {
                ps.add(f.getHasPhoto()
                        ? cb.isNotNull(root.get("upFiles"))
                        : cb.isNull(root.get("upFiles")));
            }

            // 排序
            cq.orderBy(cb.asc(root.get("motherHandbookId")));
            return cb.and(ps.toArray(Predicate[]::new));
        };

        return repository.findAll(spec);
    }
    
 // MhbService.java
    @Transactional
    public boolean softDeleteByIdAndMember(Integer mhbId, Integer memberId) {
        if (mhbId == null || memberId == null) return false;
        int n = repository.softDeleteByIdAndMember(mhbId, memberId);
        return n > 0;
    }

    }
