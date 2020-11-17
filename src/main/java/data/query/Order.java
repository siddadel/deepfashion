package data.query;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Order {

	public static final int MAX_LENGTH_OF_NAME = 50;
	public static final int MAX_LENGTH_OF_EMAIL = 254;
	public static final int LENGTH_OF_ZIPCODE = 5;
	public static final int LENGTH_OF_DATE_BYTE_ARRAY = 6;
	public static final int LENGTH_OF_PHONE_AREACODE = 3;
	public static final int MAX_NUMBER_OF_ITEMS = 150;

	String orderId;

	// input
	public static final int ZIPCODE_INDEX_IN_CSV_BYTES = 0;
	public static final int PHONEAREACODE_INDEX_IN_CSV_BYTES = 1;
	public static final int AGE_INDEX_IN_CSV_BYTES = 2;
	public static final int FIRSTNAME_INDEX_IN_CSV_BYTES = 3;
	public static final int LASTNAME_INDEX_IN_CSV_BYTES = FIRSTNAME_INDEX_IN_CSV_BYTES + MAX_LENGTH_OF_NAME;
	public static final int EMAIL_INDEX_IN_CSV_BYTES = LASTNAME_INDEX_IN_CSV_BYTES + MAX_LENGTH_OF_NAME;
	public static final int REGISTRATION_DATE_INDEX_IN_CSV_BYTES = EMAIL_INDEX_IN_CSV_BYTES + MAX_LENGTH_OF_EMAIL;
	public static final int DATE_OF_BIRTH_INDEX_IN_CSV_BYTES = REGISTRATION_DATE_INDEX_IN_CSV_BYTES
			+ LENGTH_OF_DATE_BYTE_ARRAY;

	public static final int ORDER_AMOUNT_INDEX_IN_CSV_BYTES = DATE_OF_BIRTH_INDEX_IN_CSV_BYTES
			+ LENGTH_OF_DATE_BYTE_ARRAY;
	public static final int ORDER_STATUS_INDEX_IN_CSV_BYTES = ORDER_AMOUNT_INDEX_IN_CSV_BYTES + 1;
	public static final int STATE_INDEX_IN_CSV_BYTES = ORDER_STATUS_INDEX_IN_CSV_BYTES + 1;
	public static final int GENDER_IN_CSV_BYTES = STATE_INDEX_IN_CSV_BYTES + 1;
	public static final int PURCHASE_DATE_INDEX_IN_CSV_BYTES = GENDER_IN_CSV_BYTES + 1;
	public static final int ITEMS_INDEX_IN_CSV_BYTES = PURCHASE_DATE_INDEX_IN_CSV_BYTES + LENGTH_OF_DATE_BYTE_ARRAY;

	public static void main(String[] args) {
		System.out.println("No. of inputs: " + ORDER_AMOUNT_INDEX_IN_CSV_BYTES);
		System.out.println("NO. of outputs: " + (ITEMS_INDEX_IN_CSV_BYTES - ORDER_AMOUNT_INDEX_IN_CSV_BYTES)
				+ MAX_NUMBER_OF_ITEMS * Item.SIZE_IN_CSV_STRING);
	}

	int zipCode;
	short phoneAreaCode;
	byte age;
	byte[] firstName;
	byte[] lastName;
	byte[] email;
	byte[] registrationDate;
	byte[] dateOfBirth;

	// output
	double orderAmount;
	byte orderStatus;
	byte state;
	byte gender;
	byte[] purchaseMonthDateTime;

	List<Item> items;

	public static void fillRetString(Object[] ret, byte[] data, int start, int endPlusOne) {
		for (int i = start; i < endPlusOne; i++) {
			ret[i] = data[i - start];
		}
	}

	public static void fillRetString(Object[] ret, Object[] data, int start, int endPlusOne) {
		for (int i = start; i < endPlusOne; i++) {
			ret[i] = data[i - start];
		}
	}

	public static String getStringForByteArray(String[] tokens, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++) {
			sb.append((char) Byte.parseByte(tokens[i]));
		}
		return sb.toString();
	}

	public static String parseOriginalFromSerialized(String[] tokens) {

		StringBuilder sb = new StringBuilder();

		sb.append(tokens[ZIPCODE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(tokens[PHONEAREACODE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(tokens[AGE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForByteArray(tokens, FIRSTNAME_INDEX_IN_CSV_BYTES, LASTNAME_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForByteArray(tokens, LASTNAME_INDEX_IN_CSV_BYTES, EMAIL_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForByteArray(tokens, EMAIL_INDEX_IN_CSV_BYTES, REGISTRATION_DATE_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForDateArray(tokens, REGISTRATION_DATE_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForDateArray(tokens, DATE_OF_BIRTH_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(tokens[ORDER_AMOUNT_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(Constants.ORDER_STATUS[Byte.parseByte(tokens[ORDER_STATUS_INDEX_IN_CSV_BYTES].trim())]);
		sb.append(Constants.COMMA);
		sb.append(Constants.US_STATES[Byte.parseByte(tokens[STATE_INDEX_IN_CSV_BYTES].trim())]);
		sb.append(Constants.COMMA);
		sb.append(Byte.parseByte(tokens[GENDER_IN_CSV_BYTES].trim()));
		sb.append(Constants.COMMA);
		sb.append(getStringForDateArray(tokens, PURCHASE_DATE_INDEX_IN_CSV_BYTES).trim());

		for (int i = ITEMS_INDEX_IN_CSV_BYTES; i < tokens.length; i += Item.NO_OF_PROPERTIES_IN_ITEM) {
			Item.parseOriginalFromSerialized(sb, tokens, i);
		}
		return sb.toString();

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(zipCode).append(Constants.COMMA).append(phoneAreaCode).append(Constants.COMMA).append(age)
				.append(Constants.COMMA);

		sb = Constants.builtByteArray(sb, firstName, true);
		sb = Constants.builtByteArray(sb, lastName, true);
		sb = Constants.builtByteArray(sb, email, true);
		sb = Constants.builtByteArray(sb, registrationDate, true);
		sb = Constants.builtByteArray(sb, dateOfBirth, true);

		sb = sb.append(orderAmount);
		sb = sb.append(Constants.COMMA);
		sb = sb.append(orderStatus);
		sb = sb.append(Constants.COMMA);
		sb = sb.append(state);
		sb = sb.append(Constants.COMMA);
		sb = sb.append(gender);
		sb = sb.append(Constants.COMMA);
		sb = Constants.builtByteArray(sb, purchaseMonthDateTime, true);

		for (int i = 0; i < MAX_NUMBER_OF_ITEMS; i++) {
			if (i != 0) {
				sb.append(Constants.COMMA);
			}
			if (i >= items.size()) {
				sb.append(Item.NULL_ITEM.toString());
			} else {
				sb.append(items.get(i).toString());
			}
		}
		return sb.toString();
	}

	public static final DateTimeFormatter formatInSource = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm:ss");
	public static final int START_OF_CALENDAR = 1950;

	public Order(String orderId, String firstName, String lastName, String email, String zipCode, String phone,
			String registrationDate, String orderAmount, String orderStatus, String purchaseMonthDateTime, String price,
			String color, String colorFamily, String size, String state, String itemStatus, String gender,
			String dateOfBirth, String catalogClass, String style) {
		this.orderId = orderId;
		this.firstName = parseString(firstName, MAX_LENGTH_OF_NAME);
		this.lastName = parseString(lastName, MAX_LENGTH_OF_NAME);
		this.email = parseString(email, MAX_LENGTH_OF_EMAIL);
		this.zipCode = parseZipCode(zipCode);
		this.phoneAreaCode = parsePhoneAreaCode(phone);
		this.registrationDate = parseDate(registrationDate);
		this.orderAmount = parseOrderAmount(orderAmount);
		this.orderStatus = parseOrderStatus(orderStatus);
		this.purchaseMonthDateTime = parseDate(purchaseMonthDateTime);
		this.state = parseState(state);
		this.dateOfBirth = parseDate(dateOfBirth);
		this.age = computeAge(dateOfBirth, purchaseMonthDateTime);
		items = new ArrayList<Item>(MAX_NUMBER_OF_ITEMS);
		items.add(new Item(price, color, colorFamily, size, itemStatus, catalogClass, style));
	}

	public void addItem(Item item) {
		items.add(item);
	}

	public static byte parseState(String state) {
		return (byte) Constants.getConstantIndex(state, Constants.US_STATES);
	}

	public static byte[] parseDate(String date) {
		byte[] ret = new byte[LENGTH_OF_DATE_BYTE_ARRAY];
		if (date == null || date.equals("")) {
			return ret;
		}
		LocalDateTime d = LocalDateTime.parse(date, formatInSource);
		ret[0] = (byte) (d.getYear() - START_OF_CALENDAR);
		ret[1] = (byte) d.getMonthOfYear();
		ret[2] = (byte) d.getDayOfMonth();
		ret[3] = (byte) d.getHourOfDay();
		ret[4] = (byte) d.getMinuteOfHour();
		ret[5] = (byte) d.getDayOfWeek();
		return ret;
	}

	public static String getStringForDateArray(String[] tokens, int start) {
		int year = Byte.parseByte(tokens[start + 0]) + START_OF_CALENDAR;
		int monthOfYear = Byte.parseByte(tokens[start + 1]);
		int dayOfMonth = Byte.parseByte(tokens[start + 2]);
		int hourOfDay = Byte.parseByte(tokens[start + 3]);
		int minuteOfHour = Byte.parseByte(tokens[start + 4]);
		if (monthOfYear == 0)
			return "";
		LocalDateTime d = new LocalDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour);
		return (d.toString(formatInSource));
	}

	public static int parseZipCode(String zipCode) {
		if (zipCode.length() < LENGTH_OF_ZIPCODE) {
			return Constants.DEFAULT_VALUE;
		}
		try {
			return Integer.parseInt(zipCode.substring(0, LENGTH_OF_ZIPCODE));
		} catch (NumberFormatException e) {
			return Constants.DEFAULT_VALUE;
		}
	}

	public static short parsePhoneAreaCode(String phone) {
		if (phone.equals(""))
			return Constants.DEFAULT_VALUE;
		phone = phone.replaceAll("\\s+", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\+1", "")
				.replaceAll("\\+", "");
		return Short.parseShort(phone.substring(0, LENGTH_OF_PHONE_AREACODE));
	}

	public static double parseOrderAmount(String orderAmount) {
		return Double.parseDouble(orderAmount);
	}

	public static byte parseGender(String gender) {
		if (gender == null || gender.equals("")) {
			return -1;
		}
		return Byte.parseByte(gender);
	}

	public static byte parseOrderStatus(String orderStatus) {
		return (byte) Constants.getConstantIndex(orderStatus, Constants.ORDER_STATUS);
	}

	public static byte[] parseString(String s, int maxLength) {
		byte[] ret = new byte[maxLength];
		char[] chars = s.toCharArray();
		for (byte i = 0; i < chars.length && i < maxLength; i++) {
			ret[i] = (byte) chars[i];
		}
		return ret;
	}

	public static byte computeAge(String dateOfBirth, String purchaseDateTime) {
		if (dateOfBirth.equals(""))
			return Constants.DEFAULT_VALUE;
		return (byte) (Period.fieldDifference(LocalDateTime.parse(dateOfBirth, formatInSource).toLocalDate(),
				LocalDateTime.parse(purchaseDateTime, formatInSource).toLocalDate()).getYears());
	}

}
