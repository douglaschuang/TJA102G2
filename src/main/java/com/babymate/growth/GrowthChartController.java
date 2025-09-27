package com.babymate.growth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.babymate.babyhandbook.model.BabyhandbookService;
import com.babymate.babyhandbook.model.BabyhandbookVO;
import com.babymate.babyrecord.model.BabyrecordRepository;
import com.babymate.babyrecord.model.BabyrecordService;
import com.babymate.babyrecord.model.BabyrecordVO;

@Controller
public class GrowthChartController {

    @Autowired
    private GrowthStandardService growthstandardSvc;

    @Autowired
    private BabyrecordRepository babyrecordRepository;  

	@Autowired
	private BabyrecordService babyrecordSvc;

	@Autowired
	private BabyhandbookService babyhandbookSvc;
	
    @GetMapping("/baby-chart")
    public String growthCharts(@RequestParam("babyhandbookid") Integer babyhandbookid, Model model) {
        
    
		// 拿出該寶寶的性別
    	BabyhandbookVO babyhandbook = babyhandbookSvc.getOneBabyhandbook(babyhandbookid);
    	if (babyhandbook == null) {
    	    return "/frontend/u/baby/babyrecord"; 
    	}
    	String babygender = babyhandbook.getBabygender();
        
		List<BabyrecordVO> babyrecord = babyrecordSvc.findByBabyhandbookId(babyhandbookid);
        List<Integer> babyweek = new ArrayList<>();
        List<Double> height = new ArrayList<>();
        List<Double> weight = new ArrayList<>();
        List<Double> hc = new ArrayList<>();

        for (BabyrecordVO rec : babyrecord) {
            if (rec.getBabyweek() != null && rec.getHeight() != null 
            		&& rec.getWeight() != null && rec.getHc() != null) {
                babyweek.add(rec.getBabyweek());
                height.add(rec.getHeight().doubleValue());
                weight.add(rec.getWeight().doubleValue());
                hc.add(rec.getHc().doubleValue());
            }
        }

        // 拿標準線
        Map<String, List<Double>> heightStd = growthstandardSvc.getHeightPercentiles(babygender);
        Map<String, List<Double>> weightStd = growthstandardSvc.getWeightPercentiles(babygender);
        Map<String, List<Double>> hcStd = growthstandardSvc.getHcPercentiles(babygender);

        model.addAttribute("babyweek", babyweek);
        model.addAttribute("height", height);
        model.addAttribute("weight", weight);
        model.addAttribute("hc", hc);
        model.addAttribute("heightPercentile", heightStd);
        model.addAttribute("weightPercentile", weightStd);
        model.addAttribute("hcPercentile", hcStd);
        
        return "frontend/u/baby/baby-chart";
    }
}
    
    





