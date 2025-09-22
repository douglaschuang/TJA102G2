package com.babymate.member.controller;

import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/export")
public class MemberExportController {

	@Autowired
	MemberService memberSvc;
	
	@GetMapping("/memberExcel")
	public void memberExportToExcel(HttpServletResponse response) throws IOException {
		
		// 取得會員清單
		List<MemberVO> list = memberSvc.getAll();
		
		// 判斷會員清單是否有值
		if (list.isEmpty()) {
			response.setStatus(HttpStatus.NO_CONTENT.value());
			return;
		}
		
		// 準備匯出
		// 建立Excel工作簿和工作表
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("會員資料");
        
		// 準備建立標題列
        Row headerRow = sheet.createRow(0);
        String[] headers = {"帳號", "名稱", "生日", "性別", "收件人", "電話", "地址"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
		
        // 寫入資料
        int rowNum = 1;
		for(MemberVO member : list) {
	        Row row = sheet.createRow(rowNum++);
	        row.createCell(0).setCellValue(member.getAccount());
	        row.createCell(1).setCellValue(member.getName());
	        if (member.getBirthday() != null) {
	           row.createCell(2).setCellValue("'"+member.getBirthday());
	        } else {
	        	row.createCell(2).setCellValue("");
	        }
	        
	        if (member.getGender() != null) {
	        	row.createCell(3).setCellValue(member.getGender().name());
	        } else {
	        	row.createCell(3).setCellValue("");
	        }
	        
	        row.createCell(4).setCellValue(member.getRecipientName());
	        row.createCell(5).setCellValue(member.getPhone());
	        row.createCell(6).setCellValue(member.getAddress());
	        
		}
		
		// 自動調整欄位寬度
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
		
		// 5. 設定HTTP Header以觸發下載
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=members.xlsx");

        // 6. 將Excel寫入到HttpServletResponse的輸出流
        workbook.write(response.getOutputStream());
        workbook.close();
	}
	
}
