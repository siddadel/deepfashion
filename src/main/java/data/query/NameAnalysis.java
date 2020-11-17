package data.query;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;

public class NameAnalysis {

	public static final String root = "datafashion\\";

	static void nameArray() {

		int totalFNLetters = 0;
		int totalLNLetters = 0;
		int totalFNNames = 0;
		int totalLNNames = 0;

		String firstName = "";
		String lastName = "";

		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						if (isValid(line, 19)) {
							if (firstName.length() < line[19].trim().length()) {
								firstName = line[19];
							}
							totalFNLetters += line[19].length();
							totalFNNames++;
//							if (line[19].length() > 12)
//								System.out.println(line[19]);
						}
						if (isValid(line, 20)) {
							if (lastName.length() < line[20].trim().length()) {
								lastName = line[20];
							}
							totalLNLetters += line[20].length();
							totalLNNames++;
							if (line[20].length() > 12)
								System.err.println(line[20]);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(firstName);
		System.out.println(lastName);
		System.out.println(totalFNLetters);
		System.out.println(totalFNNames);
		System.out.println(totalLNLetters);
		System.out.println(totalLNNames);

		System.out.println(((double) totalFNLetters / totalFNNames));
		System.out.println(((double) totalLNLetters / totalLNNames));
	}

	public static boolean isValid(String[] line, int index) {
		return (line.length > index);// && line[index] != null && !line[index].equals(""));
	}

	public static void main(String[] args) {
		nameArray();
	}

}
