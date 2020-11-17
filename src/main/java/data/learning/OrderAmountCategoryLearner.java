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

public class OrderAmountCategoryLearner {
	public static final double FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE = (double) 20 / 187;

	public static void main(String[] args) throws IOException, InterruptedException {
		// countNoOfSample();
		new OrderItemsLearner().run("datafashion\\nndata\\only-amount-category\\features\\",
				"datafashion\\nndata\\only-amount-category\\labels\\", FRACTION_OF_DATA_TO_USE_FOR_MEMORY_SHORTAGE,
				new ModelRunner() {

					public MultiLayerNetwork getModel() {
						MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(12534)
								.activation(Activation.SIGMOID).weightInit(WeightInit.XAVIER).updater(new Sgd(0.001))
								.l2(1e-4).list().layer(0, new DenseLayer.Builder().nIn(3).nOut(3).build())
								.layer(1, new DenseLayer.Builder().nIn(3).nOut(3).build())
								.layer(2, new DenseLayer.Builder().nIn(3).nOut(3).build())
								.layer(3,
										new OutputLayer.Builder(
												LossFunctions.LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR)
														.activation(Activation.RELU).nIn(3).nOut(1).build())
								.build();

						
//						MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//					            .seed(125)    //Random number generator seed for improved repeatability. Optional.
//					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//					            .weightInit(WeightInit.XAVIER)
//					            .updater(new Adam(0.001))
//					            .list()
//					            .layer(0, new LSTM.Builder()
//					                        .nIn(140)
//					                        .nOut(5000)
//					                        .activation(Activation.RELU)
//					                        .build())
//					            .layer(1, new LSTM.Builder()
//					                        .nIn(5000)
//					                        .nOut(5000)
//					                        .activation(Activation.RELU)
//					                        .build())
//					            .layer(2, new LSTM.Builder()
//					                        .nIn(5000)
//					                        .nOut(5000)
//					                        .activation(Activation.RELU)
//					                        .build())
//					            .layer(3, new RnnOutputLayer.Builder()
//					                        .activation(Activation.SOFTMAX)
//					                        .lossFunction(LossFunction.MCXENT)
//					                        .nIn(5000)
//					                        .nOut(6)
//					                        .build())
//					            .pretrain(false).backprop(true).build();

//						// run the model
						MultiLayerNetwork model 
						= new MultiLayerNetwork(conf);
//						= OrderItemsLearner.load("datafashion\\model-items.txt");
						return model;						
					}

					public void evaluate(DataSet outputDataSet, DataSet testDataSet) {
						OrderLearner.printAverageDistance(outputDataSet, testDataSet, 1);
					}

					public void fit(MultiLayerNetwork model, DataSet inputData) {
						OrderLearner.fitAndSave(model, inputData, "datafashion\\model-items.txt");
					}

				});
	}

}
