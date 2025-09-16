package com.babymate.preg.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PregnancyRecordService {

	private final PregnancyRecordRepository repo;

	// 建構子注入
	public PregnancyRecordService(PregnancyRecordRepository repo) {
		this.repo = repo;
	}

	public Map<Integer, Long> getCountMap() {
		Map<Integer, Long> map = new HashMap<>();
		for (PregnancyRecordRepository.MhbCount row : repo.countGroupByMhbId()) {
			map.put(row.getMhbId(), row.getCnt());
		}
		return map;
	}

	public List<PregnancyRecord> findByMhbId(Integer mhbId) {
		return repo.findByMotherHandbookIdOrderByVisitDateDesc(mhbId);
	}

	public PregnancyRecord save(PregnancyRecord r) {
		return repo.saveAndFlush(r);
	}

}
