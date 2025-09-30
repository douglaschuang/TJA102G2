package com.babymate.todo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mhb_todo")
public class MhbTodo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer todoId;

	@Column(nullable = false)
	private Integer motherHandbookId;

	@Column(nullable = false)
	private String title;

	@org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
	private java.time.LocalDate dueDate;

	private String note;

	@Column(nullable = false)
	private Boolean done = false;

	@Column(nullable = false)
	private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

	@Column(nullable = false)
	private java.time.LocalDateTime updatedAt = java.time.LocalDateTime.now();

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = java.time.LocalDateTime.now();
	}

	// ===== getters / setters =====
	public Integer getTodoId() {
		return todoId;
	}

	public void setTodoId(Integer todoId) {
		this.todoId = todoId;
	}

	public Integer getMotherHandbookId() {
		return motherHandbookId;
	}

	public void setMotherHandbookId(Integer motherHandbookId) {
		this.motherHandbookId = motherHandbookId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public java.time.LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(java.time.LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean getDone() {
		return done;
	} // 用 getDone() 方便和 Boolean 對應

	public void setDone(Boolean done) {
		this.done = done;
	}

	public java.time.LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(java.time.LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public java.time.LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
