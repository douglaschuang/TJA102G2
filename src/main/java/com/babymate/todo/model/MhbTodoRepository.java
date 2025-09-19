package com.babymate.todo.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MhbTodoRepository extends JpaRepository<MhbTodo, Integer> {
    List<MhbTodo> findByMotherHandbookIdOrderByDoneAscDueDateAsc(Integer mhbId);
}
