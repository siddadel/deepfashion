package data.query;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;

public class ZIpCodeData {

	public static Map<Integer, int[]> map;

	public static final int NO_ZIP_DATA_ITEMS = 123;

	private static Set<Integer> noDataZip = new HashSet<Integer>();
	private static Set<Integer> matchFound = new HashSet<Integer>();

	static Map<Integer, int[]> getMap() throws IOException {
		if (map != null)
			return map;
		map = new HashMap<Integer, int[]>();
		CSVReader reader = new CSVReader(new FileReader(OrderConsolidate.root + "census\\14zpallnoagi.csv"));
		List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			try {
				Integer zip = Integer.parseInt(line[2]);
				int[] values = new int[line.length - 4];
				for (int w = 4; w < line.length; w++) {
					Double d = Double.parseDouble(line[w]);
					values[w - 4] = d.intValue();
				}
				map.put(zip, values);
			} catch (NumberFormatException ne) {
				ne.printStackTrace();
			}
		}
		reader.close();
		return map;
	}

	public static int[] getValues(int zipCode) throws IOException {
		Integer i = new Integer(zipCode);
		map = getMap();
		int[] ret = map.get(i);
		if (ret == null) {
			noDataZip.add(zipCode);
			return new int[NO_ZIP_DATA_ITEMS];
		}
		matchFound.add(zipCode);
		return ret;
	}

	public static void printReport() {
		System.out.println("Match found for " + matchFound.size());
//		System.out.println(matchFound);
		System.out.println(noDataZip.size() + " with no data ");
		System.out.println(noDataZip);
	}

	public static void main(String[] args) throws IOException {
		getMap();
	}

}
