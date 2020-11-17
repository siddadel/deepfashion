package data.learning;

import java.io.IOException;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class OrderAmountCategoryOneCSVLearner extends OrderLearner {

	public static void main(String[] args) throws IOException, InterruptedException {
		// countNoOfSample();
		new OrderAmountCategoryOneCSVLearner().run("datafashion\\nndata\\only-amount-category\\out-name.csv", 30, 3,
				new ModelRunner() {

					public MultiLayerNetwork getModel() {
			            int numInputs = 30;  
			            int numOutputs = 3;
			            int numHiddenNodes = 2 * numInputs + numOutputs;
			            int seed = 123456;
			            double learningRate = 0.001;

				          MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				                  .seed(seed)
				                  .biasInit(1)
				                  .l2(1e-4)
				                  .updater(new Nesterovs(learningRate, 0.9))
				                  .list()
				                  .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
				                      .weightInit(WeightInit.XAVIER)
				                      .activation(Activation.RELU)
				                      .build())
				                  .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
				                      .weightInit(WeightInit.XAVIER)
				                      .activation(Activation.RELU)
				                      .build())
				                  .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
				                      .weightInit(WeightInit.XAVIER)
				                      .activation(Activation.SOFTMAX)
				                      .nIn(numHiddenNodes).nOut(numOutputs).build())
				                  .build();

						MultiLayerNetwork model
						= new MultiLayerNetwork(conf);
//						 = load("datafashion\\model-amount-one-5csv.txt");

						return model;
					}

					public void evaluate(DataSet outputData, DataSet testData) {
						// OrderLearner.printAverageDistance(outputData, testData, 3);
						Evaluation eval = new Evaluation();
						eval.eval(testData.getLabels(), outputData.getLabels());
						System.out.println(eval.stats());
					}

					public void fit(MultiLayerNetwork model, DataSet inputData) {
						OrderLearner.fitAndSave(model, inputData, "datafashion\\model-amount-one-7csv.txt");
					}

				}, 50 * 10001, "datafashion\\model-amount-one-7csv.txt", true);
	}

}
