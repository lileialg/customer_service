package custom.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/pbf")
public class MbProtoBuffer {

	@RequestMapping(value = "/{z}/{x}/{y}",produces="application/x-protobuf")
	public byte[] getVectorTile(int id,String condition){
		
		return new byte[2048];
	}
	
}
