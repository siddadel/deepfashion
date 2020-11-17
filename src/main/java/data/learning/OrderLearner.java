package data.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
//import org.deeplearning4j.ui.api.UIServer;
//import org.deeplearning4j.ui.stats.StatsListener;
//import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
//import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;

public abstract class OrderLearner {

	public static MultiLayerNetwork load(String modelPath) {
		try {
			return MultiLayerNetwork.load(new File(modelPath), false);
		} catch (IOException e) {
			return null;
		}
	}

	public INDArray getArray(String folderPath, double fractionOfDataUse) throws IOException {
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		List<INDArray> list = new ArrayList<INDArray>();

		for (int i = 0; i < (files.length * fractionOfDataUse); i++) {
			File f = files[i];
			INDArray r = Nd4j.readNumpy(f.getAbsolutePath(), ",");
			list.add(r);
		}
		return Nd4j.vstack(list);

	}

	public static DataSetIterator createIterator(DataSet data) {
		List<DataSet> list = data.asList();
		// Collections.shuffle(list, new Random(System.currentTimeMillis()));
		DataSetIterator iterator = new ListDataSetIterator<DataSet>(list, 1024);
		return iterator;
	}

	public DataNormalization normalize(DataSet trainingData, DataSet testData) {
		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(trainingData); // Collect the statistics (mean/stdev) from the training data. This does not
										// modify the input data
		normalizer.transform(trainingData); // Apply normalization to the training data
		normalizer.transform(testData); // Apply normalization to the test data. This is using statistics calculated
										// from the *training* set
		return normalizer;
	}

	public DataSet readCSV(String csvPath, int labelIndex, int numClasses, int size)
			throws IOException, InterruptedException {
		RecordReader recordReader = new CSVRecordReader(0, ',');
		System.out.println(csvPath);
		recordReader.initialize(new FileSplit(new File(csvPath)));
		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, size, labelIndex, numClasses);
		return iterator.next();
	}

	public void run(String csvPath, int labelIndex, int numClasses, ModelRunner modelRunner, int size, String modelPath, boolean fit)
			throws IOException, InterruptedException {
		DataSet inputData = readCSV(csvPath, labelIndex, numClasses, size);
		
		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(inputData);
		normalizer.transform(inputData);

		
		MultiLayerNetwork model 
		= modelRunner.getModel();
		
		if(fit) {
//			model.setListeners(new ScoreIterationListener(10));
//			UIServer uiServer = UIServer.getInstance();
//			StatsStorage statsStorage = new InMemoryStatsStorage();
//			uiServer.attach(statsStorage);
//			model.setListeners(new StatsListener( statsStorage),new
//			ScoreIterationListener(1));
			
			for (int i = 0; i < 10000; i++) {
				model.fit(inputData);
			}
		}else {
			model = load(modelPath);
		}
		
		model.init();
		INDArray output = model.output(inputData.getFeatures());// deliberately trying to test the training data

		model.save(new File(modelPath));

		DataSet outputData = new DataSet(inputData.getFeatures(), output);

		modelRunner.evaluate(outputData, inputData);
		System.out.println(inputData.getFeatures());
		System.out.println(inputData.getLabels());
		System.out.println(outputData.getLabels());
	}

	public void run(String featuresFolder, String classesFolder, double fractionOfDataUse, ModelRunner modelRunner)
			throws IOException, InterruptedException {

		INDArray features = getArray(featuresFolder, fractionOfDataUse);
		INDArray labels = getArray(classesFolder, fractionOfDataUse);
		DataSet inputData = new DataSet(features, labels);

		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(inputData);
		normalizer.transform(inputData);

		// SplitTestAndTrain split = inputData.splitTestAndTrain(0.99);
		// DataSet trainData = split.getTrain();
		// DataSet testData = split.getTest();

		MultiLayerNetwork model = modelRunner.getModel();
		enableCuda();
		DataSetIterator iterator = createIterator(inputData);
		modelRunner.fit(model, inputData);

		INDArray output = model.output(features);// deliberately trying to test the training data

		DataSet outputData = new DataSet(features, output);

		modelRunner.evaluate(outputData, inputData);
	}
	


	public static void fitAndSave(MultiLayerNetwork model, DataSet data, String modelPath) {
//		DataSetIterator it = createIterator(data);
		model.init();


//			model.setListeners(new ScoreIterationListener(10));
//			UIServer uiServer = UIServer.getInstance();
//			StatsStorage statsStorage = new InMemoryStatsStorage();
//			uiServer.attach(statsStorage);
//			model.setListeners(new StatsListener( statsStorage),new
//			ScoreIterationListener(1));

		for (int i = 0; i < 1000000; i++) {
//			it.reset();
			model.fit(data);
		}
		try {
			model.save(new File(modelPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double[] getLabelArray(DataSet data) {
		return data.getLabels().data().asDouble();
	}

	public static void printAverageDistance(DataSet outputData, DataSet testData, int noOfOutputColumns) {
		double[] predictedAmounts = getLabelArray(outputData);
		double[] actualAmounts = getLabelArray(testData);

		double error = 0;
		for (int i = 0; i < predictedAmounts.length; i += noOfOutputColumns) {
			for (int j = 0; j < noOfOutputColumns; j++) {
				System.out.print(predictedAmounts[i + j] + " ");
				if (actualAmounts[i + j] != 0) {
					error += (Math.abs(predictedAmounts[i + j] - actualAmounts[i + j]) / actualAmounts[i + j]);
				}
			}
			System.out.println();
			for (int j = 0; j < noOfOutputColumns; j++) {
				System.out.print(actualAmounts[i + j] + " ");
			}
			System.out.println("\n____________________________");
		}
		System.out.println(error);
		System.out.println(predictedAmounts.length);
		System.out.println("The results are on an average off by : " + (error / predictedAmounts.length));
		
	}

	public void enableCuda() {
//		 Nd4j.setDataType(DataBuffer.Type.FLOAT);
//		
//		 CudaEnvironment.getInstance().getConfiguration()
//		 // key option enabled
//		 .allowMultiGPU(true)
//		 // we're allowing larger memory caches
//		 .setMaximumDeviceCache(6L * 1024L * 1024L * 1024L)
//		 // cross-device access is used for faster model averaging over pcie
//		 .allowCrossDeviceAccess(true);
	}
}
