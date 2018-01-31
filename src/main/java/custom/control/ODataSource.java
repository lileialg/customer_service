package custom.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.ecc.vectortile.VectorTileEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mercator.TileUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import custom.dao.OracleDao;
import custom.util.SpatilUtils;

@RestController
@RequestMapping(value = "/datasource")
public class ODataSource {
	
	@Autowired
	private OracleDao dao;

	@RequestMapping(value = "/ds")
	public List<Map<String,Object>> getDataSource(){
		return dao.getDataSourceList();
	}
	
	@RequestMapping(value = "/struct")
	public List<Map<String,Object>> getDataStructure(String name){
		return dao.getTableColumns(name);
	}
	
	
	@RequestMapping(value = "/geojson")
	public Map<String,Object> geojson(int service_id,String cond_value){

		Map<String,Object> map = new HashMap<String,Object>();
		
		List<Map<String,Object>> list = dao.getGeojson(service_id, cond_value);
		
		List<Map<String,Object>> newList = new ArrayList<Map<String,Object>>();
		
		for(Map<String,Object> m : list){
			Map<String,Object> nm = new HashMap<String,Object>();
			nm.put("type", "Feature");
			Map<String,Object> prop = new HashMap<String,Object>();
			for(Entry<String,Object> en :m.entrySet()){
				if (en.getKey().equalsIgnoreCase("geometry")){
					String wkt = en.getValue().toString();
					nm.put("geometry", SpatilUtils.wkt2geojson(wkt));
				}else{
					prop.put(en.getKey().toLowerCase(), en.getValue());
				}
			}
			
			nm.put("properties", prop);
			
			newList.add(nm);
		}
		map.put("type", "FeatureCollection");
		map.put("features", newList);
		
		return map;
	}
	
	
	@RequestMapping(value = "/json")
	public List<Map<String,Object>> json(int service_id,String cond_value){

		
		List<Map<String,Object>> list = dao.getGeojson(service_id, cond_value);
		
		List<Map<String,Object>> newList = new ArrayList<Map<String,Object>>();
		
		for(Map<String,Object> m:list){
			Map<String,Object> nm = new HashMap<String,Object>();
			for(Entry<String,Object> en:m.entrySet()){
				nm.put(en.getKey().toLowerCase(), en.getValue());
			}
			newList.add(nm);
		}
		
		return newList;
	}
	
	
//	@RequestMapping(value = "/collection")
//	public String collection(int service_id){
//		return dao.getJsonSep(service_id);
//	}
	
	
	@RequestMapping(value = "/{z}/{x}/{y}",produces = "aapplication/x-protobuf")
	public byte[] getSum(@PathVariable int x, @PathVariable int y,
			@PathVariable int z,int service_id,String cond_value,String layer_name) {
		
		String tile = TileUtils.parseXyz2Bound(x, y, z);
		
		List<Map<String,Object>> list = dao.getProtoBuf(service_id, cond_value, tile);
		VectorTileEncoder vte = new VectorTileEncoder(4096, 16, false);
		for(Map<String,Object> map : list){
			String wkt = (String) map.get("geometry2");

			Geometry geom = null;
			try {
				geom = new WKTReader().read(wkt);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			TileUtils.convert2Piexl(x, y, z, geom);
			
			map.remove("geometry2");
			map.remove("geometry");
			
			vte.addFeature(layer_name, map, geom);
		}
		
		return vte.encode();
	}
	
	
	@RequestMapping(value = "/release")
	public Map<String,String> release(int service_id,String cond_value){

//		dao.lauch_vis_mb(service_id, cond_value);
		dao.lauch_vis_mb_layers(service_id, cond_value);
		
		Map<String,String> map = new HashMap<String,String>();
		
		map.put("result", "success");
		
		return map;
	}
	
}
