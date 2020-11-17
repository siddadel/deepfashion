package deep.learning.cnn;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.filters.RandomPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.PipelineImageTransform;
import org.datavec.image.transform.WarpImageTransform;
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
import org.nd4j.linalg.primitives.Pair;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

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

public class ImageClassifier2 {
	protected static final Logger log = LoggerFactory.getLogger(ImageClassifier2.class);
	
	protected static int height = 136;
	protected static int width = 102;
		
	protected static int channels = 3;
	protected static int labelIndex = -1;
	
	protected static int batchSize = 128;

	protected static long seed = 48937;
	protected static Random rng = new Random(seed);
	protected static int epochs = 100;

	private int numLabels;
	
	public void run(String[] args) throws Exception {

		log.info("Load data....");

//		String trainDataFolder = "datafashion\\images-resize\\Number\\train";
//		String testDataFolder = "datafashion\\images-resize\\Number\\test";
//		InputSplit trainData = getInputSplit(trainDataFolder);
//		InputSplit testData = getInputSplit(testDataFolder);
		
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        File mainPath = new File("datafashion\\images-resize-224-136\\Number");
        FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
        int numExamples = toIntExact(fileSplit.length());
        numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length; //This only works if your root is clean: only label subdirs.
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels,
				0);
		
		InputSplit[] inputSplit = fileSplit.sample(pathFilter, 0.8, 0.2);
		InputSplit trainData = inputSplit[0];
		InputSplit testData = inputSplit[1];
		
		
        ImageTransform flipTransform1 = new FlipImageTransform(rng);
        ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
        ImageTransform warpTransform = new WarpImageTransform(rng, 42);
        boolean shuffle = false;
        List<Pair<ImageTransform,Double>> pipeline = Arrays.asList(new Pair<>(flipTransform1,0.9),
                                                                   new Pair<>(flipTransform2,0.8),
                                                                   new Pair<>(warpTransform,0.5));
        ImageTransform transform = null;
//        		new PipelineImageTransform(pipeline,shuffle);
        
        
		HashMap<String, String[]> map = getMap("datafashion\\images.csv");
		System.out.println(map.size());
		DataNormalization normalizer = new NormalizerStandardize();
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		
		
		MultiDataSetIterator multiIterator = getIterator(trainData, map, normalizer, scaler, transform);
		
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

		ModelSerializer.writeModel(network, "datafashion\\models\\alexnet-136-102-128-100-balanced.bin", false);
		
		//deliberate evaluation of train data. 
		//presently the accuracy is quite low even on train data.
		multiIterator = getIterator(trainData, map, normalizer, scaler, null);
		Evaluation eval = network.evaluate(multiIterator);
	    log.info(eval.stats(true));
	   
	    multiIterator = getIterator(testData, map, normalizer, scaler, null);
		eval = network.evaluate(multiIterator);
	    log.info(eval.stats(true));

	}
	
	private InputSplit getInputSplit(String dataFolder) {
		FileSplit fileSplit = new FileSplit(new File(dataFolder),
				NativeImageLoader.ALLOWED_FORMATS, rng);
		
		int numExamples = toIntExact(fileSplit.length());
		log.info("No. of examples from "+dataFolder+": "+numExamples);
		numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length;
		RandomPathFilter pathFilter = new RandomPathFilter(rng, NativeImageLoader.ALLOWED_FORMATS);
		InputSplit[] input = fileSplit.sample(pathFilter);
		InputSplit data = input[0];
		return data;
	}
	
	private HashMap<String, String[]> getMap(String imageCsv) throws IOException{
		HashMap<String, String[]> map = new HashMap<>();
		CSVReader reader = new CSVReader(new FileReader(imageCsv));
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
	
	private MultiDataSetIterator getIterator(InputSplit trainData, HashMap<String, String[]> map, DataNormalization normalizer, DataNormalization scaler, ImageTransform imageTransform) throws IOException, InterruptedException {
		List<String[]> allLines = new ArrayList<String[]>();
		for (URI u : trainData.locations()) {
			String fileName = new File(u).getName();
			String[] data = map.get(fileName);
			String[] csvLine = new String[] { data[5], ImageResizer.getNumberCategory(data[1]) };
//			String[] csvLine = new String[] { getColorIndex(data[2]), data[3], data[4], data[5], ImageResizer.getNumberCategory(data[1]) };
			labelIndex = csvLine.length - 1;
			allLines.add(csvLine);
		}
		CSVWriter writer = new CSVWriter(new FileWriter("temp.csv"));
		
		writer.writeAll(allLines);
		writer.close();
		
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		ImageRecordReader imageRecordReader = new ImageRecordReader(height, width, channels, labelMaker);
		imageRecordReader.initialize(trainData, imageTransform);
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
		

		MultiDataSetIterator multiIterator = new RecordReaderMultiDataSetIterator.Builder(batchSize)
				.addReader("imageInput", imageRecordReader)
				.addReader("csvInput", csvRecordReader)
				.addInput("imageInput",0, 0)
				.addInput("csvInput", 0, labelIndex - 1)
				.addOutputOneHot("csvInput", labelIndex, numLabels)
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
		            .weightInit(WeightInit.XAVIER)
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
						fullyConnected("ffn3", numLabels + 1, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"ffn2", "csvInput")
				.addLayer("output",
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(numLabels)
								.nIn(numLabels + 1).activation(Activation.SOFTMAX).build(),
						"ffn3")
//				.backprop(true)
//				.pretrain(false)
				.setInputTypes(InputType.convolutional(height, width, channels), InputType.feedForward(labelIndex))
				.setOutputs("output").build();

		return new ComputationGraph(conf);
	}
	
	public ComputationGraph customModel2() {

		double nonZeroBias = 1;
		double dropOut = 0.1;
		
		
		ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
				
		            .seed(seed)
		            .l2(0.005)
		            .activation(Activation.RELU)
		            .weightInit(WeightInit.XAVIER)
		            .updater(new Nesterovs(0.0001,0.9))
		            
				.graphBuilder()
				.addInputs("imageInput", "csvInput")
				
				
	            .addLayer("cnn1", convInit("cnn1", channels, 50 ,  new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0),"imageInput")
	            .addLayer("maxpool1", maxPool("maxpool1", new int[]{2,2}), "cnn1")
	            .addLayer("cnn2", conv5x5("cnn2", 100, new int[]{5, 5}, new int[]{1, 1}, 0),"maxpool1")
	            .addLayer("maxpool2", maxPool("maxpool2", new int[]{2,2}), "cnn2")
	            .addLayer("ffn2", new DenseLayer.Builder().nOut(500).build(), "maxpool2")
				
				
				.addLayer("ffn3",
						fullyConnected("ffn3", numLabels + 1, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)),
						"ffn2", "csvInput")
				.addLayer("output",
						new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).nOut(numLabels)
								.nIn(numLabels + 1).activation(Activation.SOFTMAX).build(),
						"ffn3")
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
		new ImageClassifier2().run(args);
		
	}

}
