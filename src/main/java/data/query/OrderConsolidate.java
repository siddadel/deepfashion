package data.query;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;

public class OrderConsolidate {

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

	public static final String root = "datafashion\\";

	public static void printStringLine(String[] line) {
		for (String l : line) {
			System.out.print(l + ",");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };

		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			HashMap<String, Order> orders = new HashMap<String, Order>();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						if (line.length > REGISTRATION_TIME) {
							Order order = orders.get(line[ORDER_ID]);
							if (order == null) {
								order = new Order(line[ORDER_ID], line[FIRST_NAME], line[LAST_NAME], line[EMAIL],
										line[ZIPCODE], line[PHONE], line[REGISTRATION_TIME], line[ORDER_AMOUNT],
										line[ORDER_STATUS], line[PURCHASE_TIME], line[ITEM_PRICE], line[COLOR],
										line[COLOR_FAMILY], line[SIZE], line[STATE], line[ITEM_STATUS], line[GENDER],
										line[DATE_OF_BIRTH], line[CATALOG_CLASS], line[STYLE]);
								orders.put(line[ORDER_ID], order);
							} else {
								Item item = new Item(line[ITEM_PRICE], line[COLOR], line[COLOR_FAMILY], line[SIZE],
										line[ITEM_STATUS], line[CATALOG_CLASS], line[STYLE]);
								order.addItem(item);
							}
//							printStringLine(line);
//							System.out.println(Order.parseOriginalFromSerialized(order.toString().split(",")));
//							System.out.println();
						} else {
							System.err.println(line.length);
						}
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			writeToFile(orders, subfolder);
		}

	}

	static void writeToFile(HashMap<String, Order> orders, String subfolder) {
		FileWriter writer;
		try {
			int i = 0;
			int part = 0;
			writer = new FileWriter(root + subfolder + "-out" + part + ".csv");
			for (Order o : orders.values()) {
				writer.write(o.toString());
				writer.write("\n");
				i++;
				if (i > 1000) {
					i = 0;
					part++;
					writer.flush();
					writer.close();
					String f = root + subfolder + "-out" + part + ".csv";
					System.out.println(f);
					writer = new FileWriter(f);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
