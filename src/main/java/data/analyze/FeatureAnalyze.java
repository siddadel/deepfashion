package data.analyze;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;

import data.query.Order;
import data.query.ZIpCodeData;
import data.vectorize.SimpleOrder;

public class FeatureAnalyze {
	public static final int ORDER_ID = 1 - 1;
	public static final int ORDER_STATUS = 2 - 1;
	public static final int PURCHASE_TIME = 3 - 1;
	public static final int ORDER_AMOUNT = 4 - 1;

	public static final int ADDRESS_1 = 5 - 1;
	public static final int ADDRESS_2 = 6 - 1;
	public static final int CITY = 7 - 1;

	public static final int STATE = 8 - 1;
	public static final int ZIPCODE = 9 - 1;
	public static final int PHONE = 10 - 1;
	public static final int EMAIL = 11 - 1;
	public static final int ITEM_STATUS = 12 - 1;
	public static final int ITEM_PRICE = 13 - 1;

	public static final int DISPLAY_NAME = 14 - 1;
	public static final int CATALOGUE_CLASS = 15 - 1;
	public static final int STYLE = 16 - 1;

	public static final int SIZE = 17 - 1;
	public static final int COLOR_FAMILY = 18 - 1;
	public static final int COLOR = 19 - 1;
	public static final int FIRST_NAME = 20 - 1;
	public static final int LAST_NAME = 21 - 1;
	// 22
	public static final int REGISTRATION_TIME = 23 - 1;
	public static final int DATE_OF_BIRTH = 24 - 1;
	public static final int GENDER = 25 - 1;

	static final String root = "<enter path>";

	public static void printStringLine(String[] line) {
		for (String l : line) {
			System.out.print(l + ",");
		}
		System.out.println();
	}

	public static void main(String[] args) throws IOException {
		iterate();
	}

	static void iterate() throws IOException {
		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		HashMap<String, SimpleOrder> orders = new HashMap<String, SimpleOrder>();

		HashMap<String, Integer> zipPurchases = new HashMap<String, Integer>();
		HashMap<String, Double> zipBusiness = new HashMap<String, Double>();
		HashMap<String, String> zipState = new HashMap<String, String>();

		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			System.out.println(root + subfolder);
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						if (line.length > REGISTRATION_TIME) {
							SimpleOrder order = orders.get(line[ORDER_ID]);
							if (order == null) {

								order = new SimpleOrder(line[ORDER_ID], line[ZIPCODE], line[PHONE], line[STATE],
										line[FIRST_NAME], line[LAST_NAME], line[EMAIL], line[REGISTRATION_TIME],
										line[DATE_OF_BIRTH], line[GENDER], line[ORDER_AMOUNT], line[PURCHASE_TIME],
										line[ORDER_STATUS], line[CATALOGUE_CLASS], line[STYLE]);
								orders.put(line[ORDER_ID], order);
							} else {
								order.incrementItem();
							}
							// printStringLine(line);
							// System.out.println(Order.parseOriginalFromSerialized(order.toString().split(",")));
							// System.out.println();

							String zip = String.format("%05d", Order.parseZipCode(line[ZIPCODE]));
							Integer p = zipPurchases.get(zip);
							Double b = zipBusiness.get(zip);
							if (p != null) {
								zipPurchases.put(zip, new Integer(p.intValue() + 1));
								zipBusiness.put(zip, new Double(b + Double.parseDouble(line[ITEM_PRICE])));
							} else {
								zipPurchases.put(zip, 1);
								zipBusiness.put(zip, Double.parseDouble(line[ITEM_PRICE]));
								zipState.put(zip, line[STATE]);
							}
						} else {
							System.err.println(line.length);
						}
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		FileWriter fw = new FileWriter(new File(root + "zipBusiness.csv"));
		Set<String> keys = zipPurchases.keySet();
		for (String z : keys) {
			System.out.println(z + " " + zipPurchases.get(z) + "," + zipBusiness.get(z));
			fw.write(z + "," + zipPurchases.get(z) + "," + zipState.get(z) + "," + zipBusiness.get(z));
			int[] data = ZIpCodeData.getValues(Order.parseZipCode(z));
			for (int d : data) {
				fw.write("," + d);
			}
			fw.write("\n");
		}
		fw.flush();
		fw.close();

	}

}
