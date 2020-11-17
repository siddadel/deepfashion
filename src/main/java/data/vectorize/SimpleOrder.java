package data.vectorize;

import java.io.IOException;

import org.joda.time.LocalDateTime;

import data.query.Constants;
import data.query.Order;
import data.query.ZIpCodeData;

public class SimpleOrder {

	public static final int NO_OF_ZIP_DATA = 123;
	public static final int LENGTH_OF_PURCHASE_DATE = Order.LENGTH_OF_DATE_BYTE_ARRAY - 1;

	// input
	public static final int ZIPCODE_INDEX_IN_CSV_BYTES = 0;
	public static final int PHONEAREACODE_INDEX_IN_CSV_BYTES = 1;
	public static final int AGE_INDEX_IN_CSV_BYTES = 2;
	public static final int STATE_INDEX_IN_CSV_BYTES = 3;
	public static final int FIRSTNAME_INDEX_IN_CSV_BYTES = 4;
	public static final int LASTNAME_INDEX_IN_CSV_BYTES = 54;
	public static final int EMAIL_INDEX_IN_CSV_BYTES = 104;
	public static final int REGISTRATION_DATE_INDEX_IN_CSV_BYTES = 358;
	public static final int DATE_OF_BIRTH_INDEX_IN_CSV_BYTES = 364;
	public static final int GENDER_IN_CSV_BYTES = 370;
	public static final int CENSUS_ZIP_DATA = 371;

	public static final int ORDER_AMOUNT_INDEX_IN_CSV_BYTES = 494;
	public static final int PURCHASE_DATE_INDEX_IN_CSV_BYTES = 495;
	public static final int ORDER_STATUS_INDEX_IN_CSV_BYTES = 500;
	public static final int ITEMS_SIZE_INDEX = 501;

	String orderId;
	int zipCode;
	short phoneAreaCode;
	byte age;
	byte state;
	byte[] firstName;
	byte[] lastName;
	byte[] email;
	byte[] registrationDate;
	byte[] dateOfBirth;
	byte gender;
	int[] zipData;

	// output
	double orderAmount;
	byte[] purchaseDate;
	byte orderStatus;
	byte noOfItems;
	short catalogClass;
	short catalogStyle;
	

