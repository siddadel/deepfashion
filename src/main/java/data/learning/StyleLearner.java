package data.learning;

import java.io.IOException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class StyleLearner extends OrderLearner {
	public static final double FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE = (double) 50 / 187;

	public static void main(String[] args) throws IOException, InterruptedException {
		// countNoOfSample();
		new StyleLearner().run("datafashion\\nndata\\only-style\\features\\",
				"datafashion\\nndata\\only-style\\classes\\",
				FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE, new ModelRunner() {

					public MultiLayerNetwork getModel() {
						MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(6)
								.activation(Activation.RELU).weightInit(WeightInit.XAVIER).updater(new Sgd(0.1))
								.l2(1e-4).list().layer(0, new DenseLayer.Builder().nIn(494).nOut(30).build())
								.layer(1, new DenseLayer.Builder().nIn(30).nOut(3).build())
								.layer(2,
										new OutputLayer.Builder(
												LossFunctions.LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR)
														.activation(Activation.RELU).nIn(3).nOut(1).build())
//								.backprop(true)
//								.pretrain(false)
								.build();

						// run the model
						MultiLayerNetwork model = new MultiLayerNetwork(conf);
						return model;
					}

					public void evaluate(DataSet outputDataSet, DataSet testDataSet) {
						OrderLearner.printAverageDistance(outputDataSet, testDataSet, 1);
					}

					public void fit(MultiLayerNetwork model, DataSet inputData) {
						OrderLearner.fitAndSave(model, inputData,  "datafashion\\model-items-style.txt");
					}
				});
	}
}
