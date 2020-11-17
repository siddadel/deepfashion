package deep.learning.cnn;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LocalResponseNormalization;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import it.unimi.dsi.fastutil.Arrays;

/**
 * Animal Classification
 *
 * Example classification of photos from 4 different animals (bear, duck, deer,
 * turtle).
 *
 * References: - U.S. Fish and Wildlife Service (animal sample dataset):
 * http://digitalmedia.fws.gov/cdm/ - Tiny ImageNet Classification with CNN:
 * http://cs231n.stanford.edu/reports/2015/pdfs/leonyao_final.pdf
 *
 * CHALLENGE: Current setup gets low score results. Can you improve the scores?
 * Some approaches: - Add additional images to the dataset - Apply more
 * transforms to dataset - Increase epochs - Try different model configurations
 * - Tune by adjusting learning rate, updaters, activation & loss functions,
 * regularization, ...
 */

public class ImageClassifier3 {
	protected static final Logger log = LoggerFactory.getLogger(ImageClassifier2.class);
	protected static int height = 240;
	protected static int width = 180;
	
//	protected static int height = 320;
//	protected static int width = 240;

//	protected static int height = 136;
//	protected static int width = 102;	

	protected static int channels = 3;
	protected static int labelIndex = -1;
	
	protected static int batchSize = 128;

	protected static long seed = 456572;
	protected static Random rng = new Random(seed);
	protected static int epochs = 50;
	protected static double splitTrainTest = 0.8;
	protected static boolean save = true;
	protected static int maxPathsPerLabel = 0;

	protected static String modelType = "AlexNet"; // LeNet, AlexNet or Custom but you need to fill it out
	private int numLabels;
	
	public void run(String[] args) throws Exception {

		log.info("Load data....");

		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		FileSplit fileSplit = new FileSplit(new File("datafashion\\images-resize-180\\Number"),
				NativeImageLoader.ALLOWED_FORMATS, rng);
		
		int numExamples = toIntExact(fileSplit.length());
		System.out.println(numExamples);
		numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
		BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels,
				maxPathsPerLabel);
		
		InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
		InputSplit trainData = inputSplit[0];
		InputSplit testData = inputSplit[1];
		

		System.out.println(trainData.locations().length);
		System.out.println(testData.locations().length);

		HashMap<String, String[]> map = getMap();
		System.out.println(map.size());
		DataNormalization normalizer = new NormalizerStandardize();
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		
		MultiDataSetIterator multiIterator = getIterator(trainData, labelMaker, map, normalizer, scaler);
		
		log.info("Build model....");
		ComputationGraph network = customModel();

		network.init();
		network.setListeners(new ScoreIterationListener(10));
		UIServer uiServer = UIServer.getInstance();
		StatsStorage statsStorage = new InMemoryStatsStorage();
		uiServer.attach(statsStorage);
		network.setListeners(new StatsListener( statsStorage),new
		ScoreIterationListener(1));

		log.info("Train model....");
		network.fit(multiIterator, epochs);

		ModelSerializer.writeModel(network, "datafashion\\models\\model-alexnet-combined-graph-128-50-320height.bin", true);
		
