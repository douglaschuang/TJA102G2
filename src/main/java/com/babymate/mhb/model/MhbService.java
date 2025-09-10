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

    public MhbVO addMhb(MhbVO vo) {
        return repository.saveAndFlush(vo);
    }

    public MhbVO updateMhb(MhbVO vo) {
        return repository.saveAndFlush(vo);
    }

    /* ========= 軟刪除 =========
       若你的 Entity 有 @SQLDelete，也可以改回 deleteById(id)。
       這裡顯式呼叫 softDelete，確保一定是「更新 deleted=1」。
    */
    public void deleteMhb(Integer id) {
        repository.softDelete(id);        // UPDATE mother_handbook SET deleted=1 WHERE id=? AND deleted=0
        repository.flush();
    }

    /* ========= R ========= */

    @Transactional(readOnly = true)
    public MhbVO getOneMhb(Integer id) {
        return repository.findById(id).orElse(null); // 受 @Where 過濾，只會拿到未刪
    }

    /* ========= Admin 殼用的清單 ========= */

    @Transactional(readOnly = true)
    public List<MhbVO> findAllActive() {
        return repository.findByDeletedFalseOrderByMotherHandbookIdAsc();
    }

    @Transactional(readOnly = true)
    public List<MhbVO> findAllDeleted() {
        // 若你已做 derived method 也可用：
        // return repository.findByDeletedTrueOrderByMotherHandbookIdAsc();
        return repository.findAllDeletedNative();
    }

    /* ========= 復原 / 取圖（忽略 @Where） ========= */

    public int restoreMhb(Integer id) {
        int n = repository.restoreById(id); // UPDATE mother_handbook SET deleted=0 WHERE id=? AND deleted=1
        repository.flush();
        return n;
    }

    @Transactional(readOnly = true)
    public byte[] getPhotoBytesRaw(Integer id) {
        return repository.findPhotoBytesByIdNative(id);
    }
}
