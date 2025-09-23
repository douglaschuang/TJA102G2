package com.babymate.photoWrite;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class PhotoWriteMhb {

    // 依你的環境調整
    static final String URL = "jdbc:mysql://localhost:3306/babymate?serverTimezone=Asia/Taipei&useSSL=false&useUnicode=true&characterEncoding=UTF-8";
    static final String USER = "root";
    static final String PASS = "123456";

    // 放手冊照片的資料夾（建議：以「手冊ID.副檔名」命名）
    // 例如：1.jpg、2.png、15.jpeg ...
    static final String PHOTO_DIR = "src/main/resources/static/assets/images/mhb_photo";

    // 依你的資料表實際欄位調整
    static final String UPDATE_SQL = "UPDATE mother_handbook SET upfiles = ? WHERE mother_handbook_id = ?";

    public static void main(String[] args) {
        File dir = new File(PHOTO_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("找不到目錄: " + dir.getAbsolutePath());
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("資料夾中沒有任何圖片檔");
            return;
        }

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {

            // 建議關掉 auto-commit 做批次提交
            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            int success = 0, fail = 0;

            for (File f : files) {
                if (!f.isFile()) continue;

                // 僅處理常見圖片格式
                String name = f.getName().toLowerCase();
                if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) continue;

                // 檔名（去副檔名）必須是手冊ID
                String idPart = name.replaceAll("\\.(jpg|jpeg|png)$", "");
                int mhbId;
                try {
                    mhbId = Integer.parseInt(idPart);
                } catch (NumberFormatException e) {
                    System.out.println("檔名不是數字ID，略過: " + f.getName());
                    continue;
                }

                try (InputStream in = new FileInputStream(f)) {
                    ps.setBinaryStream(1, in, (int) f.length());
                    ps.setInt(2, mhbId);
                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        success++;
                        System.out.printf("✔ 更新 handBookId=%d ← %s%n", mhbId, f.getName());
                    } else {
                        fail++;
                        System.out.printf("✘ 找不到 handBookId=%d，未更新 ← %s%n", mhbId, f.getName());
                    }
                } catch (Exception ex) {
                    fail++;
                    System.out.printf("✘ 寫入失敗 handBookId=%d ← %s，原因：%s%n", mhbId, f.getName(), ex.getMessage());
                }
            }

            con.commit();
            con.setAutoCommit(oldAutoCommit);
            System.out.printf("%n完成！成功=%d，失敗=%d%n", success, fail);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("批次作業失敗，可能已回滾。");
        }
    }
}
