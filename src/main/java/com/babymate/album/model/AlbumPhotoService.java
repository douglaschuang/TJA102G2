package com.babymate.album.model;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AlbumPhotoService {
    private final AlbumPhotoRepository repo;
    public AlbumPhotoService(AlbumPhotoRepository repo) { this.repo = repo; }

    public AlbumPhoto save(AlbumPhoto p) { return repo.saveAndFlush(p); }
    public List<AlbumPhoto> findByMember(Integer memberId) { return repo.findByMemberIdOrderByCreatedAtDesc(memberId); }
    public Optional<AlbumPhoto> findById(Integer id) { return repo.findById(id); }
    
    public void delete(Integer id) { repo.deleteById(id); }
    
 // ★ 新增：關鍵字搜尋
    public List<AlbumPhoto> searchByMember(Integer memberId, String kw) {
        if (kw == null || kw.isBlank()) return findByMember(memberId);
        return repo.searchAll(memberId, kw.trim());
    }
}
