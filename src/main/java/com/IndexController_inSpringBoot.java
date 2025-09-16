package com;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.category.model.CategoryService;
import com.babymate.category.model.CategoryVO;
import com.babymate.member.model.MemberVO;
import com.babymate.member.service.MemberService;
import com.babymate.product.model.ProductService;
import com.babymate.product.model.ProductVO;
import com.babymate.staff.model.StaffVO;
import com.babymate.staff.service.StaffService;



//@PropertySource("classpath:application.properties") // 於https://start.spring.io建立Spring Boot專案時, application.properties文件預設已經放在我們的src/main/resources 目錄中，它會被自動檢測到
@Controller
public class IndexController_inSpringBoot {
	
	// @Autowired (●自動裝配)(Spring ORM 課程)
	// 目前自動裝配了EmpService --> 供第66使用
	@Autowired
	StaffService staffSvc;
	
	@Autowired
	MemberService memberSvc;
	
	@Autowired
	ProductService productSvc;
	
	@Autowired
	CategoryService categorySvc;
	
    // inject(注入資料) via application.properties
    @Value("${welcome.message}")
    private String message;
	
    private List<String> myList = Arrays.asList("Spring Boot Quickstart 官網 : https://start.spring.io", "IDE 開發工具", "直接使用(匯入)官方的 Maven Spring-Boot-demo Project + pom.xml", "直接使用官方現成的 @SpringBootApplication + SpringBootServletInitializer 組態檔", "依賴注入(DI) HikariDataSource (官方建議的連線池)", "Thymeleaf", "Java WebApp (<font color=red>快速完成 Spring Boot Web MVC</font>)");
    @GetMapping("/")
    public String index(Model model) {
    	model.addAttribute("message", message);
        model.addAttribute("myList", myList);
        return "frontend/index"; //view
    }
    
    // http://......../hello?name=peter1
    @GetMapping("/hello")
    public String indexWithParam(
            @RequestParam(name = "name", required = false, defaultValue = "") String name, Model model) {
        model.addAttribute("message", name);
        return "frontend/index"; //view
    }
    
  
    //=========== 以下第63~75行是提供給 /src/main/resources/templates/back-end/emp/select_page.html 與 listAllEmp.html 要使用的資料 ===================   
    @GetMapping("/admin/product/select_page")
	public String select_page(Model model) {
		return "admin/product/select_page";
	}
    
//    @GetMapping("/admin/product/listAllProduct")
//	public String listAllProduct(Model model) {
//		return "admin/product/listAllProduct";
//	}
    
//    @GetMapping("/admin")
//	public String listAllStaff(Model model) {
//		return "admin/index";
//	}
    
    @GetMapping("/admin/staff/listAllStaff")
	public String listAllEmp(Model model) {
		return "admin/staff/stafflist";
	}
    
    @GetMapping("/admin/login")
	public String staffLogin(Model model) {
		return "admin/login";
	}
    
    @GetMapping("/admin/member/listAllMember")
	public String listAllMember(Model model) {
		return "admin/member/memberlist";
	}
    
    @ModelAttribute("staffListData")  // for select_page.html 第97 109行用 // for listAllEmp.html 第117 133行用
	protected List<StaffVO> referenceListDataStaff(Model model) {
		
    	List<StaffVO> list = staffSvc.getAll();
		return list;
	}
    
    @ModelAttribute("memberListData")
	protected List<MemberVO> referenceListDataMem(Model model) {
		
    	List<MemberVO> list = memberSvc.getAll();
		return list;
	}
    
    @ModelAttribute("productListData")  // for select_page.html 第97 109行用 // for listAllEmp.html 第85行用
	protected List<ProductVO> referenceListData(Model model) {
		
    	List<ProductVO> list = productSvc.getAll();
		return list;
	}
    
	@ModelAttribute("categoryListData") // for select_page.html 第135行用
	protected List<CategoryVO> referenceListData_Category(Model model) {
		model.addAttribute("categoryVO", new CategoryVO()); // for select_page.html 第133行用
		List<CategoryVO> list = categorySvc.getAll();
		return list;
	}

}