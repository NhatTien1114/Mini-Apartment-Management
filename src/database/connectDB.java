package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connectDB {
	private static final String USERNAME = "sa";
	private static final String PASSWORD = "123";

	private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=ChungCuMini"
			+ ";encrypt=true;trustServerCertificate=true";

	private static Connection connection;

	public static Connection connect() {
		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					return connection;
				}
			} catch (SQLException e) {
				System.out.println("Kết nối SQL Server đã bị lỗi. Đang tạo kết nối mới...");
			}
		}

		try {
			// Nạp Driver
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			// Thực hiện kết nối
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.println("Kết nối SQL Server thành công!");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Lỗi: Không tìm thấy file thư viện .jar (Driver). Hãy add mssql-jdbc vào Libraries.", e);
		} catch (SQLException e) {
			throw new RuntimeException("Lỗi: Không thể kết nối. Hãy kiểm tra: 1. SQL Service đã chạy chưa? 2. Port 1433 đã mở chưa? 3. Pass đúng chưa?", e);
		}

		return connection;
	}

	public static Connection getConnection() {
		return connect();
	}

	public static void closeConnection() {
		if (connection == null) {
			return;
		}

		try {
			if (!connection.isClosed()) {
				connection.close();
				System.out.println("Đã đóng kết nối SQL Server.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Lỗi khi đóng kết nối SQL Server.", e);
		} finally {
			connection = null;
		}
	}
}