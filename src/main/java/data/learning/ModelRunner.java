package data.learning;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;

public interface ModelRunner {

	public MultiLayerNetwork getModel();

	public void evaluate(DataSet outputDataSet, DataSet testDataSet);

	public void fit(MultiLayerNetwork model, DataSet inputData);

}
