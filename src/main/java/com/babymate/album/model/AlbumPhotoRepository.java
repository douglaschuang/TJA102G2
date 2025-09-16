package com.babymate.album.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhoto, Integer> {
    List<AlbumPhoto> findByMemberIdOrderByCreatedAtDesc(Integer memberId);
}
