package data.vectorize;

import java.io.IOException;

public class OrderStatusVectorize {
	static int noOfShipped = 0;

	public static void main(String[] args) throws IOException {
		new Vectorize().iterate("nndata\\only-status-notshipped\\features\\out",
				"nndata\\only-status-notshipped\\classes\\out", new MLPInputable() {
					public String toLabelString(SimpleOrder order) {
						return "" + (order.orderStatus != 2 ? 1 : 0);
					}

					public boolean isWritable(SimpleOrder o) {
						if (o.orderStatus == 2 && noOfShipped <= 30000) {
							noOfShipped++;
							return true;
						} else if (o.orderStatus != 2) {
							return true;
						}
						return false;
					}
					
					public String toFeatureString(SimpleOrder order) {
						return order.featureString();
					}
				});

	}

}
