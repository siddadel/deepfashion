package data.vectorize;

public class ItemSizeVectorize {
	public static void main(String[] args) {
		new Vectorize().iterate("nndata\\only-itemsize\\features\\out", "nndata\\only-itemsize\\labels\\out", new MLPInputable() {
			public String toLabelString(SimpleOrder order) {
				return ""+order.noOfItems;
			}
			
			public boolean isWritable(SimpleOrder order) {
				return true;
			}
			public String toFeatureString(SimpleOrder order) {
				return order.smallFeatureString();
			}
		});
	}
}