		multiIterator = getIterator(testData, labelMaker, map, normalizer, scaler);
		Evaluation eval = network.evaluate(multiIterator);
	    log.info(eval.stats(true));
	   

	}
	
	private HashMap<String, String[]> getMap() throws IOException{
		HashMap<String, String[]> map = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader("datafashion\\images.csv"));
		List<String[]> lines = reader.readAll();
		for (String[] line : lines) {
			map.put(line[0] + ".jpg", line);
		}
		reader.close();
		return map;
	}
	
	private static String[] COLOR = new String[] {"BLACK","BLUE","BROWN","DENIM","GREEN","GREY","NATURAL","ORANGE","PINK","PURPLE","RED","WHITE","YELLOW"};
	private static String getColorIndex(String color) {
		for(int i =0; i<COLOR.length;i++) {
			if(color.equals(COLOR[i])) {
				return ""+i;
			}
		}
		return ""+-1;
	}
	
	private MultiDataSetIterator getIterator(InputSplit trainData, ParentPathLabelGenerator labelMaker, HashMap<String, String[]> map, DataNormalization normalizer, DataNormalization scaler) throws IOException, InterruptedException {
		List<String[]> allLines = new ArrayList<String[]>();
		for (URI u : trainData.locations()) {
			String fileName = new File(u).getName();
			String[] data = map.get(fileName);
//			String[] csvLine = new String[] { data[5], ImageResizer.getNumberCategory(data[1]) };
			String[] csvLine = new String[] { getColorIndex(data[2]), data[3], data[4], data[5], ImageResizer.getNumberCategory(data[1]) };
			labelIndex = csvLine.length - 1;
			allLines.add(csvLine);
		}
		System.out.println(labelIndex);
		CSVWriter writer = new CSVWriter(new FileWriter("temp.csv"));
		
		writer.writeAll(allLines);
		writer.close();
		
		
		ImageRecordReader imageRecordReader = new ImageRecordReader(height, width, channels, labelMaker);
		imageRecordReader.initialize(trainData, null);
		RecordReaderDataSetIterator imageIterator = new RecordReaderDataSetIterator(imageRecordReader, batchSize, 1,
				numLabels);


		CSVRecordReader csvRecordReader = new CSVRecordReader(0, ',');
		csvRecordReader.initialize(new FileSplit(new File("temp.csv")));
		DataSetIterator csvIterator = new RecordReaderDataSetIterator(csvRecordReader, batchSize, labelIndex, numLabels);
		csvRecordReader.setLabels(imageRecordReader.getLabels());
		
		
		normalizer.fit(csvIterator);
		csvIterator.setPreProcessor(normalizer);

		scaler.fit(imageIterator);
		imageIterator.setPreProcessor(scaler);
		

		System.out.println("creating iterator");
		MultiDataSetIterator multiIterator = new RecordReaderMultiDataSetIterator.Builder(batchSize)
				.addReader("imageInput", imageRecordReader)
				.addReader("csvInput", csvRecordReader)
				.addInput("imageInput",0, 0)
				.addInput("csvInput", 0, labelIndex - 1)
				.addOutputOneHot("imageInput", 1, numLabels)
				.build();	
		
		return multiIterator;
	}

	private ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad,
			double bias) {
		return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
	}

	private ConvolutionLayer conv3x3(String name, int out, double bias) {
		return new ConvolutionLayer.Builder(new int[] { 3, 3 }, new int[] { 1, 1 }, new int[] { 1, 1 }).name(name)
				.nOut(out).biasInit(bias).build();
	}

	private ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
		return new ConvolutionLayer.Builder(new int[] { 5, 5 }, stride, pad).name(name).nOut(out).biasInit(bias)
				.build();
	}

	private SubsamplingLayer maxPool(String name, int[] kernel) {
		return new SubsamplingLayer.Builder(kernel, new int[] { 2, 2 }).name(name).build();
	}

	private DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
		return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
	}

	public ComputationGraph customModel() {

		double nonZeroBias = 1;
		double dropOut = 0.5;
		
		
		ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
				
				 	.seed(seed)
		            .weightInit(WeightInit.DISTRIBUTION)
		            .dist(new NormalDistribution(0.0, 0.01))
		            .activation(Activation.RELU)
		            .updater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 1e-2, 0.1, 100000), 0.9))
		            .biasUpdater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 2e-2, 0.1, 100000), 0.9))
		            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
		            .l2(5 * 1e-4)
				
				.graphBuilder()
				.addInputs("imageInput", "csvInput")
				.addLayer("cnn1",
						convInit("cnn1", channels, 96, new int[] { 11, 11 }, new int[] { 4, 4 }, new int[] { 3, 3 }, 0),
						"imageInput")
				.addLayer("lrn1", new LocalResponseNormalization.Builder().name("lrn1").build(), "cnn1")
				.addLayer("maxpool1", maxPool("maxpool1", new int[] { 3, 3 }), "lrn1")
				.addLayer("cnn2", conv5x5("cnn2", 256, new int[] { 1, 1 }, new int[] { 2, 2 }, nonZeroBias), "maxpool1")
				.addLayer("lrn2", new LocalResponseNormalization.Builder().name("lrn2").build(), "cnn2")
				.addLayer("maxpool2", maxPool("maxpool2", new int[] { 3, 3 }), "lrn2")
				.addLayer("cnn3", conv3x3("cnn3", 384, 0), "maxpool2")
				.addLayer("cnn4", conv3x3("cnn4", 384, nonZeroBias), "cnn3")
				.addLayer("cnn5", conv3x3("cnn5", 256, nonZeroBias), "cnn4")
				.addLayer("maxpool3", maxPool("maxpool3", new int[] { 3, 3 }), "cnn5")
				.addLayer("ffn1",
						fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"maxpool3")
				.addLayer("ffn2",
						fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)), "ffn1")
				.addLayer("ffn3",
						fullyConnected("ffn3", 2 * 4097 + numLabels + 1, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"ffn2", "csvInput")
				.addLayer("ffn4",
						fullyConnected("ffn4",  2 * 4097 + numLabels + 1, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"ffn3")
				.addLayer("ffn5",
						fullyConnected("ffn5", numLabels + 1, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"ffn4")
				.addLayer("output",
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(numLabels)
								.nIn(numLabels + 1).activation(Activation.SOFTMAX).build(),
						"ffn5")
//				.backprop(true)
//				.pretrain(false)
				.setInputTypes(InputType.convolutional(height, width, channels), InputType.feedForward(labelIndex))
				.setOutputs("output").build();

		return new ComputationGraph(conf);
	}
	
	public static void enableCuda() {
//		 Nd4j.setDataType(DataBuffer.Type.HALF);
//		 CudaEnvironment.getInstance().getConfiguration()
//		 .allowMultiGPU(true)
//		 .setMaximumDeviceCache(2L * 1024L * 1024L * 1024L)
//		 .allowCrossDeviceAccess(true);
	}

	public static void main(String[] args) throws Exception {
//		enableCuda();
		new ImageClassifier3().run(args);
//		System.out.println(new File("").getAbsolutePath());
		
	}

}
