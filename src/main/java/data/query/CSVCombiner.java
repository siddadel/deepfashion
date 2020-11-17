package data.query;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVCombiner {

	public static final String root = "datafashion\\";
	static Logger logger = Logger.getLogger("CSV Combiner");

	public static void main(String[] args) throws IOException {
		analyze();
	}

	static void readBigFile() throws IOException {
		logger.info("Total memory " + Runtime.getRuntime().totalMemory());
		logger.info("Free memory " + Runtime.getRuntime().freeMemory());

		logger.info("Memory usage" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		CSVReader reader = new CSVReader(new FileReader(root + File.separator + "out2.csv"));
		long t = System.currentTimeMillis();
		List<String[]> allLines = reader.readAll();
		logger.info("Time elapsed" + (System.currentTimeMillis() - t));
		logger.info("Memory usage" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		t = System.currentTimeMillis();
		CSVWriter writer = new CSVWriter(new FileWriter(root + File.separator + "out3.csv"));
		writer.writeAll(allLines, false);
		logger.info("Time elapsed" + (System.currentTimeMillis() - t));
		writer.close();
		reader.close();
	}

	static void writeInOne() throws IOException {

		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		CSVWriter writer = new CSVWriter(new FileWriter(root + File.separator + "out2.csv"));

		double sum = 0;
		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					writer.writeAll(allLines, false);
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("" + f);
				}
			}
		}
		logger.info("" + sum);
		writer.close();
	}

	static void analyze() throws IOException {

		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		Set<String> set = new HashSet<String>();

		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						for (int i = 0; i < line.length; i++) {
							set.add(line[OrderConsolidate.ITEM_STATUS]);
						}
					}

					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
					logger.info(f.toString());
				}
			}
		}

		logger.info(set.toString());

	}

	static void addToSet(Set set, int index, String[] line) {
		if (line.length > index) {
			set.add(line[index].toLowerCase());
		}
	}

}
