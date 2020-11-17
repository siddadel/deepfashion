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

import data.query.Constants;

public class ClassLearner {

	public static final double FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE = (double) 30 / 187;

	public static void main(String[] args) throws IOException, InterruptedException {
		// countNoOfSample();
		new OrderItemsLearner().run("D:\\datafashion\\nndata\\only-class-softmax\\features\\",
				"D:\\datafashion\\nndata\\only-class-softmax\\classes\\",
				FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE, new ModelRunner() {

					public MultiLayerNetwork getModel() {
						MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(6)
								.activation(Activation.RELU).weightInit(WeightInit.XAVIER).updater(new Sgd(0.1))
								.l2(1e-4).list().layer(0, new DenseLayer.Builder().nIn(494).nOut(400).build())
								.layer(1, new DenseLayer.Builder().nIn(400).nOut(300).build())
								.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
										.activation(Activation.SOFTMAX).nIn(300).nOut(Constants.CLASS.length).build())
//								.backprop(true)
//								.pretrain(false)
								.build();

						// run the model
						MultiLayerNetwork model = new MultiLayerNetwork(conf);

						// MultiLayerNetwork model = MultiLayerNetwork.load(new File(modelPath), false);

						return model;

					}

					public void evaluate(DataSet outputDataSet, DataSet testDataSet) {
						OrderLearner.printAverageDistance(outputDataSet, testDataSet, Constants.CLASS.length);
					}

					public void fit(MultiLayerNetwork model, DataSet inputData) {
						OrderLearner.fitAndSave(model, inputData, "datafashion\\model-items-class.txt");
					}

				});
	}
}
