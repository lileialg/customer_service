package others;

import java.io.File;

public class test {

	public static void main(String[] args)throws Exception {


		File[] files = new File("c:/").listFiles();
		
		for(File f: files){
			System.out.println(f.getName());
		}
		
	}

}
