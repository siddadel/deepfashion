package deep.learning.cnn;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import com.opencsv.CSVReader;

/**
 * This program demonstrates how to resize an image.
 *
 * @author www.codejava.net
 *
 */
public class ImageResizer {

	 static int scaledWidth = 240;
	 static int scaledHeight = 320;

	// static int scaledWidth = 102;
	// static int scaledHeight = 136;

//	static int scaledWidth = 224;
//	static int scaledHeight = 299;

	/**
	 * Resizes an image to a absolute width and height (the image may not be
	 * proportional)
	 * 
	 * @param inputImagePath
	 *            Path of the original image
	 * @param outputImagePath
	 *            Path to save the resized image
	 * @param scaledWidth
	 *            absolute width in pixels
	 * @param scaledHeight
	 *            absolute height in pixels
	 * @throws IOException
	 */
	public static void resize(String inputImagePath, String outputImagePath, int scaledWidth, int scaledHeight)
			throws IOException {
		// reads input image
		File inputFile = new File(inputImagePath);
		BufferedImage inputImage = ImageIO.read(inputFile);

		// creates output image
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		// extracts extension of output file
		String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);

		// writes to output file
		ImageIO.write(outputImage, formatName, new File(outputImagePath));
	}

	/**
	 * Resizes an image by a percentage of original size (proportional).
	 * 
	 * @param inputImagePath
	 *            Path of the original image
	 * @param outputImagePath
	 *            Path to save the resized image
	 * @param percent
	 *            a double number specifies percentage of the output image over the
	 *            input image.
	 * @throws IOException
	 */
	public static void resize(String inputImagePath, String outputImagePath, double percent) throws IOException {
		File inputFile = new File(inputImagePath);
		BufferedImage inputImage = ImageIO.read(inputFile);
		int scaledWidth = (int) (inputImage.getWidth() * percent);
		int scaledHeight = (int) (inputImage.getHeight() * percent);
		resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
	}

	public static String getNumberCategory(String data) {
		int noOfPurchase = Integer.parseInt(data);
		if (noOfPurchase >= 500) {
			return "" + 1;
		} else {
			return "" + 0;
		}
	}


	/**
	 * Test resizing images
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		int failures = 0;
		CSVReader reader = new CSVReader(new FileReader("datafashion\\images - Copy.csv"));
		List<String[]> lines = reader.readAll();
		for (String[] line : lines) {

			String inputImagePath = "datafashion\\images-copy\\" + line[0] + ".jpg";
			String outputRoot = "datafashion\\images-resize-" + scaledWidth + "-" + scaledHeight + "\\Number/";
			String labelDir = line[1];
			labelDir = getNumberCategory(labelDir);

			new File(outputRoot + labelDir + "/").mkdirs();
			String outputImagePath = outputRoot + labelDir + "/" + line[0] + ".jpg";
			try {

				resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);

			} catch (Exception ex) {
				failures++;
			}
		}
		System.out.println(failures);
		reader.close();

	}

	private static void move(String inputImagePath, String outputImagePath) throws IOException {
		Path temp = Files.copy(Paths.get(inputImagePath), Paths.get(outputImagePath));

		if (temp != null) {
			System.out.println("File renamed and moved successfully");
		} else {
			System.out.println("Failed to move the file");
		}
	}

}