	public static String parseOriginalFromSerialized(String[] tokens) {

		StringBuilder sb = new StringBuilder();

		sb.append(tokens[ZIPCODE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(tokens[PHONEAREACODE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(tokens[AGE_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(Constants.US_STATES[Byte.parseByte(tokens[STATE_INDEX_IN_CSV_BYTES].trim())]);
		sb.append(Constants.COMMA);
		sb.append(
				Order.getStringForByteArray(tokens, FIRSTNAME_INDEX_IN_CSV_BYTES, LASTNAME_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(Order.getStringForByteArray(tokens, LASTNAME_INDEX_IN_CSV_BYTES, EMAIL_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(Order.getStringForByteArray(tokens, EMAIL_INDEX_IN_CSV_BYTES, REGISTRATION_DATE_INDEX_IN_CSV_BYTES)
				.trim());
		sb.append(Constants.COMMA);
		sb.append(Order.getStringForDateArray(tokens, REGISTRATION_DATE_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(Order.getStringForDateArray(tokens, DATE_OF_BIRTH_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(Byte.parseByte(tokens[GENDER_IN_CSV_BYTES].trim()));
		sb.append(Constants.COMMA);

		sb.append(tokens[ORDER_AMOUNT_INDEX_IN_CSV_BYTES].trim());
		sb.append(Constants.COMMA);
		sb.append(getStringForPurchaseDate(tokens, PURCHASE_DATE_INDEX_IN_CSV_BYTES).trim());
		sb.append(Constants.COMMA);
		sb.append(Constants.ORDER_STATUS[Byte.parseByte(tokens[ORDER_STATUS_INDEX_IN_CSV_BYTES].trim())]);
		sb.append(Constants.COMMA);
		sb.append(tokens[ITEMS_SIZE_INDEX]);
		return sb.toString();
	}

	public String featureString() {
		StringBuilder sb = new StringBuilder();
		sb.append(zipCode).append(Constants.COMMA);
		sb.append(phoneAreaCode).append(Constants.COMMA);
		sb.append(age).append(Constants.COMMA);
		sb.append(state);
		sb.append(Constants.COMMA);
		Constants.builtByteArray(sb, firstName, true);
		Constants.builtByteArray(sb, lastName, true);
		Constants.builtByteArray(sb, email, true);
		Constants.builtByteArray(sb, registrationDate, true);
		Constants.builtByteArray(sb, dateOfBirth, true);
		sb.append(gender).append(Constants.COMMA);
		Constants.builtByteArray(sb, zipData);
		return sb.toString();
	}

	public String smallFeatureString() {
		StringBuilder sb = new StringBuilder();
//		sb.append(zipCode).append(Constants.COMMA);
//		sb.append(phoneAreaCode).append(Constants.COMMA);
//		sb.append(age).append(Constants.COMMA);
//		sb.append(state).append(Constants.COMMA);
		Constants.builtByteArray(sb, firstName, true);
		Constants.builtByteArray(sb, lastName, true);
//		sb.append(gender).append(Constants.COMMA);
//		sb.append(zipData[0]).append(Constants.COMMA);
//		sb.append(zipData[12]).append(Constants.COMMA);
		System.out.println(sb.toString().split(",").length);
		return sb.toString();
	}
	
	public String allClassString() {
		StringBuilder sb = new StringBuilder();
		sb.append(orderAmount).append(Constants.COMMA);
		Constants.builtByteArray(sb, purchaseDate, true);
		sb.append(orderStatus).append(Constants.COMMA);
		sb.append(noOfItems);
		return sb.toString();
	}

	public String orderAmountString() {
		StringBuilder sb = new StringBuilder();
		sb.append(orderAmount);
		return sb.toString();
	}

	public SimpleOrder(String orderId, String zipCode, String phone, String state, String firstName, String lastName,
			String email, String registrationDate, String dateOfBirth, String gender, String orderAmount,
			String purchaseDateMonthTime, String orderStatus, String catalogClass, String catalogStyle) throws IOException {
		this.orderId = orderId;

		this.zipCode = Order.parseZipCode(zipCode);
		this.phoneAreaCode = Order.parsePhoneAreaCode(phone);
		this.age = Order.computeAge(dateOfBirth, purchaseDateMonthTime);
		this.state = Order.parseState(state);
		this.firstName = Order.parseString(firstName, Order.MAX_LENGTH_OF_NAME);
		this.lastName = Order.parseString(lastName, Order.MAX_LENGTH_OF_NAME);
		this.email = Order.parseString(email, Order.MAX_LENGTH_OF_EMAIL);
		this.registrationDate = Order.parseDate(registrationDate);
		this.dateOfBirth = Order.parseDate(dateOfBirth);
		this.gender = Order.parseGender(gender);

		this.zipData = ZIpCodeData.getValues(this.zipCode);

		this.orderAmount = Order.parseOrderAmount(orderAmount);
		this.purchaseDate = parseDateNoYear(purchaseDateMonthTime);
		this.orderStatus = Order.parseOrderStatus(orderStatus);

		this.catalogClass = parseCatalogClass(catalogClass);
		this.catalogStyle = parseNycoStyle(catalogStyle);
		incrementItem();

	}

	public void incrementItem() {
		noOfItems++;
	}

	public short parseCatalogClass(String catalogClass) {
		return Short.parseShort(catalogClass);
	}

	public short parseNycoStyle(String nycoStyle) {
		return Short.parseShort(nycoStyle);
	}

	public static byte[] parseDateNoYear(String date) {
		byte[] ret = new byte[LENGTH_OF_PURCHASE_DATE];
		if (date == null || date.equals("")) {
			return ret;
		}
		LocalDateTime d = LocalDateTime.parse(date, Order.formatInSource);
		ret[0] = (byte) d.getMonthOfYear();
		ret[1] = (byte) d.getDayOfMonth();
		ret[2] = (byte) d.getHourOfDay();
		ret[3] = (byte) d.getMinuteOfHour();
		ret[4] = (byte) d.getDayOfWeek();
		return ret;
	}

	public static String getStringForPurchaseDate(String[] tokens, int start) {
		int monthOfYear = Byte.parseByte(tokens[start + 0]);
		int dayOfMonth = Byte.parseByte(tokens[start + 1]);
		int hourOfDay = Byte.parseByte(tokens[start + 2]);
		int minuteOfHour = Byte.parseByte(tokens[start + 3]);
		int datOfWeek = Byte.parseByte(tokens[start + 4]);
		if (monthOfYear == 0)
			return "";

		return dayOfMonth + "/" + monthOfYear + " " + hourOfDay + ":" + minuteOfHour + " "
				+ Constants.daysOfWeek[datOfWeek - 1];
	}

}
