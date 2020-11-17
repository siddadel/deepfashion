package data.vectorize;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.opencsv.CSVReader;

import data.query.Item;
import data.query.OrderConsolidate;

public class ZipCodeConsolidate {
	public static int SKU = 0;
	public static int NUMBER_OF_PURCHASE = 1;
	public static int COLOR_FAMILY = 2;
	public static int CATALOG_CLASS = 3;
	public static int STYLE = 4;
	public static int PRICE = 5;
	public static int ZIPCODE = 6;

	private String[] params = null;

	public ZipCodeConsolidate(String[] params) {
		this.params = params;

	}

	public String toString() {
		return params[SKU] + "," + params[NUMBER_OF_PURCHASE] + "," + params[COLOR_FAMILY] + "," + params[CATALOG_CLASS]
				+ "," + params[STYLE] + "," + params[PRICE] + "," + params[ZIPCODE]+ ","+ Item.parseColorFamily(params[COLOR_FAMILY]);
	}

	public static final String root = "datafashion\\pos\\";

	static HashSet<String> errorColors = new HashSet<String>();
	static HashMap<String, ZipCodeConsolidate> table = new HashMap<String, ZipCodeConsolidate>();

	public static void main(String[] args) throws IOException {
		String[] subfolders = new String[] { "2015_jan2jun", "2015_jul2dec", "2016_jan2jun", "2016_jul2dec" };

		CSVReader colorTable = new CSVReader(new FileReader(new File("datafashion\\csv\\colors.csv")));
		List<String[]> colorTableLines = colorTable.readAll();

		for (String subfolder : subfolders) {
			File[] files = new File(root + subfolder).listFiles();
			for (File f : files) {
				try {
					CSVReader reader = new CSVReader(new FileReader(f));
					List<String[]> allLines = reader.readAll();
					for (String[] line : allLines) {
						String colorId = findColorId(line[OrderConsolidate.COLOR], colorTableLines);
						if (line.length > OrderConsolidate.REGISTRATION_TIME && colorId != null) {
							String sku = String.format("%04d", Integer.parseInt(line[OrderConsolidate.CATALOG_CLASS]))
									+ String.format("%04d", Integer.parseInt(line[OrderConsolidate.STYLE])) + "_"
									+ String.format("%03d", Integer.parseInt(colorId));
							
							String zipCode = line[OrderConsolidate.ZIPCODE];
							
							String key = zipCode+"_"+sku;
							ZipCodeConsolidate image = table.get(key);
							if (image == null) {
								table.put(key,
										new ZipCodeConsolidate(new String[] { sku, "1", line[OrderConsolidate.COLOR_FAMILY],
												line[OrderConsolidate.CATALOG_CLASS], line[OrderConsolidate.STYLE],
												line[OrderConsolidate.ITEM_PRICE],line[OrderConsolidate.ZIPCODE] }));

							} else {
								image.params[NUMBER_OF_PURCHASE] = ""
										+ (Integer.parseInt(image.params[NUMBER_OF_PURCHASE]) + 1);

							}

						}
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
		System.out.println(errorColors);
		System.out.println(errorColors.size());
		writeToFile(table);
		colorTable.close();
	}

	static String findColorId(String color, List<String[]> colorTableLines) throws IOException {
		String ret = null;

		for (String[] line : colorTableLines) {
			if (line[5].trim().equals(color.trim())) {
				return line[4];
			}
		}
		errorColors.add(color);
		return ret;
	}

	static void writeToFile(HashMap<String, ZipCodeConsolidate> table) {
		FileWriter writer;
		try {
			writer = new FileWriter(root + "product_zip.csv");
			writer.write("SKU,NUMBER_OF_PURCHASE,COLOR_FAMILY,CATALOG_CLASS,STYLE,PRICE,ZIPCODE,COLOR_FAMILY_ID\n");

			for (String s : table.keySet()) {
				ZipCodeConsolidate image = table.get(s);
				writer.write(image.toString() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
