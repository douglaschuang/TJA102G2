package com.babymate.todo.model;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MhbTodoService {

    private final MhbTodoRepository repo;

    public MhbTodoService(MhbTodoRepository repo) { this.repo = repo; }

    public List<MhbTodo> listByMhb(Integer mhbId){
        return repo.findByMotherHandbookIdOrderByDoneAscDueDateAsc(mhbId);
    }

    @Transactional
    public MhbTodo save(MhbTodo t){ return repo.saveAndFlush(t); }

    public MhbTodo getOne(Integer id){ return repo.findById(id).orElse(null); }

    @Transactional
    public void delete(Integer id){ repo.deleteById(id); }
}

//@Service
//@Transactional
//public class MhbTodoService {
//
//	private final MhbTodoRepository repo;
//
//	public MhbTodoService(MhbTodoRepository repo) {
//		this.repo = repo;
//	}
//
//	public List<MhbTodo> findByMhb(Integer mhbId) {
//		return repo.findByMotherHandbookIdOrderByDoneAscDueDateAsc(mhbId);
//	}
//
//	public MhbTodo save(MhbTodo t) {
//		return repo.saveAndFlush(t);
//	}
//
//	@Transactional(readOnly = true)
//	public MhbTodo get(Integer id) {
//		return repo.findById(id).orElse(null);
//	}
//
//	public void delete(Integer id) {
//		repo.deleteById(id);
//	}
//}
