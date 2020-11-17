package deep.learning.cnn;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VGGClassifier {
	protected static final Logger log = LoggerFactory.getLogger(VGGClassifier.class);

	protected static int width = 224;
	protected static int height = 224;

	protected static int channels = 3;
	protected static int batchSize = 32;

	protected static long seed = 42;
	protected static Random rng = new Random(seed);
	protected static int epochs = 15;
	protected static double splitTrainTest = 0.8;
	protected static boolean save = true;
	protected static int maxPathsPerLabel = 0;

	public void run(String[] args) throws Exception {
		enableCuda();
		log.info("Load data....");
		/**
		 * cd Data Setup -> organize and limit data file paths: - mainPath = path to
		 * image files - fileSplit = define basic dataset split with limits on format -
		 * pathFilter = define additional file load filter to limit size and balance
		 * batch content
		 **/
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		File mainPath = new File("datafashion\\images-resize-240-320\\Number\\resize");
		FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
		int numExamples = toIntExact(fileSplit.length());
		int numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length; // This only works if your root is
																					// clean: only label subdirs.
		System.out.println(numLabels);
		System.out.println(numExamples);

		BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels,
				maxPathsPerLabel);

		InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
		InputSplit trainData = inputSplit[0];
		InputSplit testData = inputSplit[1];

		log.info("Build model....");

		ZooModel zooModel = VGG16.builder().build();
		ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained(PretrainedType.IMAGENET);
		log.info(vgg16.summary());

		// Decide on a fine tune configuration to use.
		// In cases where there already exists a setting the fine tune setting will
		// override the setting for all layers that are not "frozen".
		FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder().updater(new Nesterovs(5e-5)).seed(seed)
				.build();

		// Construct a new model with the intended architecture and print summary
		ComputationGraph vgg16Transfer = new TransferLearning.GraphBuilder(vgg16).fineTuneConfiguration(fineTuneConf)
				.setFeatureExtractor("fc2")
				// the specified layer and below are "frozen"
				.removeVertexKeepConnections("predictions")
				// replace the functionality of the final vertex
				.addLayer("predictions",
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nIn(4096)
								.nOut(numLabels).weightInit(new NormalDistribution(0, 0.2 * (2.0 / (4096 + numLabels))))
								.activation(Activation.SOFTMAX).build(),
						"fc2")
				.build();
		log.info(vgg16Transfer.summary());

//		vgg16Transfer.setListeners(new ScoreIterationListener(100));
//		UIServer uiServer = UIServer.getInstance();
//		StatsStorage statsStorage = new InMemoryStatsStorage();
//		uiServer.attach(statsStorage);
//		vgg16Transfer.setListeners(new StatsListener(statsStorage), new ScoreIterationListener(1));
		
		ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
		DataSetIterator trainIter;
		recordReader.initialize(trainData, null);
		trainIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);

		recordReader = new ImageRecordReader(height, width, channels, labelMaker);
		DataSetIterator testIter;
		recordReader.initialize(testData, null);
		testIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);

		Evaluation eval;
		eval = vgg16Transfer.evaluate(testIter);
		log.info("Eval stats BEFORE fit.....");
		log.info(eval.stats() + "\n");
		testIter.reset();

		int iter = 0;
		while (trainIter.hasNext()) {
			vgg16Transfer.fit(trainIter.next());
			if (iter % 10 == 0) {
				log.info("Evaluate model at iter " + iter + " ....");
				eval = vgg16Transfer.evaluate(testIter);
				log.info(eval.stats());
				testIter.reset();
			}
			iter++;
		}

		log.info("Model build complete");
	}

	public void enableCuda() {
//		Nd4j.setDefaultDataTypes(DataType.FLOAT, DataType.BFLOAT16);

//		CudaEnvironment.getInstance().getConfiguration()
//				// key option enabled
//				.allowMultiGPU(true)
//				// we're allowing larger memory caches
//				.setMaximumDeviceCache(6L * 1024L * 1024L * 1024L)
//				// cross-device access is used for faster model averaging over pcie
//				.allowCrossDeviceAccess(true);
	}

	public static void main(String[] args) throws Exception {
		new VGGClassifier().run(args);
	}

}
