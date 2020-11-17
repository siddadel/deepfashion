package data.vectorize;

import java.util.HashSet;

public class StyleVectorize {
	static HashSet<String> s = new HashSet<String>();

	public static void main(String[] args) {

		new Vectorize().iterate("nndata\\only-style\\features\\out", "nndata\\only-style\\classes\\out",
				new MLPInputable() {
					public String toLabelString(SimpleOrder order) {
						s.add("" + order.catalogStyle);
						return "" + order.catalogStyle;
					}

					public boolean isWritable(SimpleOrder o) {
						// return true; commented for creating set
						return true;
					}

					public String toFeatureString(SimpleOrder order) {
						return order.featureString();
					}
				});
		System.out.println(s);
	}

}
