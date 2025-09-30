package com.babymate.album.model;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Integer> {
    List<AlbumPhoto> findByMemberIdOrderByCreatedAtDesc(Integer memberId);

 // AlbumPhotoRepository
    @Query("""
      select p from AlbumPhoto p
      where p.memberId = :memberId
        and lower(coalesce(cast(p.caption as string), '')) like concat('%', lower(:kw), '%')
      order by coalesce(p.takenAt, p.createdAt) desc
    """)
    List<AlbumPhoto> searchTopN(@Param("memberId") Integer memberId,
                                @Param("kw") String kw,
                                PageRequest top);


    // === 新增：關鍵字搜尋（完整清單） ===
    @Query("""
      select p from AlbumPhoto p
      where p.memberId = :memberId
        and lower(coalesce(p.caption, '')) like lower(concat('%', :kw, '%'))
      order by coalesce(p.takenAt, p.createdAt) desc
    """)
    List<AlbumPhoto> searchAll(@Param("memberId") Integer memberId,
                               @Param("kw") String kw);
}

