package data.vectorize;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;

import data.query.ZIpCodeData;

public class Vectorize {

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
	public static final int CATALOG_CLASS = 15 - 1;
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

	final String root = "datafashion\\";

	public static void printStringLine(String[] line) {
		for (String l : line) {
			System.out.print(l + ",");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		new Vectorize().iterate("nndata\\simple\\labels\\out", "nndata\\simple\\classes\\out",
				new MLPInputable() {
					public String toLabelString(SimpleOrder order) {
						return order.allClassString();
					}

					public boolean isWritable(SimpleOrder order) {
						return true;
					}

					public String toFeatureString(SimpleOrder order) {
						return order.featureString();
					}
				});
	}

	void iterate(String featuresPath, String classesPath, MLPInputable classString) {
		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };
		HashMap<String, SimpleOrder> orders = new HashMap<String, SimpleOrder>();

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
										line[ORDER_STATUS], line[CATALOG_CLASS], line[STYLE]);
								orders.put(line[ORDER_ID], order);
							} else {
								order.incrementItem();
							}
							// printStringLine(line);
							// System.out.println(Order.parseOriginalFromSerialized(order.toString().split(",")));
							// System.out.println();
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
		writeToFile(orders, root + featuresPath, root + classesPath, classString);
		ZIpCodeData.printReport();
	}

	void writeToFile(HashMap<String, SimpleOrder> orders, String featurePath, String classesPath,
			MLPInputable classString) {
		FileWriter writer1;
		FileWriter writer2;
		try {
			int i = 0;
			int part = 0;
			writer1 = new FileWriter(featurePath + part + ".csv");
			writer2 = new FileWriter(classesPath + part + ".csv");
			List<SimpleOrder> values = new ArrayList<SimpleOrder>();
			for (SimpleOrder s : orders.values()) {
				values.add(s);
			}
			Collections.shuffle(values);
			for (SimpleOrder o : values) {
				String features = classString.toFeatureString(o);
				String classes = classString.toLabelString(o);

				if (classString.isWritable(o)) {
					writer1.write(features);
					writer1.write("\n");
					writer2.write(classes);
					writer2.write("\n");
				}
				i++;
				if (i > 10000) {
					i = 0;
					part++;
					writer1.flush();
					writer1.close();
					writer2.flush();
					writer2.close();
					writer1 = new FileWriter(featurePath + part + ".csv");
					writer2 = new FileWriter(classesPath + part + ".csv");
				}
			}
			writer1.flush();
			writer1.close();
			writer2.flush();
			writer2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
