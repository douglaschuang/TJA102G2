package com.babymate.babyrecord.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
//
@Service("babyrecordService")
public class BabyrecordService {

	 @Autowired
	 BabyrecordRepository repository;
	 
	 
	 public void addBabyrecord(BabyrecordVO babyrecordVO) {
		 repository.save(babyrecordVO);
	 }
	 
	 public void updateBabyrecord(BabyrecordVO babyrecordVO) {
		 repository.save(babyrecordVO);
	 }
	 
	 public void deleteBabyrecord(Integer babyrecordid) {
		 if(repository.existsById(babyrecordid))
		 	repository.deleteByBabyrecordid(babyrecordid);
	 }

	 public BabyrecordVO getOneBabyrecord(Integer babyrecordid) {
		 if (babyrecordid == null) {
				throw new IllegalArgumentException("babyrecordid 不能為 null");
			}
			return repository.findById(babyrecordid)
					.orElseThrow(() -> new EntityNotFoundException("找不到 babyrecord，id: " + babyrecordid));	 
	 }
	 
	 public List<BabyrecordVO> getAll(){
		return repository.findAll();
	 }
	 
	 public Map<Integer, Integer> getCountMap() {
	        List<Object[]> resultList = repository.countRecordsGroupByBabyhandbookId();

	        Map<Integer, Integer> countMap = new HashMap<>();
	        for (Object[] row : resultList) {
	            Integer handbookId = (Integer) row[0];
	            Long count = (Long) row[1];
	            countMap.put(handbookId, count.intValue());
	        }
	        return countMap;
	    }
	 
	   @Transactional(readOnly = true)
	    public byte[] getPhotoBytesRaw(Integer id) {
	        return repository.findPhotoBytesById(id);
	    }

	   public List<BabyrecordVO> findByBabyhandbookId(Integer babyhandbookid) {
		    return repository.findByBabyhandbook_Babyhandbookid(babyhandbookid);
		}
	   
	   
}
