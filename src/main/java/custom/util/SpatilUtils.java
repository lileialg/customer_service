package custom.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SpatilUtils {

	
	public static Map<String,Object> wkt2geojson(String wkt){
		
		try {
			Geometry geom = new WKTReader().read(wkt);
			
			String type = geom.getGeometryType();
			Map<String,Object> map = new HashMap<String,Object>();
			if ("Point".equals(type)){
//				"geometry": {
//			    "type": "Point",
//			    "coordinates": [125.6, 10.1]
//			  }
				
				
				
				map.put("type", "Point");
				
				List<Double> list = new ArrayList<Double>();
				
				list.add(geom.getCentroid().getX());
				list.add(geom.getCentroid().getY());
				
				map.put("coordinates", list);
			}else if ("LineString".equals(type)){
//				{
//			           "type": "LineString",
//			           "coordinates": [
//			               [-170, 10],
//			               [170, 11]
//			           ]
//			       }
				
				
				map.put("type", "LineString");
				List<List<Double>> list = new ArrayList<List<Double>>();
				
				for(Coordinate c:geom.getCoordinates()){
					List<Double> xy = new ArrayList<Double>();
					
					xy.add(c.x);
					xy.add(c.y);
					list.add(xy);
				}
				
				map.put("coordinates", list);
				
			}else if ("Polygon".equals(type)){
//				{
//			           "type": "Polygon",
//			           "coordinates": [
//			               [
//			                   [-10.0, -10.0],
//			                   [10.0, -10.0],
//			                   [10.0, 10.0],
//			                   [-10.0, -10.0]
//			               ]
//			           ]
//			       }
				
				map.put("type", "LineString");
				List<List<List<Double>>> list = new ArrayList<List<List<Double>>>();
				List<List<Double>> mlist = new ArrayList<List<Double>>();
				for(Coordinate c:geom.getCoordinates()){
					List<Double> xy = new ArrayList<Double>();
					
					xy.add(c.x);
					xy.add(c.y);
					mlist.add(xy);
				}
				
				map.put("coordinates", list);
			}
			
			return map;
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
