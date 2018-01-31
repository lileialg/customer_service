package custom.service;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/json")
public class JSONService {

	
	@RequestMapping(value = "/service")
	public Object service(String condition){
		
		
		return new Date();
		
	}
	
}
