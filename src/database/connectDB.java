package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class connectDB {
	private static final String USERNAME = "sa";
	private static final String PASSWORD = "sapassword";

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
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.println("========================================");
			System.out.println("KẾT NỐI SQL SERVER THÀNH CÔNG RỒI ÔNG ƠI!");
			System.out.println("========================================");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Lỗi: Không tìm thấy file thư viện .jar (Driver). Hãy add mssql-jdbc vào Libraries.", e);
		} catch (SQLException e) {
			throw new RuntimeException("Lỗi: Không thể kết nối. Hãy kiểm tra: 1. SQL Service chạy chưa? 2. Pass đúng chưa? 3. Tên DB 'ChungCuMini' đúng chưa?", e);
		}

		return connection;
	}

	public static Connection getConnection() {
		return connect();
	}

	public static void closeConnection() {
		if (connection == null) return;
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

	// --- HÀM MAIN ĐỂ ÔNG TEST ĐÂY ---
	public static void main(String[] args) {
		System.out.println("Đang kiểm tra kết nối...");
		Connection con = connect();

		if (con != null) {
			System.out.println("--- DANH SÁCH TÀI KHOẢN TRONG DATABASE ---");
			String sql = "SELECT tenDangNhap, matKhau FROM TaiKhoan";
			try (Statement st = con.createStatement();
				 ResultSet rs = st.executeQuery(sql)) {

				boolean hasData = false;
				while (rs.next()) {
					hasData = true;
					System.out.println("User: " + rs.getString("tenDangNhap") + " | Pass: " + rs.getString("matKhau"));
				}

				if (!hasData) {
					System.out.println("!!! CẢNH BÁO: Bảng TaiKhoan đang TRỐNG TRƠN. Ông hãy vào SQL chèn data mẫu mới đăng nhập được.");
				}

			} catch (SQLException e) {
				System.out.println("Lỗi khi truy vấn bảng TaiKhoan: " + e.getMessage());
				System.out.println("=> Có thể ông chưa tạo bảng hoặc tên bảng/cột bị sai so với code.");
			}
		}
	}
}