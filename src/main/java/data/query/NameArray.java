package data.query;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;

public class NameArray {

	public static final String root = "datafashion\\";
	public static final int FIRST_NAME_INDEX = 19;
	public static final int LAST_NAME_INDEX = 20;

	public static void main(String[] args) {
		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						int[] firstName = new int[50];
						int[] lastName = new int[50];

						if (isValid(line, FIRST_NAME_INDEX)) {
							char[] chars = line[FIRST_NAME_INDEX].toCharArray();
							int i = 0;
							for (char c : chars) {
								firstName[i++] = (int) c;
							}
						}
						if (isValid(line, LAST_NAME_INDEX)) {
							char[] chars = line[LAST_NAME_INDEX].toCharArray();
							int i = 0;
							for (char c : chars) {
								lastName[i++] = (int) c;
							}
						}
					}
					System.out.println();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	static boolean isValid(String[] line, int index) {
		return (line.length > index);// && line[index] != null && !line[index].equals(""));
	}
}
