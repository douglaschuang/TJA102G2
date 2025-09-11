package com.babymate.babyhandbook.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("babyhandbookService")
public class BabyhandbookService {

	 @Autowired
	 BabyhandbookRepository repository;
	 
	 
	 public void addBabyhandbook(BabyhandbookVO babyhandbookVO) {
		 repository.save(babyhandbookVO);
	 }
	 
	 public void updateBabyhandbook(BabyhandbookVO babyhandbookVO) {
		 repository.save(babyhandbookVO);
	 }
	 
	 public void deleteBabyhandbook(Integer babyhandbookid) {
		 if(repository.existsById(babyhandbookid))
		 	repository.deleteByBabyhandbookid(babyhandbookid);
	 }

	 public BabyhandbookVO getOneBabyhandbook(Integer babyhandbookid) {
		 Optional<BabyhandbookVO> optional = repository.findById(babyhandbookid);
		 return optional.orElse(null);
	 }
	 
	 public List<BabyhandbookVO> getAll(){
		return repository.findAll();
	 }
	 
}
