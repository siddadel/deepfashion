package data.vectorize;

import java.io.File;

public class ErrorImages {

	public static void main(String[] args) {
		File[] files = new File("datafashion\\images").listFiles();
		
		for(File f: files) {
			if(f.length()==0) {
				String name = f.getName().replaceAll(".jpg", "");
				String command = "wget -O "+name+".jpg \"https://images.nyandcompany.com/is/image/NewYorkCompany/"+name+"?$productlist2$\"";
				System.out.println(command);
			}
		}
	}

}
