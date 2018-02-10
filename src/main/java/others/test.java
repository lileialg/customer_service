package others;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Scanner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class test {

	public static void main(String[] args)throws Exception {

	
		StringBuilder sb = new StringBuilder();
		
		Scanner scanner = new Scanner(new FileInputStream("C:\\Users\\lilei3774\\workspace-4.2\\custom\\src\\main\\webapp\\earthquakes.geojson"));
		
		while(scanner.hasNextLine()){
			sb.append(scanner.nextLine());
		}
		
		JSONObject json = JSONObject.parseObject(sb.toString());
		
		JSONArray ja = json.getJSONArray("features");
		
		Class.forName("oracle.jdbc.driver.OracleDriver");
		
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.131:1521:orcl", "scott", "tiger");
		
		Statement stmt = conn.createStatement();
		
		
		
		
		for(int i=0;i<ja.size();i++){
			JSONObject row = ja.getJSONObject(i);
			
//			{"tsunami":0,"mag":4.3,"felt":823,"id":"us2000ahub","time":1504837583700}
			
			JSONObject properties = row.getJSONObject("properties");
			
			long tsunami = properties.getLongValue("tsunami");
			
			double mag = properties.getDoubleValue("mag");
			
			long felt = properties.getLongValue("felt");
			
			String id = properties.getString("id");
			
			long time = properties.getLongValue("time");
			
			JSONArray coordinates = row.getJSONObject("geometry").getJSONArray("coordinates");
			
			double lng = coordinates.getDoubleValue(0);
			double lat = coordinates.getDoubleValue(1);
			
//			String sql = "insert into earthquake(tsunami,mag,felt,id,time,geometry) values ("+tsunami+","+mag+","+felt+",'"+id+"',"+time+",mdsys.sdo_geometry('Point("+lng+" "+lat+")',8307));";
			
			String sql = "insert into earthquake(tsunami,mag,felt,id,time,geometry) values (?,?,?,?,?,mdsys.sdo_geometry(?,8307))";
			
			PreparedStatement pstmt = conn.prepareStatement(sql);
			try {
				pstmt.setLong(1, tsunami);
				pstmt.setDouble(2, mag);
				pstmt.setLong(3,felt);
				pstmt.setString(4, id);
				pstmt.setLong(5, time);
				pstmt.setString(6,"Point("+lng+" "+lat+")");
				pstmt.executeUpdate();
				
			}catch(Exception e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			pstmt.close();
		}
		
		
	}

}
