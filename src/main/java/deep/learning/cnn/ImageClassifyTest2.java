package deep.learning.cnn;

import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.transform.FlipImageTransform;
import org.datavec.image.transform.ImageTransform;
import org.datavec.image.transform.PipelineImageTransform;
import org.datavec.image.transform.WarpImageTransform;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
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
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.primitives.Pair;
import org.nd4j.linalg.schedule.ScheduleType;
import org.nd4j.linalg.schedule.StepSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;
	

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

public class ImageClassifyTest2 {
	protected static final Logger log = LoggerFactory.getLogger(ImageClassifier2.class);
	protected static int height = 102;
	protected static int width = 136;
		
	protected static int channels = 3;
	
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
        /**cd
         * Data Setup -> organize and limit data file paths:
         *  - mainPath = path to image files
         *  - fileSplit = define basic dataset split with limits on format
         *  - pathFilter = define additional file load filter to limit size and balance batch content
         **/
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
        File mainPath = new File("datafashion\\images-resize-102\\Number");
        FileSplit fileSplit = new FileSplit(mainPath, NativeImageLoader.ALLOWED_FORMATS, rng);
        int numExamples = toIntExact(fileSplit.length());
        numLabels = fileSplit.getRootDir().listFiles(File::isDirectory).length; //This only works if your root is clean: only label subdirs.
        System.out.println(numLabels);;
        System.out.println(numExamples);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, labelMaker, numExamples, numLabels, maxPathsPerLabel);

        /**
         * Data Setup -> train test split
         *  - inputSplit = define train and test split
         **/
        InputSplit[] inputSplit = fileSplit.sample(pathFilter, splitTrainTest, 1 - splitTrainTest);
        InputSplit trainData = inputSplit[0];
        InputSplit testData = inputSplit[1];

        /**
         * Data Setup -> transformation
         *  - Transform = how to tranform images and generate large dataset to train on
         **/
        ImageTransform flipTransform1 = new FlipImageTransform(rng);
        ImageTransform flipTransform2 = new FlipImageTransform(new Random(123));
        ImageTransform warpTransform = new WarpImageTransform(rng, 42);
        boolean shuffle = false;
        List<Pair<ImageTransform,Double>> pipeline = Arrays.asList(new Pair<>(flipTransform1,0.9),
                                                                   new Pair<>(flipTransform2,0.8),
                                                                   new Pair<>(warpTransform,0.5));

        ImageTransform transform = new PipelineImageTransform(pipeline,shuffle);
        /**
         * Data Setup -> normalization
         *  - how to normalize images and generate large dataset to train on
         **/
        DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
        ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelMaker);
        DataSetIterator dataIter;


        log.info("Train model....");
        // Train without transformations
        recordReader.initialize(trainData, null);
        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);
		
		log.info("Build model....");
		MultiLayerNetwork network = alexnetModel();

		network = network.load(new File("datafashion\\models\\model-alexnet-nnumber-128-50.bin"), false);
		

		Evaluation eval = network.evaluate(dataIter);
	    log.info(eval.stats(true));
		
	    recordReader.reset();
	    recordReader.initialize(testData, null);
        dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, numLabels);
        scaler.fit(dataIter);
        dataIter.setPreProcessor(scaler);
		eval = network.evaluate(dataIter);
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

	   public MultiLayerNetwork alexnetModel() {
	        /**
	         * AlexNet model interpretation based on the original paper ImageNet Classification with Deep Convolutional Neural Networks
	         * and the imagenetExample code referenced.
	         * http://papers.nips.cc/paper/4824-imagenet-classification-with-deep-convolutional-neural-networks.pdf
	         **/

	        double nonZeroBias = 1;
	        double dropOut = 0.5;

	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	            .seed(seed)
	            .weightInit(WeightInit.DISTRIBUTION)
	            .dist(new NormalDistribution(0.0, 0.01))
	            .activation(Activation.RELU)
	            .updater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 1e-2, 0.1, 100000), 0.9))
	            .biasUpdater(new Nesterovs(new StepSchedule(ScheduleType.ITERATION, 2e-2, 0.1, 100000), 0.9))
	            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
	            .l2(5 * 1e-4)
	            .list()
	            .layer(0, convInit("cnn1", channels, 96, new int[]{11, 11}, new int[]{4, 4}, new int[]{3, 3}, 0))
	            .layer(1, new LocalResponseNormalization.Builder().name("lrn1").build())
	            .layer(2, maxPool("maxpool1", new int[]{3,3}))
	            .layer(3, conv5x5("cnn2", 256, new int[] {1,1}, new int[] {2,2}, nonZeroBias))
	            .layer(4, new LocalResponseNormalization.Builder().name("lrn2").build())
	            .layer(5, maxPool("maxpool2", new int[]{3,3}))
	            .layer(6,conv3x3("cnn3", 384, 0))
	            .layer(7,conv3x3("cnn4", 384, nonZeroBias))
	            .layer(8,conv3x3("cnn5", 256, nonZeroBias))
	            .layer(9, maxPool("maxpool3", new int[]{3,3}))
	            .layer(10, fullyConnected("ffn1", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
	            .layer(11, fullyConnected("ffn2", 4096, nonZeroBias, dropOut, new GaussianDistribution(0, 0.005)))
	            .layer(12, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
	                .name("output")
	                .nOut(numLabels)
	                .activation(Activation.SOFTMAX)
	                .build())
//	            .backprop(true)
//	            .pretrain(false)
	            .setInputType(InputType.convolutional(height, width, channels))
	            .build();

	        return new MultiLayerNetwork(conf);

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
		new ImageClassifyTest2().run(args);
//		System.out.println(new File("").getAbsolutePath());
		
	}

}
