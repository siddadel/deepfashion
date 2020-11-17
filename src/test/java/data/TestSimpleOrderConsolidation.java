package data;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import com.opencsv.CSVReader;

import data.query.Constants;
import data.query.Order;
import data.query.OrderConsolidate;
import data.vectorize.SimpleOrder;

public class TestSimpleOrderConsolidation {
	public static void main(String[] args) {
		File[] files = new File(OrderConsolidate.root+"simple").listFiles();
		HashMap<String, SimpleOrder> orders = new HashMap<String, SimpleOrder>();
		for (File f : files) {
			try {
				System.out.println(f);
				CSVReader reader = new CSVReader(new FileReader(f));
				List<String[]> allLines = reader.readAll();
				for (String[] line : allLines) {
					System.out.println();
					System.out.println("_________________");
					OrderConsolidate.printStringLine(line);
					System.out.println(SimpleOrder.parseOriginalFromSerialized(line));
					System.out.println("_________________");
					System.out.println();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void test() {
		StringBuilder sb = new StringBuilder();
		Constants.builtByteArray(sb,Order.parseDate(""), true);
		System.out.println(sb.toString());
		String[] tokens =sb.toString().split(",");
		System.out.println(tokens.length);
		System.out.println(Order.getStringForDateArray(tokens, 0));
	}

}
