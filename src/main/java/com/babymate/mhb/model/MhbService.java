package com.babymate.mhb.model;

import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("mhbService")
@Transactional
public class MhbService {

    private final MhbRepository repository;

    public MhbService(MhbRepository repository, SessionFactory sessionFactory) {
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

    /* ========= R ========= */
    @Transactional(readOnly = true)
    public MhbVO getOneMhb(Integer id) {
        return repository.findById(id).orElse(null); // 受 @Where 過濾
    }

    // ★ 新增：取會員最新的一本手冊（當作 Active）
    @Transactional(readOnly = true)
    public MhbVO findActiveByMemberId(Integer memberId) {
        if (memberId == null) return null;
        // 推導式：
        return repository.findTopByMemberIdAndDeletedFalseOrderByUpdateTimeDesc(memberId);
        // 或 native：
        // return repository.findLatestByMemberIdNative(memberId);
    }


    /* ========= Admin 用清單 ========= */
    @Transactional(readOnly = true)
    public List<MhbVO> findAllActive() { return repository.findByDeletedFalseOrderByMotherHandbookIdAsc(); }

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
}
