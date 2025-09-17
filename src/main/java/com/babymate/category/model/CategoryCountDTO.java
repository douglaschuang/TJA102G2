package com.babymate.category.model;

public record CategoryCountDTO(
		Integer categoryId,
		String categoryName,
		long count
) {}
