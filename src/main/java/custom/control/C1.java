package custom.control;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/c1")
public class C1 {

	@RequestMapping("/test")
	public String test(Map<String, Object> model,String name){
		model.put("name", name);
		return "test";
	}
}
