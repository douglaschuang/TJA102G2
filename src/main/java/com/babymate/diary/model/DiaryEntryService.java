package com.babymate.diary.model;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DiaryEntryService {
    private final DiaryEntryRepository repo;
    public DiaryEntryService(DiaryEntryRepository repo) { this.repo = repo; }

    public DiaryEntry save(DiaryEntry e) { return repo.saveAndFlush(e); }
    public List<DiaryEntry> findByMember(Integer memberId) { return repo.findByMemberIdOrderByWrittenAtDesc(memberId); }
    public Optional<DiaryEntry> findById(Integer id) { return repo.findById(id); }
    
    public void delete(Integer id) { repo.deleteById(id); }  //刪除
    
    public List<DiaryEntry> latest3(Integer memberId) {
        return repo.findRecentTop3(memberId, org.springframework.data.domain.PageRequest.of(0, 3));
    }

}
