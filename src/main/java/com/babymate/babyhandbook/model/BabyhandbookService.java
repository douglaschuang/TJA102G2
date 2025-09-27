package com.babymate.babyhandbook.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.babymate.mhb.model.MhbVO;

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
	 

	 public BabyhandbookVO getOneBabyhandbook(Integer babyhandbookid) {
		 Optional<BabyhandbookVO> optional = repository.findById(babyhandbookid);
		 return optional.orElse(null);
	 }
	 
	 public List<BabyhandbookVO> getAll(){
		return repository.findAll();
	 }
	 
	 //軟刪除
	 @Transactional
	 public void softDelete(Integer babyhandbookid) {
		 	repository.softDelete(babyhandbookid);
	 }
	 
	 //還原資料
	 @Transactional
	  public int restore(Integer babyhandbookid) {
	        return repository.restoreById(babyhandbookid);
	  }
	    
	 //Admin後台管理用的清單
      @Transactional(readOnly = true)
	   public List<BabyhandbookVO> findAllActive() {
	        return repository.findAllActive();
	   }

	   @Transactional(readOnly = true)
	   public List<BabyhandbookVO> findAllDeleted() {
	        return repository.findAllDeleted();
	    }

	   @Transactional(readOnly = true)
	    public byte[] getPhotoBytesRaw(Integer id) {
	        return repository.findPhotoBytesById(id);
	    }
	   
	  //前台會用到
//	    public BabyhandbookVO findLatestByMemberId(Integer memberId) {
//	    	List<BabyhandbookVO> list = repository.findAllByMemberId(memberId);
//	        return list.isEmpty() ? null : list.get(0);
//	    }

	    public BabyhandbookVO findByIdAndMemberId(Integer babyhandbookId, Integer memberId) {
	        return repository.findByIdAndMemberId(babyhandbookId, memberId).orElse(null);
	    }

		public List<BabyhandbookVO> findAllByMemberId(Integer memberId) {
			return repository.findAllByMemberId(memberId);
		}
	    
}


