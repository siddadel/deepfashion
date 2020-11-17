package data.vectorize;

import data.query.Constants;

public class OrderAmountVectorize {

	public static void main(String[] args) {
		new Vectorize().iterate("nndata\\only-amount-minimal\\features\\out", "nndata\\only-amount-minimal\\labels\\out",
				new MLPInputable() {

					public String toLabelString(SimpleOrder order) {
						return order.orderAmountString();
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
//						Constants.builtByteArray(sb, firstName, true);
//						Constants.builtByteArray(sb, lastName, true);
//						Constants.builtByteArray(sb, email, true);
						Constants.builtByteArray(sb, order.registrationDate, true);
						Constants.builtByteArray(sb, order.dateOfBirth, true);
						sb.append(order.gender)
						;
//						.append(Constants.COMMA);
//						Constants.builtByteArray(sb, zipData);
						return sb.toString();

					}

				});
	}

}
