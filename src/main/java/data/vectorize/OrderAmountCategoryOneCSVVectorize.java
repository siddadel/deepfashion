package data.vectorize;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import data.query.Constants;

public class OrderAmountCategoryOneCSVVectorize extends Vectorize {

	public static void main(String[] args) {
		new OrderAmountCategoryOneCSVVectorize().iterate("nndata\\only-amount-category\\", "", new MLPInputable() {

			public String toLabelString(SimpleOrder order) {
				 if (order.orderAmount <= 50) {
				 return "0";
				 } else if (order.orderAmount > 50 && order.orderAmount <= 100) {
				 return "1";
				 } else if (order.orderAmount > 100) {
				 return "2";
				 } 
				 return "0";
			}

			public boolean isWritable(SimpleOrder order) {
				return true;
				
			}

			public String toFeatureString(SimpleOrder order) {
				 return order.smallFeatureString();

			}

		});
	}

	void writeToFile(HashMap<String, SimpleOrder> orders, String csvPath, String classesPath,
			MLPInputable classString) {
		FileWriter writer1;
		try {
			writer1 = new FileWriter(csvPath + "out-name.csv");
			List<SimpleOrder> values = new ArrayList<SimpleOrder>();
			for (SimpleOrder s : orders.values()) {
				values.add(s);
			}
			Collections.shuffle(values);
			int i = 0;
			for (SimpleOrder o : values) {
//				if (i < 18 * 10001) {
					String features = classString.toFeatureString(o);
					String classes = classString.toLabelString(o);

					if (classString.isWritable(o)) {
						writer1.write(features + classes);
						writer1.write("\n");
					}
//				} else {
//					break;
//				}
				i++;

			}
			writer1.flush();
			writer1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
