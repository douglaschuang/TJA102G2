package com.babymate.orders.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.babymate.member.model.MemberVO;
import com.babymate.orderDetail.model.OrderDetailVO;
import com.babymate.orders.model.OrdersService;
import com.babymate.orders.model.OrdersVO;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my-orders")
public class OrdersFrontEndController {
	
	@Autowired
	private OrdersService ordersSvc;
	
	@GetMapping
	public String myOrders(HttpSession session, ModelMap model) {
		MemberVO member = (MemberVO) session.getAttribute("member");
		if (member == null) {
			return "redirect:/shop/login";
		}
		
		List<OrdersVO> myOrders = ordersSvc.getOrdersByMemberIdDesc(member.getMemberId());
		model.addAttribute("orders", myOrders);
		return "frontend/my-orders";
	}
	
	/**
	 * 下載指定訂單的 PDF 明細
	 * 範例路徑：/orders/{orderNo}/export/pdf
	 * 例如：/orders/202510050001/export/pdf
	 */
	@GetMapping("/{orderNo}/export/pdf")
	public void exportOrderToPdf(@PathVariable String orderNo,
	                             HttpServletResponse response) throws IOException {

	    OrdersVO order = ordersSvc.findByOrderNo(orderNo);
	    if (order == null) {
	        response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        return;
	    }

	    response.setContentType("application/pdf");
	    response.setHeader("Content-Disposition", "attachment; filename=" + orderNo + ".pdf");

	    try (OutputStream out = response.getOutputStream()) {
	        // 1. 設定文件與頁面邊界
	        Document document = new Document(PageSize.A4, 36, 36, 48, 36);
	        PdfWriter.getInstance(document, out);
	        document.open();

	        // 2. 中文字型（請先把 NotoSansTC-Regular.ttf 放到 resources/fonts/）
	        //    用 ClassPathResource 取實體路徑
	        BaseFont bf;
	        try (var fontStream = new ClassPathResource("fonts/NotoSansTC-Regular.ttf")
	        						  .getInputStream()) {
	        		byte[] fontBytes = fontStream.readAllBytes();
	        		bf = BaseFont.createFont("NotoSansTC-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
	        }
	        
	        // 3. 設定字型樣式
	        Font titleFont = new Font(bf, 18, Font.BOLD);
	        Font hFont     = new Font(bf, 12, Font.BOLD);
	        Font bodyFont  = new Font(bf, 12);

	        // 4. 標題
	        Paragraph title = new Paragraph("訂單明細", titleFont);
	        title.setAlignment(Element.ALIGN_CENTER);
	        title.setSpacingAfter(10f);
	        document.add(title);

	        // 5. 基本資料（左邊欄位名、右邊值）
	        PdfPTable infoTable = new PdfPTable(2);
	        infoTable.setWidths(new float[]{1.4f, 3.6f});
	        infoTable.setWidthPercentage(100);
	        addRow(infoTable, "訂單編號：", order.getOrderNo(), hFont, bodyFont);
	        // 將 LocalDateTime 格式化為 yyyy-MM-dd HH:mm:ss
	        String formattedTime = order.getOrderTime() != null
	            ? order.getOrderTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
	            : "";
	        addRow(infoTable, "訂購日期：", formattedTime, hFont, bodyFont);
	        addRow(infoTable, "收件人：",   nullToEmpty(order.getRecipient()), hFont, bodyFont);
	        addRow(infoTable, "地址：",     nullToEmpty(order.getAddress()), hFont, bodyFont);
	        addRow(infoTable, "電話：",     nullToEmpty(order.getPhone()), hFont, bodyFont);
	        document.add(infoTable);

	        document.add(Chunk.NEWLINE);

	        // 6. 商品明細表
	        PdfPTable table = new PdfPTable(4);
	        table.setWidths(new float[]{3.5f, 1.0f, 1.2f, 1.2f});
	        table.setWidthPercentage(100);
	        addHeader(table, "商品", hFont);
	        addHeader(table, "數量", hFont);
	        addHeader(table, "單價", hFont);
	        addHeader(table, "小計", hFont);

	        NumberFormat nf = java.text.NumberFormat.getInstance();
	        nf.setGroupingUsed(true);
	        nf.setMaximumFractionDigits(0);
	        nf.setMinimumFractionDigits(0);

	        for (OrderDetailVO d : order.getOrderDetails()) {
	            String ProductName = d.getProductVO() != null ? d.getProductVO().getProductName() : "";
	            int qty = d.getQuantity() == null ? 0 : d.getQuantity();
	            var price = d.getPrice() == null ? BigDecimal.ZERO : d.getPrice();
	            BigDecimal subTotal  = price.multiply(BigDecimal.valueOf(qty));

	            addCellLeft(table,  ProductName, bodyFont);
	            addCellCenter(table, String.valueOf(qty), bodyFont);
	            addCellRight(table,  "NT$" + nf.format(price), bodyFont);
	            addCellRight(table,  "NT$" + nf.format(subTotal),  bodyFont);
	        }

	        document.add(table);
	        document.add(Chunk.NEWLINE);

	        // 7. 總金額
	        Paragraph total = new Paragraph("總金額：NT$" +
	        					  nf.format(order.getAmount()), new Font(bf, 14, Font.BOLD));
	        total.setAlignment(Element.ALIGN_RIGHT);
	        document.add(total);

	        document.close();
	    } catch (Exception e) {
	        // 可視情況記錄 log
	        throw new IOException(e);
	    }
	}

	/** 工具方法們（可放同一個 Controller 底下） */
	private static void addRow(PdfPTable t, String k, String v, Font kf, Font vf){
	    PdfPCell c1 = new PdfPCell(new Phrase(k, kf));
	    c1.setBorder(Rectangle.NO_BORDER);
	    t.addCell(c1);

	    PdfPCell c2 = new PdfPCell(new Phrase(nullToEmpty(v), vf));
	    c2.setBorder(Rectangle.NO_BORDER);
	    t.addCell(c2);
	}
	private static void addHeader(PdfPTable t, String text, Font f){
	    PdfPCell c = new PdfPCell(new Phrase(text, f));
	    c.setHorizontalAlignment(Element.ALIGN_CENTER);
	    c.setBackgroundColor(new BaseColor(240,240,240));
	    t.addCell(c);
	}
	private static void addCellLeft(PdfPTable t, String text, Font f){
	    PdfPCell c = new PdfPCell(new Phrase(text, f));
	    c.setHorizontalAlignment(Element.ALIGN_LEFT);
	    c.setPadding(5f);
	    c.setBorderWidth(0.5f);
	    c.setBorderColor(BaseColor.LIGHT_GRAY);
	    t.addCell(c);
	}
	private static void addCellCenter(PdfPTable t, String text, Font f){
	    PdfPCell c = new PdfPCell(new Phrase(text, f));
	    c.setHorizontalAlignment(Element.ALIGN_CENTER);
	    c.setPadding(5f);
	    c.setBorderWidth(0.5f);
	    c.setBorderColor(BaseColor.LIGHT_GRAY);
	    t.addCell(c);
	}
	private static void addCellRight(PdfPTable t, String text, Font f){
	    PdfPCell c = new PdfPCell(new Phrase(text, f));
	    c.setHorizontalAlignment(Element.ALIGN_RIGHT);
	    c.setPadding(5f);
	    c.setBorderWidth(0.5f);
	    c.setBorderColor(BaseColor.LIGHT_GRAY);
	    t.addCell(c);
	}
	private static String nullToEmpty(String s){ return s == null ? "" : s; }
	

}
