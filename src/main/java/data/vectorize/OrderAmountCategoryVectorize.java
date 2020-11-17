package data.vectorize;

import data.query.Constants;

public class OrderAmountCategoryVectorize {
	public static void main(String[] args) {
		new Vectorize().iterate("nndata\\only-amount-category\\features\\out",
				"nndata\\only-amount-category\\labels\\out", new MLPInputable() {

					public String toLabelString(SimpleOrder order) {
						if (order.orderAmount < 50) {
							return "" + 0;
						} else if (order.orderAmount > 50 && order.orderAmount < 100) {
							return "" + 1;
						} else if (order.orderAmount > 100 && order.orderAmount < 200) {
							return "" + 2;
						} else if (order.orderAmount > 200 && order.orderAmount < 300) {
							return "" + 3;
						} else if (order.orderAmount > 300 && order.orderAmount < 400) {
							return "" + 4;
						} else if (order.orderAmount > 400) {
							return "" + 5;
						} else {
							return "" + 6;
						}
					}

					public boolean isWritable(SimpleOrder order) {
						return true;
					}

					public String toFeatureString(SimpleOrder order) {
						// return order.featureString();
						StringBuilder sb = new StringBuilder();
						sb.append(order.zipCode).append(Constants.COMMA);
						sb.append(order.phoneAreaCode).append(Constants.COMMA);
						sb.append(order.age).append(Constants.COMMA);
						sb.append(order.state);
						sb.append(Constants.COMMA);
						// Constants.builtByteArray(sb, firstName, true);
						// Constants.builtByteArray(sb, lastName, true);
						// Constants.builtByteArray(sb, email, true);
						Constants.builtByteArray(sb, order.registrationDate, true);
						Constants.builtByteArray(sb, order.dateOfBirth, true);
						sb.append(order.gender)
								// ;
								.append(Constants.COMMA);
						Constants.builtByteArray(sb, order.zipData);
						return sb.toString();

					}

				});
	}

}
