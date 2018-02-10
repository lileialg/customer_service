package custom.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Map<String,Object> release_vis(String ditu_style,String data_source,String layers){
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		System.out.println(ditu_style);
		System.out.println(data_source);
		System.out.println(layers);
		
		map.put("result", "success");
		
		map.put("address", "http://www.baidu.com");
		
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
}
