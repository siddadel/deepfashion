package data.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.opencsv.CSVWriter;

public class OracleConnect {

	final String serviceName;
	final String userName;
	final String password;
	final String serverName;
	final String portNumber;
	final String dbms;
	final String queryFile;
	final String outputPathRoot;

	int totalNoParts;
	LocalDate startDate;
	LocalDate endDate;

	final static Logger logger = Logger.getLogger(OracleConnect.class);

	private OracleConnect() throws IOException, ParseException {
		Properties prop = new Properties();
		prop.load(getClass().getResourceAsStream("/application.properties"));

		serviceName = getProperty(prop, "serviceName");
		userName = getProperty(prop, "userName");
		password = getProperty(prop, "password");
		serverName = getProperty(prop, "serverName");
		portNumber = getProperty(prop, "portNumber");
		dbms = getProperty(prop, "dbms");
		queryFile = getProperty(prop, "queryFile");
		outputPathRoot = getProperty(prop, "outputPathRoot");
		totalNoParts = Integer.parseInt(getProperty(prop, "totalNoParts"));
		startDate = LocalDate.parse(getProperty(prop, "startDate"));
		endDate = LocalDate.parse(getProperty(prop, "endDate"));

	}

	private static String getProperty(Properties prop, String propName) {
		String p = prop.getProperty(propName);
		logger.info(p);
		return p;
	}

	public static void main(String[] args) throws Exception {
		new OracleConnect().run();
	}

	public void queryAndWrite(Connection con, LocalDate i, int part) throws IOException, SQLException {
		String path = getOutputFilePath(i, part);
		if (!new File(path).exists()) {
			Statement stmt = con.createStatement();
			String query = getSelectQuery(queryFile, getAddendum(i, part, totalNoParts));
			long t = System.currentTimeMillis();
			logger.info("Start querying\t" + path);
			ResultSet rs = stmt.executeQuery(query);
			logger.info("Query for\t" + path + "\t:\t" + (System.currentTimeMillis() - t));
			writeToFile(rs, path);
			rs.close();
			stmt.close();
		} else {
			logger.info(path + " already exists");
		}
	}


	public void run() throws SQLException, ClassNotFoundException, IOException {
		final Connection con = getConnection();
		for (LocalDate i = startDate; i.isBefore(endDate); i = i.plusDays(1)) {
			for (int part = 1; part <= totalNoParts; part++) {
				queryAndWrite(con, i, part);
			}
		}
		con.close();
		System.out.println("Connections closed");
	}

	public String getOutputFilePath(LocalDate date, int part) {
		return outputPathRoot + date + "-" + part + ".csv";
	}

	public void writeToFile(ResultSet rs, String outputFileName) throws IOException, SQLException {
		// new Thread(new Runnable() {
		// public void run() {
		CSVWriter writer = null;
		writer = new CSVWriter(new FileWriter(outputFileName));
		long t = System.currentTimeMillis();
		logger.info("Start writing\t" + outputFileName);
		writer.writeAll(rs, false);
		logger.info("To file for\t" + outputFileName + "\t:\t" + (System.currentTimeMillis() - t));
		writer.close();

		// }
		// }).start();
	}

	public String getSelectQuery(String queryFile, String addendum) throws IOException {
		InputStream inputStream = getClass().getResourceAsStream(queryFile);
		String content = IOUtils.toString(inputStream) + addendum;
		return content;
	}

	public String getAddendum(LocalDate date, int part, int totalNoParts) {
		String s = " and o.SUBMITTED_DATE >= DATE'" + date + "' and o.SUBMITTED_DATE < DATE'" + date.plusDays(1) + "'";

		s += " and extract(hour from o.SUBMITTED_DATE) >= " + 24 / totalNoParts * (part - 1)
				+ " and extract(hour from o.SUBMITTED_DATE) < " + 24 / totalNoParts * (part);

		return s;
	}

	public Connection getConnection() throws SQLException, ClassNotFoundException {

		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(
				"jdbc:oracle:thin:@" + serverName + ":" + portNumber + "/" + serviceName, userName, password);

		System.out.println("Connected to database");
		return con;
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
