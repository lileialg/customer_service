package custom.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Repository
public class OracleDao {

	@Value("${html.path}")
	private String htmlPath;

	@Value("${vector.url}")
	private String vectorUrl;

	@Value("${geojson.url}")
	private String geojsonUrl;

	@Autowired
	protected JdbcTemplate jdbc;
	
	
	public Map<String,Object> get_source(long pid){
		String sql = "select * from data_service where pid=?";
		
		Map<String,Object> m = jdbc.queryForMap(sql, pid);
		
		String return_type = m.get("RETURN_TYPE").toString();
		
		if ("geojson".equals(return_type)){
			m.put("URL", "http://localhost:8080/datasource/geojson?service_id="+m.get("PID"));
		}else if("json".equals(return_type)){
			m.put("URL", "http://localhost:8080/datasource/json?service_id="+m.get("PID"));
		}else if("vector".equals(return_type)){
			m.put("URL", "http://localhost:8080/datasource/{z}/{x}/{y}?service_id="+m.get("PID")+"&layer_name="+m.get("source_name"));
		}
		
		return m;
		
	}
	
	public Map<String,Object> get_bg_cfg(long pid){
		String sql = "select * from datavis_mb where pid=?";
		
		return jdbc.queryForMap(sql, pid);
		
	}
	
	
	public List<Map<String,Object>> list_bg_mb(){
		String sql = "select * from datavis_mb where header is not null";
		
		return jdbc.queryForList(sql);
	}
	
	public List<Map<String,Object>> list_style_mb(){
		String sql = "select * from datavis_style";
		
		return jdbc.queryForList(sql);
	}
	
	
	public List<Map<String,Object>> list_service(){
		String sql = "select * from data_service where return_type is not null";
		
		List<Map<String,Object>> list = jdbc.queryForList(sql);
		
		for(Map<String,Object> m: list){
			String return_type = m.get("RETURN_TYPE").toString();
			
			if ("geojson".equals(return_type)){
				m.put("URL", "http://localhost:8080/datasource/geojson?service_id="+m.get("PID"));
			}else if("json".equals(return_type)){
				m.put("URL", "http://localhost:8080/datasource/json?service_id="+m.get("PID"));
			}else if("vector".equals(return_type)){
				m.put("URL", "http://localhost:8080/datasource/{z}/{x}/{y}?service_id="+m.get("PID")+"&layer_name="+m.get("source_name"));
			}
		}
		
		return list;
	}
	
	
	public void add_service(JSONObject json){
		
		String source_name = json.getString("source_name");
		JSONArray ja_cols = json.getJSONArray("cols");
		String return_type = json.getString("return_type");
		String describe = json.getString("describe");
		
		
		StringBuilder sb = new StringBuilder(ja_cols.getString(0));
		
		for(int i=1;i<ja_cols.size();i++){
			sb.append(",");
			sb.append(ja_cols.getString(i));
		}
		
		String cols = sb.toString();
		
		String sql = "insert into data_service(pid,is_group,source_name,return_type,cols,describe) values (?,?,?,?,?,?)";
		
		jdbc.update(sql, new Date().getTime(),0,source_name,return_type,cols,describe);
		
	}
	
