package com.babymate.babyrecord.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		 Optional<BabyrecordVO> optional = repository.findById(babyrecordid);
		 return optional.orElse(null);
	 }
	 
	 public List<BabyrecordVO> getAll(){
		return repository.findAll();
	 }
	 
}
