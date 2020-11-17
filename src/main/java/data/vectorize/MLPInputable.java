package data.vectorize;

public interface MLPInputable {
	
	public String toLabelString(SimpleOrder order);
	public boolean isWritable(SimpleOrder order);
	public String toFeatureString(SimpleOrder order);

}
