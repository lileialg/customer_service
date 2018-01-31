package others;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class validate_url {

	public static void main(String[] args) throws FileNotFoundException {
		File dir = new File("C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/datav/road-track2");
		
		if (!dir.exists())
			dir.mkdirs();
		
		String path = "C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/datav/road-track2/";
		
		PrintWriter outb = new PrintWriter(path+"/bundle.js");
		
		
		
		String file = "C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/datav/road-track/bundle.js";
		
		Scanner scanner = new Scanner(new FileInputStream(file));
		
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			
//			if (line.indexOf("https://raw.githubusercontent.com/uber-common/deck.gl-data/master/examples/trips/buildings.json" )>=0){
//				System.out.println(line);
//			}
			
			//
			
			
			if (line.indexOf("https://raw.githubusercontent.com/uber-common/deck.gl-data/master/examples/trips/trips.json" )>=0){
				line = line.replaceAll("https://raw.githubusercontent.com/uber-common/deck.gl-data/master/examples/trips/trips.json", "http://localhost:8080/trips.json");
			}
				outb.println(line);
			
			
		}
		
		scanner.close();
		
		outb.flush();
		
		outb.close();
		
		scanner = new Scanner(new FileInputStream("C:/Users/lilei3774/workspace-4.2/custom/src/main/webapp/datav/road-track/index.html"));
		outb = new PrintWriter(path+"/index.html");
		while(scanner.hasNextLine()){
			outb.println(scanner.nextLine());
		}
		
		scanner.close();
		
		outb.flush();
		
		outb.close();
	}

}
