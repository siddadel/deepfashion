package data.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import scala.util.parsing.json.JSON;

public abstract class ZipGeoJSONCreator {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File folder = new File("\\datafashion\\State-zip-code-GeoJSON");
		File[] jsons = folder.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().indexOf(".json") != -1;
			}
		});
		for (File f : jsons) {
			FileReader fr = new FileReader(f);
			JSONObject j = new JSONObject((new BufferedReader(fr).readLine()));
			JSONArray s = (JSONArray)j.get("features");
			System.out.println(s.length());
			for(Object o: s) {
				System.out.println(((JSONObject)o).get("properties"));
				System.out.println(((JSONObject)((JSONObject)o).get("geometry")).length());
			}
			
			System.out.println("______________");
			fr.close();
		}
	}

}