	public List<Map<String,Object>> list_task(){
		String sql = "select * from transdata_task";
		
		return jdbc.queryForList(sql);
	}
	
	
	public void trans(JSONObject jsonTab){
		 try {
			String jdbc_url = jsonTab.getString("jdbc_url");
			 String username = jsonTab.getString("username");
			 String password = jsonTab.getString("password");
			 String source_tab_name = jsonTab.getString("source_tab_name");
			 String tab_name = jsonTab.getString("tab_name");
			 long pid = new Date().getTime();
			 
			 String initsql = "insert into transdata_task(pid,jdbc_url,username,password,source_tab_name,tab_name,status) values (?,?,?,?,?,?,?)";
			 
			 
			 
			 jdbc.update(initsql, pid,jdbc_url,username,password,source_tab_name,tab_name,"进行中");
			
			String sql = "insert into "+tab_name+" select * from "+source_tab_name;
			
			jdbc.execute(sql);
			
			String finishsql = "update transdata_task set status=? where pid=?";
			
			jdbc.update(finishsql, "完成",pid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getStyles(int style_pid){
		String sql = "select * from datavis_style where style_pid=?";
		
		
		Map<String,Object> result = jdbc.queryForMap(sql, style_pid);
		
		String styles = result.get("styles").toString();
		
		
		JSONArray ja = JSONArray.parseArray(styles);
		
		JSONObject jo = new JSONObject();
		
		jo.put("describe", result.get("describe").toString());
		
		jo.put("styles", ja);
		
		return jo.toJSONString();
	}

	public List<Map<String, Object>> getDataSourceList() {

		String sql = "select * from data_source";

		return jdbc.queryForList(sql);

	}

	public List<Map<String, Object>> getTableColumns(String tabName) {

		String sql = "select column_name,data_type from user_tab_cols where"
				+ " column_name not like 'SYS_NC%' and table_name =?";

		return jdbc.queryForList(sql, tabName.toUpperCase());

	}

	public String generateSql(long gid, String cond_value) {
		StringBuilder sb = new StringBuilder("select ");

		List<Map<String, Object>> list = jdbc.queryForList(
				"select * from data_service where pid=?", gid);

		for (Map<String, Object> map : list) {
			int is_group = ((BigDecimal) map.get("is_group")).intValue();
			String return_type = map.get("return_type").toString();

			if (is_group == 0) {
				// 不分组
				if (!"vector".equals(return_type))
					sb.append(map
							.get("cols")
							.toString().toLowerCase()
							.replace("geometry",
									"sdo_util.to_wktgeometry_varchar(geometry) as geometry"));
				else
					sb.append(map.get("cols").toString());
				sb.append(" from ");
				sb.append(map.get("source_name").toString());
				List<Map<String, Object>> list2 = jdbc
						.queryForList(
								"select * from condition_info where pid=? order by cid",
								gid);

				if (list2.size() > 0) {
					String[] splits = cond_value.split(",");
					Map<String, Object> tmpMap = list2.get(0);
					sb.append(" where ");
					String colType = tmpMap.get("col_type").toString();
					String cond = tmpMap.get("cond").toString();
					String col = tmpMap.get("column_name").toString();

					if (colType.equals("geometry")) {
						sb.append(" sdo_anyinteract(geometry,");
						sb.append("sdo_geometry(sdo_util.to_wktgeometry_varchar('"
								+ splits[0] + "'),8307) = 'TRUE' ");
					} else {
						sb.append(col);
						sb.append(" ");
						sb.append(cond);
						sb.append(" ");
						if (colType.equals("Integer")) {

							sb.append(Integer.parseInt(splits[0]));
						} else if (colType.equals("Double")) {
							sb.append(Double.parseDouble(splits[0]));
						} else {
							// String

							sb.append("'");
							if ("like".equals(cond.trim()))
								sb.append("%");
							sb.append(splits[0]);
							if ("like".equals(cond.trim()))
								sb.append("%");
							sb.append("'");
						}

					}

					for (int i = 1; i < list2.size(); i++) {
						sb.append(" and ");
						tmpMap = list2.get(i);
						colType = tmpMap.get("col_type").toString();
						cond = tmpMap.get("cond").toString();
						col = tmpMap.get("column_name").toString();

						if (colType.equals("geometry")) {
							sb.append(" sdo_anyinteract(geometry,");
							sb.append("sdo_geometry(sdo_util.to_wktgeometry_varchar('"
									+ splits[i] + "'),8307) = 'TRUE' ");
						} else {
							sb.append(col);
							sb.append(" ");
							sb.append(cond);
							sb.append(" ");
							if (colType.equals("Integer")) {

								sb.append(Integer.parseInt(splits[i]));
							} else if (colType.equals("Double")) {
								sb.append(Double.parseDouble(splits[i]));
							} else {
								// String
								sb.append("'");
								if ("like".equals(cond.trim()))
									sb.append("%");
								sb.append(splits[i]);
								if ("like".equals(cond.trim()))
									sb.append("%");
								sb.append("'");
							}
						}
					}

				}

			} else {
				// 分组
				List<Map<String, Object>> list3 = jdbc.queryForList(
						"select group_value from group_info where pid=?", gid);

				sb.append(map.get("group_cols").toString());

				for (Map<String, Object> map2 : list3) {
					sb.append(",");
					sb.append(map2.get("group_value").toString());

				}
				sb.append(" from ");
				sb.append(map.get("source_name").toString());
				List<Map<String, Object>> list2 = jdbc
						.queryForList(
								"select * from condition_info where pid=? order by cid",
								gid);
				if (list2.size() > 0) {
					String[] splits = cond_value.split(",");
					Map<String, Object> tmpMap = list2.get(0);
					sb.append(" where ");
					String colType = tmpMap.get("col_type").toString();
					String cond = tmpMap.get("cond").toString();
					String col = tmpMap.get("column_name").toString();

					sb.append(col);
					sb.append(" ");
					sb.append(cond);
					sb.append(" ");
					if (colType.equals("Integer")) {

						sb.append(Integer.parseInt(splits[0]));
					} else if (colType.equals("Double")) {
						sb.append(Double.parseDouble(splits[0]));
					} else {
						// String
						sb.append("\'");
						if ("like".equals(cond.trim()))
							sb.append("%");
						sb.append(splits[0]);
						if ("like".equals(cond.trim()))
							sb.append("%");
						sb.append("\'");
					}

					for (int i = 1; i < list2.size(); i++) {
						sb.append(" and ");
						tmpMap = list2.get(i);
						colType = tmpMap.get("col_type").toString();
						cond = tmpMap.get("cond").toString();
						col = tmpMap.get("column_name").toString();

						sb.append(col);
						sb.append(" ");
						sb.append(cond);
						sb.append(" ");
						if (colType.equals("Integer")) {

							sb.append(Integer.parseInt(splits[i]));
						} else if (colType.equals("Double")) {
							sb.append(Double.parseDouble(splits[i]));
						} else {
							// String
							sb.append("\'");
							if ("like".equals(cond.trim()))
								sb.append("%");
							sb.append(splits[i]);
							if ("like".equals(cond.trim()))
								sb.append("%");
							sb.append("\'");
						}

					}

				}
				sb.append(" group by ");
				sb.append(map.get("group_cols").toString());

			}

			List<Map<String, Object>> list2 = jdbc.queryForList(
					"select * from order_info where pid=? order by order_id",
					gid);

			if (list2.size() > 0) {
				Map<String, Object> tmpMap = list2.get(0);

				sb.append(" order by ");
				sb.append(tmpMap.get("column_name").toString());
				sb.append(" ");
				sb.append(tmpMap.get("order_rule"));

				for (int i = 1; i < list2.size(); i++) {
					tmpMap = list2.get(i);

					sb.append(",");
					sb.append(tmpMap.get("column_name").toString());
					sb.append(" ");
					sb.append(tmpMap.get("order_rule"));
				}
			}
		}

		return sb.toString();

	}

	public List<Map<String, Object>> getGeojson(long service_pid,
			String cond_value) {

		String sql = this.generateSql(service_pid, cond_value);

		return jdbc.queryForList(sql);

	}

//	public String getJsonSep(int service_pid) {
//		String sql = "select source_name from data_service where pid=?";
//
//		Map<String, Object> map = jdbc.queryForMap(sql, service_pid);
//
//		String tab_name = map.get("source_name").toString();
//
//		sql = "select * from " + tab_name;
//
//		List<Map<String, Object>> list = jdbc.queryForList(sql);
//
//		JSONArray ja = new JSONArray();
//
//		for (Map<String, Object> m : list) {
//			JSONObject jo = new JSONObject();
//
//			for (Entry<String, Object> en : m.entrySet()) {
//				Object value = en.getValue();
//
//				if (value.toString().startsWith("[")) {
//					jo.put(en.getKey(),
//							JSONArray.fromString(en.getValue().toString()));
//				} else if (value.toString().startsWith("{")) {
//					jo.put(en.getKey(),
//							JSONObject.fromString(en.getValue().toString()));
//				} else {
//					jo.put(en.getKey(), en.getValue());
//				}
//			}
//
//			ja.put(jo);
//
//		}
//
//		return ja.toString();
//	}

	public List<Map<String, Object>> getProtoBuf(int service_pid,
			String cond_value, String wkt) {

		String sql = this.generateSql(service_pid, cond_value);

		if (sql.indexOf(" where ") > 0) {
			sql = sql + " and ";
		} else {
			sql = sql + " where ";
		}

		// sql = sql + "  sdo_anyinteract(geometry,sdo_geometry('"
		// + wkt + "',8307)) = 'TRUE'";
		//
		// sql =
		// "select a.*,sdo_util.to_wktgeometry_varchar(geometry) geometry2 from ("+
		// sql+") a ";

		sql = sql + "  sdo_anyinteract(geometry,sdo_geometry(?,8307)) = 'TRUE'";
		//
		sql = "select a.*,sdo_util.to_wktgeometry_varchar(geometry) geometry2 from ("
				+ sql + ") a ";

		return jdbc.queryForList(sql, wkt);

	}

	// 发布mapbox gl可视化
	public void lauch_vis_mb(int sid, String cond_value) {
		String sql = "select a.service_pid,a.layer_name,a.source_name,"
				+ "a.cond_value,a.lauch_name,b.header,b.ender,a.layer_cfg "
				+ "from datavis_service a,datavis_mb b where a.pid=b.pid and a.sid=?";

		List<Map<String, Object>> list = jdbc.queryForList(sql, sid);

		for (Map<String, Object> map : list) {
			String lauch_name = map.get("lauch_name").toString();

			try {
				PrintWriter out = new PrintWriter(htmlPath + lauch_name);

				String header = map.get("header").toString();

				out.println(header);

				// 添加source
				String source_name = map.get("source_name").toString();
				String layer_name = map.get("layer_name").toString();

				sql = "select return_type from data_service where pid=?";

				int service_pid = ((BigDecimal) map.get("service_pid"))
						.intValue();

				List<Map<String, Object>> dslist = jdbc.queryForList(sql,
						service_pid);

				String return_type = dslist.get(0).get("return_type")
						.toString();

				if ("vector".equals(return_type)) {
					out.println("map.addSource('" + source_name + "',{");
					out.println("'type':'vector',");
					out.print("'tiles':['" + vectorUrl + "?service_id="
							+ service_pid + "&layer_name=" + layer_name + "");
					if (cond_value != null && cond_value.length() > 0)
						out.print("&cond_value=" + cond_value);
					out.print("']});");
				} else if ("geojson".equals(return_type)) {
					out.println("map.addSource('" + source_name + "',{");
					out.println("'type':'geojson',");
					out.print("'data':'" + geojsonUrl + "?service_id="
							+ service_pid + "&layer_name=" + layer_name + "");
					if (cond_value != null && cond_value.length() > 0)
						out.print("&cond_value=" + cond_value);

					out.println("'");
					out.println("});");
				}

				// 添加layer
				String layer_cfg = map.get("layer_cfg").toString();
				out.println("map.addLayer(");
				out.println(layer_cfg);
				out.println(")");

				String ender = map.get("ender").toString();
				out.println(ender);
				out.flush();
				out.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 发布mapbox gl可视化
	public void lauch_vis_mb_layers(int html_pid, String cond_value) {
		String sql = "select header,ender,lauch_name,mb_source from datavis_html a,datavis_mb b where a.mb_pid=b.pid and a.html_pid=?";

		List<Map<String, Object>> list = jdbc.queryForList(sql, html_pid);

		String[] splitsCondValue = cond_value != null
				&& cond_value.length() > 0 ? cond_value.split("-") : null;

		for (Map<String, Object> map : list) {
			
			String mb_source = map.get("mb_source")!=null ? map.get("mb_source").toString():null;
			String lauch_name = map.get("lauch_name").toString();
			
			if (mb_source!= null && mb_source.length()>0){
				
					 
					String dir = htmlPath + lauch_name;
					File fileDir = new File(dir);
					if (!fileDir.exists())
						fileDir.mkdirs();
					
					File mbSourceFiles = new File(htmlPath+mb_source);
					
					File[] mbfiles = mbSourceFiles.listFiles();
					
					sql = "select a.source_name from data_service a,datavis_layer b where a.pid=b.service_pid and b.html_pid=? order by b.order_num";
					
					List<Map<String,Object>> list_source = jdbc.queryForList(sql, html_pid);
					
					for(File f: mbfiles){
						String file_name = f.getName();
						
						try {
							PrintWriter out = new PrintWriter(htmlPath+lauch_name+"/"+file_name);
							
							Scanner scanner = new Scanner(new FileInputStream(f));
							
							String c = "1";
							
							String key = "1111111111";
							
							int idx = 0;
							
							while(scanner.hasNextLine()){
								
								String line = scanner.nextLine();
								
								if (line.indexOf(key)>0){
									
									line = line.replace(key, list_source.get(idx).get("source_name").toString());
								
									int nci = Integer.parseInt(c) + 1;
									
									key = key.replaceAll(c, String.valueOf(nci));
									
									idx++;
									
									c =  String.valueOf(nci);
								}
								
								out.println(line);
								
							}
							
							scanner.close();
							
							out.flush();
							
							out.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				
				
				return;
			}
			
			

			try {
				PrintWriter out = new PrintWriter(htmlPath + lauch_name);

				String header = map.get("header").toString();

				out.println(header);

				String layerSql = "select layer_name,source_name,service_pid,layer_cfg from datavis_layer where html_pid=? order by order_num";

				List<Map<String, Object>> listLayers = jdbc.queryForList(
						layerSql, html_pid);

				for (int i = 0; i < listLayers.size(); i++) {
					String layerCondValue = null;

					if (splitsCondValue != null
							&& splitsCondValue.length - 1 >= i)
						layerCondValue = splitsCondValue[i].trim();

					Map<String, Object> layerMap = listLayers.get(i);

					// 添加source
					String source_name = layerMap.get("source_name").toString();
					String layer_name = layerMap.get("layer_name").toString();

					sql = "select return_type from data_service where pid=?";

					int service_pid = ((BigDecimal) layerMap.get("service_pid"))
							.intValue();

					List<Map<String, Object>> dslist = jdbc.queryForList(sql,
							service_pid);

					String return_type = dslist.get(0).get("return_type")
							.toString();

					if ("vector".equals(return_type)) {
						out.println("map.addSource('" + source_name + "',{");
						out.println("'type':'vector',");
						out.print("'tiles':['" + vectorUrl + "?service_id="
								+ service_pid + "&layer_name=" + layer_name
								+ "");
						if (layerCondValue != null
								&& layerCondValue.length() > 0)
							out.print("&cond_value=" + layerCondValue);
						out.print("']});");
					} else if ("geojson".equals(return_type)) {
						out.println("map.addSource('" + source_name + "',{");
						out.println("'type':'geojson',");
						out.print("'data':'" + geojsonUrl + "?service_id="
								+ service_pid + "&layer_name=" + layer_name
								+ "");
						if (layerCondValue != null
								&& layerCondValue.length() > 0)
							out.print("&cond_value=" + layerCondValue);

						out.println("'");
						out.println("});");
					}

					// 添加layer
					String layer_cfg = layerMap.get("layer_cfg").toString();
					out.println("map.addLayer(");
					out.println(layer_cfg);
					out.println(")");

				}

				String ender = map.get("ender").toString();
				out.println(ender);
				out.flush();
				out.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	//创建表
	public void create_table(String sql){
		jdbc.execute(sql);
	}

}
