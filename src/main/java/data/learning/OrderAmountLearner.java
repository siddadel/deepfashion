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

public class OrderAmountLearner extends OrderLearner {

	public static final double FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE = (double) 2 / 187;

	public static void main(String[] args) throws IOException, InterruptedException {
		// countNoOfSample();
		new OrderAmountLearner().run("datafashion\\nndata\\only-amount\\features\\",
				"datafashion\\nndata\\only-amount\\labels\\", FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE,
				new ModelRunner() {

					public MultiLayerNetwork getModel() {
						MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(6)
								.activation(Activation.SIGMOID).weightInit(WeightInit.XAVIER).updater(new Sgd(0.1))
								.l2(1e-4).list().layer(0, new DenseLayer.Builder().nIn(494).nOut(300).build())
								.layer(1, new DenseLayer.Builder().nIn(300).nOut(300).build())
								.layer(2, new DenseLayer.Builder().nIn(300).nOut(300).build())
								.layer(3, new DenseLayer.Builder().nIn(300).nOut(300).build())
								.layer(4, new DenseLayer.Builder().nIn(300).nOut(300).build())
								.layer(5, new DenseLayer.Builder().nIn(300).nOut(10).build())
								.layer(6, new DenseLayer.Builder().nIn(10).nOut(3).build())
								.layer(7,
										new OutputLayer.Builder(
												LossFunctions.LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR)
														.activation(Activation.RELU).nIn(3).nOut(1).build())
//								.backprop(true)
//								.pretrain(false)
								.build();

						MultiLayerNetwork model =
						new MultiLayerNetwork(conf);
//						load("datafashion\\model-amount.txt");
						return model;
					}

					public void evaluate(DataSet outputDataSet, DataSet testDataSet) {
						OrderLearner.printAverageDistance(outputDataSet, testDataSet, 1);
					}

					public void fit(MultiLayerNetwork model, DataSet inputData) {
						OrderLearner.fitAndSave(model, inputData, "datafashion\\model-amount.txt");
					}
				});

	}

}
