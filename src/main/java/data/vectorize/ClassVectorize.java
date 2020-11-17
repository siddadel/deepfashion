package data.vectorize;

import data.query.Constants;

public class ClassVectorize {
	public static void main(String[] args) {

		new Vectorize().iterate("nndata\\only-class-softmax\\features\\out",
				"nndata\\only-class-softmax\\classes\\out", new MLPInputable() {
					public String toLabelString(SimpleOrder order) {
						short index = Constants.getConstantIndex(order.catalogClass, Constants.CLASS);
						byte[] array = new byte[Constants.CLASS.length];
						array[index] = 1;
						return Constants.builtByteArray(new StringBuilder(), array, false).toString();
					}

					public boolean isWritable(SimpleOrder o) {
						return true;
					}

					public String toFeatureString(SimpleOrder order) {
						return order.featureString();
					}
				});
	}

}
