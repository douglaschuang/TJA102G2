package com.babymate.photoWrite;

import java.sql.*;
import java.io.*;

/**
 * 💡 PhotoWrite.java
 * ---------------------
 * 這是一個用來「將圖片匯入資料庫」的 Java 小工具程式。
 * 
 * 🔹用途：
 *   - 將商品、會員、員工的圖片（.jpg）從本地端指定資料夾批次讀入 MySQL。
 *   - 透過 JDBC 的 PreparedStatement，把圖片轉成 BLOB 格式儲存。
 * 
 * 🔹執行方式：
 *   這不是 Spring Boot 控制器，而是可直接執行的 Java 主程式。
 *   通常在開發階段執行一次，用於初始化測試資料。
 */
class PhotoWrite {

    public static void main(String argv[]) {
    	
    	/** JDBC 連線設定 */
        Connection con = null;            // 資料庫連線物件
        PreparedStatement pstmt = null;   // 預備敘述物件，用來執行 SQL 語法
        
        /** 連線設定：資料庫位置與帳密 */
        String url = "jdbc:mysql://localhost:3306/babymate?serverTimezone=Asia/Taipei";
        String userid = "root";
        String passwd = "123456";
        
        /******************** 各類圖片與對應 SQL 語法 ********************/
        
        /** 📁 各類圖片的資料夾路徑（相對於專案目錄） */
        /** 商品圖片路徑 */
        String photos = "src/main/resources/static/assets/images/product_icon"; 
        String update = "UPDATE product SET product_icon = ? WHERE product_id = ?";
        
        /** 會員圖片路徑 */
        String photosMember = "src/main/resources/static/assets/images/member_icon"; 
        String updateMember = "update member set profile_picture =? where member_id=?";
        
        /** 員工圖片路徑 */
        String photosStaff = "src/main/resources/static/assets/images/staff_icon"; 
		String updateStaff = "update staff set pic =? where staff_id=?";

        try {
        	/** 1.建立資料庫連線 */
            con = DriverManager.getConnection(url, userid, passwd);
            
            /** 2.準備商品圖片更新 SQL */
            pstmt = con.prepareStatement(update);
            
            /** 3.讀取商品圖片資料夾內容（所有檔案） */
            File[] photoFiles = new File(photos).listFiles();
            if (photoFiles == null) {
                System.out.println("找不到圖片目錄: " + photos);
                return;
            }

            /** 4.逐一處理每個商品圖片檔案 */
            for (File f : photoFiles) {
            	/** 判斷檔案是否為普通檔案，且名稱符合「數字.jpg」格式（例如 1.jpg） */
                if (f.isFile() && f.getName().matches("\\d+\\.jpg")) {
                	
                    /** 從檔名取出數字部分當作商品編號，例如 1.jpg → 1 */
                    int productId = Integer.parseInt(f.getName().replace(".jpg", ""));

                    /** 使用 try-with-resources 自動關閉 InputStream */
                    try (InputStream fin = new FileInputStream(f)) {
                    	/** 將圖片以二進位串流寫入 PreparedStatement */
                        pstmt.setBinaryStream(1, fin, (int) f.length());
                        /** 設定商品編號 */
                        pstmt.setInt(2, productId);
                        /** 執行更新語法 */
                        int rows = pstmt.executeUpdate();
                        /** 顯示更新結果 */
                        System.out.printf("product_id=%d ← %s，更新筆數=%d%n", productId, f.getName(), rows);
                    }
                }
            }

            /** 新增Member測試資料圖檔至DB */
            InputStream fin = null;   // 宣告檔案輸入串流
            int count = 1;            // 用來對應 member_id
            pstmt = con.prepareStatement(updateMember);  // 準備更新語法
            
            /** 讀取會員圖片資料夾 */
            File[] photoFilesM = new File(photosMember).listFiles();
			for (File f : photoFilesM) {
				fin = new FileInputStream(f);  // 讀取圖片檔
				pstmt.setInt(2, count);        // 指定 member_id
				pstmt.setBinaryStream(1, fin); // 指定圖片資料
				pstmt.executeUpdate();         // 執行更新
				count++;                       // 會員編號遞增
				System.out.print(" update the database...");
				System.out.println(f.toString());  // 印出目前更新的檔案名稱
			}
            
			/** 新增Staff測試資料圖檔至DB */
            count = 1; // 重設計數器
            pstmt = con.prepareStatement(updateStaff); // 準備員工更新語法
            
            /** 讀取員工圖片資料夾 */
            File[] photoFilesS = new File(photosStaff).listFiles();
			for (File f : photoFilesS) {
				fin = new FileInputStream(f);   // 讀取圖片檔
				pstmt.setInt(2, count);         // 指定 staff_id
				pstmt.setBinaryStream(1, fin);  // 指定圖片內容
				pstmt.executeUpdate();          // 執行更新
				count++;                        // 員工編號遞增
				System.out.print(" update the database...");
				System.out.println(f.toString());  // 印出目前更新的檔案名稱
			}
			
            pstmt.close();  // 關閉 PreparedStatement 物件的連線資源
            System.out.println("圖片更新完成！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	/** 最後階段：釋放資料庫連線資源 */
            try {
                if (con != null) con.close(); // 關閉 Connection 物件的連線資源
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}