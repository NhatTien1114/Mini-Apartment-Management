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
				// Connection closed, will reconnect
			}
		}

		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.println("KẺT NốI SQL SERVER THÀNH CÔNG!");
			ensureAdminAccount(connection);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"Lỗi: Không tìm thấy file thư viện .jar (Driver). Hãy add mssql-jdbc vào Libraries.", e);
		} catch (SQLException e) {
			throw new RuntimeException(
					"Lỗi: Không thể kết nối. Hãy kiểm tra: 1. SQL Service chạy chưa? 2. Pass đúng chưa? 3. Tên DB 'ChungCuMini' đúng chưa?",
					e);
		}

		return connection;
	}

	public static Connection getConnection() {
		return connect();
	}

	public static void closeConnection() {
		if (connection == null)
			return;
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

	private static void ensureAdminAccount(Connection con) {
		String sqlCheck = "SELECT 1 FROM TaiKhoan WHERE tenDangNhap = 'admin@gmail.com'";
		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlCheck)) {
			if (!rs.next()) {
				// Xoá record cũ (vi du: admin@example.com) để đè account mới vào
				st.executeUpdate("DELETE FROM TaiKhoan WHERE maTaiKhoan = 'TK00'");

				String sqlInsert = "INSERT INTO TaiKhoan (maTaiKhoan, tenDangNhap, matKhau, hoTen, soDienThoai, ngaySinh, diaChi, role) VALUES ('TK00', 'admin@gmail.com', 'admin123', 'Administrator', '0123456789', '2000-01-01', 'Admin System', 0)";
				st.executeUpdate(sqlInsert);
				System.out.println("Đã tự động khởi tạo tài khoản admin@gmail.com với vai trò Chủ.");
			} else {
				String sqlUpdate = "UPDATE TaiKhoan SET soDienThoai = '0123456789', ngaySinh = '2000-01-01', diaChi = 'Admin System' WHERE tenDangNhap = 'admin@gmail.com' AND soDienThoai IS NULL";
				st.executeUpdate(sqlUpdate);
			}
		} catch (SQLException e) {
			System.out.println("Lỗi lúc tạo admin account: " + e.getMessage());
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
					System.out.println(
							"!!! CẢNH BÁO: Bảng TaiKhoan đang TRỐNG TRƠN. Ông hãy vào SQL chèn data mẫu mới đăng nhập được.");
				}

			} catch (SQLException e) {
				System.out.println("Lỗi khi truy vấn bảng TaiKhoan: " + e.getMessage());
				System.out.println("=> Có thể ông chưa tạo bảng hoặc tên bảng/cột bị sai so với code.");
			}
		}
	}
}