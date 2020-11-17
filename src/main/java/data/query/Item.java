package data.query;

public class Item {

	static final Item NULL_ITEM = new Item();

	public static final int PRICE_INDEX_IN_CSV_STRING = 0;
	public static final int COLOR_INDEX_IN_CSV_STRING = 1;
	public static final int COLOR_FAMILY_INDEX_IN_CSV_STRING = 2;
	public static final int SIZE_IN_CSV_STRING = 3;
	public static final int STATUS_INDEX_IN_CSV_STRING = 4;
	public static final int CATALOG_CLASS_INDEX_IN_CSV_STRING = 5;
	public static final int STYLE_INDEX_IN_CSV_STRING = 6;
	public static final int NO_OF_PROPERTIES_IN_ITEM = 7;

	double price;
	short color;
	byte colorFamily;
	byte size;
	byte status;
	short catalogClass;
	short style;

	// only for null
	private Item() {
		price = Constants.DEFAULT_VALUE;
		color = Constants.DEFAULT_VALUE;
		colorFamily = Constants.DEFAULT_VALUE;
		size = Constants.DEFAULT_VALUE;
		status = Constants.DEFAULT_VALUE;
		catalogClass = Constants.DEFAULT_VALUE;
		style = Constants.DEFAULT_VALUE;
	}

	public static void parseOriginalFromSerialized(StringBuilder sb, String[] tokens, int firstTokenInItem) {
			sb.append(tokens[firstTokenInItem + PRICE_INDEX_IN_CSV_STRING]).append(Constants.COMMA);
			sb.append(Constants.COLOR[Short.parseShort(tokens[firstTokenInItem + COLOR_INDEX_IN_CSV_STRING])]).append(Constants.COMMA);

			sb.append(Constants.COLOR_FAMILY[Byte.parseByte(tokens[firstTokenInItem + COLOR_FAMILY_INDEX_IN_CSV_STRING])]).append(Constants.COMMA);
			sb.append(Constants.SIZES[Byte.parseByte(tokens[firstTokenInItem + SIZE_IN_CSV_STRING])]).append(Constants.COMMA);
			sb.append(Constants.ITEM_STATUS[Byte.parseByte(tokens[firstTokenInItem + STATUS_INDEX_IN_CSV_STRING])]).append(Constants.COMMA);
			sb.append(Short.parseShort(tokens[firstTokenInItem + CATALOG_CLASS_INDEX_IN_CSV_STRING])).append(Constants.COMMA);
			sb.append(Short.parseShort(tokens[firstTokenInItem + STYLE_INDEX_IN_CSV_STRING])).append(Constants.COMMA);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(price);
		sb.append(Constants.COMMA);
		sb.append(color);
		sb.append(Constants.COMMA);
		sb.append(colorFamily);
		sb.append(Constants.COMMA);
		sb.append(size);
		sb.append(Constants.COMMA);
		sb.append(status);
		sb.append(Constants.COMMA);
		sb.append(catalogClass);
		sb.append(Constants.COMMA);
		sb.append(style);
		return sb.toString();
	}

	public Item(String price, String color, String colorFamily, String size, String status, String catalogClass,
			String style) {
		this.price = parsePrice(price);
		this.color = (short) parseColor(color);
		if(this.color<0) {
			System.out.println(color);
		}
		this.colorFamily = (byte) parseColorFamily(colorFamily);
		this.size = (byte) parseSize(size);
		this.status = (byte) parseStatus(status);
		this.catalogClass = (short) parseClass(catalogClass);
		this.style = (short) parseStyle(style);
	}

	static int parseClass(String catalogClass) {
		return Short.parseShort(catalogClass);
	}

	static int parseStyle(String style) {
		return Short.parseShort(style);
	}

	static double parsePrice(String price) {
		return Double.parseDouble(price);
	}

	static int parseColor(String color) {
		return Constants.getConstantIndex(color, Constants.COLOR);
	}

	public static int parseColorFamily(String colorFamily) {
		return Constants.getConstantIndex(colorFamily, Constants.COLOR_FAMILY);
	}

	static int parseSize(String size) {
		return Constants.getConstantIndex(size, Constants.SIZES);
	}

	static int parseStatus(String status) {
		return Constants.getConstantIndex(status, Constants.ITEM_STATUS);
	}

}
