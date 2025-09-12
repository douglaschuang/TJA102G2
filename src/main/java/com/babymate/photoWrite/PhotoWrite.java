package com.babymate.photoWrite;

import java.sql.*;
import java.io.*;

class PhotoWrite {

    public static void main(String argv[]) {
        Connection con = null;
        PreparedStatement pstmt = null;
        String url = "jdbc:mysql://localhost:3306/babymate?serverTimezone=Asia/Taipei";
        String userid = "root";
        String passwd = "123456";
        String photos = "src/main/resources/static/assets/images/product_icon"; 
        String update = "UPDATE product SET product_icon = ? WHERE product_id = ?";

        try {
            con = DriverManager.getConnection(url, userid, passwd);
            pstmt = con.prepareStatement(update);

            File[] photoFiles = new File(photos).listFiles();
            if (photoFiles == null) {
                System.out.println("找不到圖片目錄: " + photos);
                return;
            }

            for (File f : photoFiles) {
                if (f.isFile() && f.getName().matches("\\d+\\.jpg")) {
                    // 從檔名取出數字 (1.jpg → 1)
                    int productId = Integer.parseInt(f.getName().replace(".jpg", ""));

                    try (InputStream fin = new FileInputStream(f)) {
                        pstmt.setBinaryStream(1, fin, (int) f.length());
                        pstmt.setInt(2, productId);
                        int rows = pstmt.executeUpdate();

                        System.out.printf("product_id=%d ← %s，更新筆數=%d%n", productId, f.getName(), rows);
                    }
                }
            }

            pstmt.close();
            System.out.println("圖片更新完成！");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}