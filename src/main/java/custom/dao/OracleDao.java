package custom.dao;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

	public List<Map<String, Object>> getDataSourceList() {

		String sql = "select * from data_source";

		return jdbc.queryForList(sql);

	}

	public List<Map<String, Object>> getTableColumns(String tabName) {

		String sql = "select column_name,data_type from user_tab_cols where"
				+ " column_name not like 'SYS_NC%' and table_name =?";

		return jdbc.queryForList(sql, tabName.toUpperCase());

	}

	public String generateSql(int gid, String cond_value) {
		StringBuilder sb = new StringBuilder("select ");

		List<Map<String, Object>> list = jdbc.queryForList(
				"select * from data_service where pid=?", gid);

		for (Map<String, Object> map : list) {
			int is_group = ((BigDecimal) map.get("is_group")).intValue();
			String return_type = map.get("return_type").toString();

			if (is_group == 0) {
				// 不分组
				if (!"vector".equals(return_type))
					sb.append(map.get("cols").toString().replace("geometry", "sdo_util.to_wktgeometry_varchar(geometry) as geometry"));
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
	
	
	public List<Map<String,Object>> getGeojson(int service_pid,String cond_value){
		
		String sql = this.generateSql(service_pid, cond_value);
		
		return jdbc.queryForList(sql);
		
	}
	
	public List<Map<String,Object>> getProtoBuf(int service_pid,String cond_value,String wkt){
		
		String sql = this.generateSql(service_pid, cond_value);
		
		if (sql.indexOf(" where ")>0){
			sql = sql + " and ";
		}else{
			sql = sql +" where ";
		}

//		sql = sql + "  sdo_anyinteract(geometry,sdo_geometry('"
//				+ wkt + "',8307)) = 'TRUE'";
//		
//		sql = "select a.*,sdo_util.to_wktgeometry_varchar(geometry) geometry2 from ("+ sql+") a ";
		
		sql = sql + "  sdo_anyinteract(geometry,sdo_geometry(?,8307)) = 'TRUE'";
//		
		sql = "select a.*,sdo_util.to_wktgeometry_varchar(geometry) geometry2 from ("+ sql+") a ";
		
		return jdbc.queryForList(sql,wkt);
		
	}
	
	//发布mapbox gl可视化
	public void lauch_vis_mb(int sid,String cond_value){
		String sql = "select a.service_pid,a.layer_name,a.source_name,"
				+ "a.cond_value,a.lauch_name,b.header,b.ender,a.layer_cfg "
				+ "from datavis_service a,datavis_mb b where a.pid=b.pid and a.sid=?";
		
		List<Map<String,Object>> list = jdbc.queryForList(sql,sid);
		
		for(Map<String,Object> map : list){
			String lauch_name = map.get("lauch_name").toString();
			
			try {
				PrintWriter out = new PrintWriter(htmlPath+lauch_name);
				
				String header = map.get("header").toString();
				
				out.println(header);
				
				//添加source
				String source_name = map.get("source_name").toString();
				String layer_name = map.get("layer_name").toString();
				
				sql = "select return_type from data_service where pid=?";
				
				int service_pid = ((BigDecimal)map.get("service_pid")).intValue();
				
				List<Map<String,Object>> dslist = jdbc.queryForList(sql, service_pid);
				
				String return_type = dslist.get(0).get("return_type").toString();
				
				if ("vector".equals(return_type)){
					out.println("map.addSource('"+source_name+"',{");
					out.println("'type':'vector',");
					out.print("'tiles':['"+vectorUrl+"?service_id="+service_pid+"&layer_name="+layer_name+"");
					if (cond_value!= null && cond_value.length()>0)
						out.print("&cond_value="+cond_value);
					out.print("']});");
				}else if ("geojson".equals(return_type)){
					out.println("map.addSource('"+source_name+"',{");
					out.println("'type':'geojson',");
					out.print("'data':'"+geojsonUrl+"?service_id="+service_pid+"&layer_name="+layer_name+"");
					if (cond_value!= null && cond_value.length()>0)
						out.print("&cond_value="+cond_value);
					
					out.println("'");
					out.println("});");
				}
				
				//添加layer
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
	
	
	
	//发布mapbox gl可视化
	public void lauch_vis_mb_layers(int html_pid,String cond_value){
			String sql = "select header,ender,lauch_name from datavis_html a,datavis_mb b where a.mb_pid=b.pid and a.html_pid=?";
			
			List<Map<String,Object>> list = jdbc.queryForList(sql,html_pid);
			
			String[] splitsCondValue = cond_value!= null && cond_value.length()>0?cond_value.split("-"):null;
			
			for(Map<String,Object> map : list){
				String lauch_name = map.get("lauch_name").toString();
				
				try {
					PrintWriter out = new PrintWriter(htmlPath+lauch_name);
					
					String header = map.get("header").toString();
					
					out.println(header);
					
					String layerSql = "select layer_name,source_name,service_pid,layer_cfg from datavis_layer where html_pid=? order by order_num";
					
					List<Map<String,Object>> listLayers = jdbc.queryForList(layerSql, html_pid);
					
					for(int i=0;i<listLayers.size();i++){
						String layerCondValue = null;
						
						if (splitsCondValue!=null && splitsCondValue.length-1>=i)
							layerCondValue = splitsCondValue[i].trim();
						
						Map<String,Object> layerMap = listLayers.get(i);
						
						//添加source
						String source_name = layerMap.get("source_name").toString();
						String layer_name = layerMap.get("layer_name").toString();
						
						sql = "select return_type from data_service where pid=?";
						
						int service_pid = ((BigDecimal)layerMap.get("service_pid")).intValue();
						
						List<Map<String,Object>> dslist = jdbc.queryForList(sql, service_pid);
						
						String return_type = dslist.get(0).get("return_type").toString();
						
						if ("vector".equals(return_type)){
							out.println("map.addSource('"+source_name+"',{");
							out.println("'type':'vector',");
							out.print("'tiles':['"+vectorUrl+"?service_id="+service_pid+"&layer_name="+layer_name+"");
							if (layerCondValue!= null && layerCondValue.length()>0)
								out.print("&cond_value="+layerCondValue);
							out.print("']});");
						}else if ("geojson".equals(return_type)){
							out.println("map.addSource('"+source_name+"',{");
							out.println("'type':'geojson',");
							out.print("'data':'"+geojsonUrl+"?service_id="+service_pid+"&layer_name="+layer_name+"");
							if (layerCondValue!= null && layerCondValue.length()>0)
								out.print("&cond_value="+layerCondValue);
							
							out.println("'");
							out.println("});");
						}
						
						//添加layer
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

}
