package com.babymate.category.model;

/**
 * DTO：分類與商品數量
 * 
 * 用於接收 Repository 查詢結果。
 * Java 14+ 可使用 record 來簡化資料封裝。
 *
 * 欄位：
 *  - categoryId：商品分類ID
 *  - categoryName：商品分類名稱
 *  - count：該商品分類底下的上架商品數量
 */
public record CategoryCountDTO(
		Integer categoryId,
		String categoryName,
		long count
) {}
