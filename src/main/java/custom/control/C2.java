package custom.control;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import custom.dao.OracleDao;

@RestController
@RequestMapping(value = "/cj")
public class C2 {
	@RequestMapping("/release_vis")
	public Map<String,Object> release_vis(String ditu_style,String data_source,String layers,String map_cfg,String option){
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		System.out.println(ditu_style);
		System.out.println(data_source);
		System.out.println(layers);
		
		long id = new Date().getTime();
		
		JSONObject json_data_source = JSONObject.parseObject(data_source);
		String source_name = json_data_source.getString("my_source_name");
		JSONObject source_value = json_data_source.getJSONObject("my_source_value");
		JSONObject json_map_cfg = JSONObject.parseObject(map_cfg);
		
		JSONArray jaLayers = JSONArray.parseArray(layers);
		
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream("C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/release/"+id+".html"));
			
			Scanner scanner = new Scanner(new FileInputStream("C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/vis_mb.html"));
			
			int row = 0;
			
			while(scanner.hasNextLine()){
				row++;
				
				String line = scanner.nextLine();
				
				out.println(line);
				
				if (row==56){
					out.println(" var simple = ");
					out.println(ditu_style);
//					var map = new mapboxgl.Map({
//					    container: 'map',
//					    style : simple,
//					    center: [-120, 50],
//					    zoom: 2
//					});
					out.println("var map = new mapboxgl.Map({");
					out.println("container: 'map',");
					out.println("style : simple,");
					out.println("center: ["+json_map_cfg.getDoubleValue("lng")+", "+json_map_cfg.getDoubleValue("lat")+"],");
					out.println("zoom: "+json_map_cfg.getDoubleValue("zoom"));
					out.println(",pitch :"+ json_map_cfg.getDoubleValue("pitch"));
					out.println("})");
					
					out.println("map.on('load', function() {");
					out.println("map.addSource('"+source_name+"', ");
					out.println(source_value);
					out.println(")");
					
					for(int i=0;i<jaLayers.size();i++){
						out.println("map.addLayer(");
						out.println(jaLayers.getJSONObject(i));
						out.println(")");
					}
					
					out.println("})");
					
					
				}else if ("option=null".equals(line)){
					out.println("option="+JSONObject.parseObject(option));
				}
			}
			
			out.flush();
			
			out.close();
			
			scanner.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		map.put("result", "success");
		
		map.put("address", "http://localhost:8080/release/"+id+".html");
		
		return map;
		
	}
	
	@RequestMapping("/list_task")
	public List<Map<String,Object>> list_task(){
		
		return dao.list_task();
	}
	
	@RequestMapping("/get_source")
	public Map<String,Object> get_source(long id){
		return dao.get_source(id);
	}
	
	
	@RequestMapping("/get_bg_cfg")
	public Map<String,Object> get_bg_cfg(long id){
		return dao.get_bg_cfg(id);
	}
	
	
	@RequestMapping("/list_report_style_mb")
	public List<Map<String,Object>> list_report_style_mb(){
		
		return dao.list_style_report_mb();
	}
	
	@RequestMapping("/list_style")
	public List<Map<String,Object>> list_style(){
		
		return dao.list_style_mb();
	}
	
	
	@RequestMapping("/list_bg_mb")
	public List<Map<String,Object>> list_bg_mb(){
		
		return dao.list_bg_mb();
	}
	
	
	@RequestMapping("/list_service")
	public List<Map<String,Object>> list_service(){
		
		return dao.list_service();
	}
	
	
	@RequestMapping("/add_service")
	public Map<String,Object> add_service(String json){
		
		System.out.println(json);
		
		JSONObject jo = JSONObject.parseObject(json);
		
		dao.add_service(jo);
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		map.put("result", "服务创建完成成功");
		
		return map;
	}
	
	

	@RequestMapping("/test")
	public String test(String tab_name,String attrs){
		
		System.out.println(tab_name);
		System.out.println(attrs);

		return "ok";
	}
	
	@RequestMapping("/tab_con")
	public Map<String,Object> getTabConstruct(String tab_name){
		System.out.println("------->"+tab_name);
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		map.put("name", "link");
		map.put("describe", "describe ... ...");
		
		List<Map<String,String>> attrs = new ArrayList<Map<String,String>>();
		
		Map<String,String> m1 = new HashMap<String,String>();
		m1.put("name", "link_pid");
		m1.put("type", "Integer");
		attrs.add(m1);
		
		m1 = new HashMap<String,String>();
		m1.put("name", "link_pid2");
		m1.put("type", "Integer");
		attrs.add(m1);
		
		
		map.put("attrs", attrs);
		
		return map;
	}
	@Autowired
	private OracleDao dao;
	
	//创建表
	@RequestMapping("/add_schema")
	public Map<String,Object> add_schema(String json){
		
		JSONObject jsonTab = JSONObject.parseObject(json);
		
		String tab_name = jsonTab.getString("tab_name");
		
		JSONArray jaAttr = jsonTab.getJSONArray("attrs");
		
		StringBuilder sb = new StringBuilder("create table ");
		sb.append(tab_name);
		sb.append("(");
		JSONObject firstRow = jaAttr.getJSONObject(0);
		sb.append(firstRow.getString("name"));
		sb.append(" ");
		sb.append(firstRow.getString("type"));
		
		for(int i=1;i<jaAttr.size();i++){
			sb.append(",");
			JSONObject row = jaAttr.getJSONObject(i);
			sb.append(row.getString("name"));
			sb.append(" ");
			sb.append(row.getString("type"));
		}
		
		sb.append(")");
		
		String sql = sb.toString();
		
		System.out.println(sql);
		
		dao.create_table(sql);
		
		sql = "insert into data_source(pid,tab_name) values ("+new Date().getTime()+",'"+tab_name+"')";
		
		dao.create_table(sql);
		
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		map.put("result", "创建表"+tab_name+"成功");
		
		return map;
	}
	
	static class transdata{
		
		private OracleDao dao;
		
		private JSONObject jsonTab;
		
		public transdata(OracleDao dao,JSONObject jsonTab){
			this.dao = dao;
			this.jsonTab = jsonTab;
		}
		
		
		public void trans(){
			System.out.println("-------------start-------------");
			dao.trans(jsonTab);
		}
	}
	
	@RequestMapping("/custom_data")
	public Map<String,Object> custom_data(String db_info){
		
		final JSONObject jsonTab = JSONObject.parseObject(db_info);
		
		Thread task = new Thread(){
			
			@Override
			public void run() {

				try{
					
					System.out.println("-----task-----");
					
					new transdata(dao,jsonTab).trans();
					
					System.out.println("-----task-----");
					
				}catch(Exception e){
					
				}
			
			}
			
		};
		
		task.start();
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		map.put("result", "任务提交成功");
		
		return map;
	}
	
	
	@RequestMapping("/report_styles")
	public List<Map<String,Object>> list_report_style(){
		return dao.list_report_style();
	}
	
	@RequestMapping("/get_report_style")
	public Map<String,Object> get_report_style_by_pid(int pid){
		return dao.get_report_style_by_pid(pid);
	}
}
