package dao;

import database.connectDB;
import entity.HoaDon;
import ui.main.HoaDonUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class HoaDonDAO {
    QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();
    HopDongDAO hdDAO = new HopDongDAO();

    private String taoMaTheoThoiGian(String prefix) {
        long millis = System.currentTimeMillis() % 1_000_000L;
        int random = (int) (Math.random() * 1000);
        return prefix + String.format("%012d%03d", millis, random);
    }
    public boolean luuNhieuHoaDonMoi(ArrayList<HoaDonUI.Bill> danhSachBill, String nguoiLap) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "INSERT INTO HoaDon (maHoaDon, maHopDong, maPhong, tuNgay, denNgay, trangThaiThanhToan, nguoiLap, createdAt) "
                    + "VALUES (?,?,?,?,?,0,?,GETDATE())";

            String sqlDetail = "INSERT INTO HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia) "
                    + "VALUES (?,?,?,?,?,?)";

            try (PreparedStatement psHD = con.prepareStatement(sqlHD);
                 PreparedStatement psDetail = con.prepareStatement(sqlDetail)) {

                for (HoaDonUI.Bill bill : danhSachBill) {
                    String maHoaDon = taoMaTheoThoiGian("HD");

                    int thang = Integer.parseInt(bill.month);
                    int nam = Integer.parseInt(bill.year);
                    java.time.LocalDate ngayDauThang = java.time.LocalDate.of(nam, thang, 1);
                    java.time.LocalDate ngayCuoiThang = ngayDauThang.withDayOfMonth(ngayDauThang.lengthOfMonth());

                    // 1. LƯU BẢNG HOADON
                    psHD.setString(1, maHoaDon);
                    psHD.setString(2, hdDAO.getHopDongByMaPhong(bill.phong).getMaHopDong());
                    psHD.setString(3, bill.phong);
                    psHD.setDate(4, java.sql.Date.valueOf(ngayDauThang));
                    psHD.setDate(5, java.sql.Date.valueOf(ngayCuoiThang));
                    psHD.setString(6, nguoiLap);

                    if (psHD.executeUpdate() == 0) {
                        throw new SQLException("Khong the tao hoa don cho phong " + bill.phong);
                    }

                    // 2. LƯU CHI TIẾT: TIỀN PHÒNG
                    psDetail.setString(1, taoMaTheoThoiGian("CT"));
                    psDetail.setString(2, maHoaDon);
                    psDetail.setString(3, "DV00");
                    psDetail.setInt(4, 1);
                    psDetail.setString(5, "Tiền thuê phòng");
                    psDetail.setDouble(6, bill.tienPhong);
                    psDetail.addBatch();

                    // 3. LƯU CHI TIẾT: TIỀN ĐIỆN
                    psDetail.setString(1, taoMaTheoThoiGian("CT"));
                    psDetail.setString(2, maHoaDon);
                    psDetail.setString(3, bill.maDichVuDien); // Tự động lấy "DV03"
                    psDetail.setInt(4, bill.tongTieuThuD);
                    psDetail.setString(5, "Tiền điện");
                    psDetail.setDouble(6, bill.donGiaDien);
                    psDetail.addBatch();

                    // 4. LƯU CHI TIẾT: TIỀN NƯỚC
                    psDetail.setString(1, taoMaTheoThoiGian("CT"));
                    psDetail.setString(2, maHoaDon);
                    psDetail.setString(3, bill.maDichVuNuoc); // Tự động lấy "DV02"
                    psDetail.setInt(4, bill.tongTieuThuN);
                    psDetail.setString(5, "Tiền nước");
                    psDetail.setDouble(6, bill.donGiaNuoc);
                    psDetail.addBatch();
                }

                // Thực thi toàn bộ lệnh Insert chi tiết
                psDetail.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("Lỗi lưu hóa đơn mới: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public ArrayList<Object[]> getAllHoaDon() {
        ArrayList<Object[]> listHoaDon = new ArrayList<>();

        DichVuDAO dvDAO = new DichVuDAO();
        String maDien = dvDAO.getDichVuByTen("Điện").getMaDichVu();
        String maNuoc = dvDAO.getDichVuByTen("Nước").getMaDichVu();

        String sql = "SELECT "
                + "  hd.maPhong, "
                + "  MONTH(hd.tuNgay) AS Thang, "
                + "  YEAR(hd.tuNgay) AS Nam, "
                + "  hd.trangThaiThanhToan, "
                + "  ISNULL(SUM(CASE WHEN ct.maDichVu = ? THEN ct.soLuong ELSE 0 END), 0) AS SoDien, "
                + "  ISNULL(SUM(CASE WHEN ct.maDichVu = ? THEN ct.soLuong ELSE 0 END), 0) AS SoNuoc, "
                + "  ISNULL(SUM(ct.thanhTien), 0) AS TongTien "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "GROUP BY hd.maHoaDon, hd.maPhong, hd.tuNgay, hd.trangThaiThanhToan "
                + "ORDER BY Nam DESC, Thang DESC, hd.maPhong ASC";

        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // 3. Truyền mã vừa lấy được vào câu SQL
            ps.setString(1, maDien);
            ps.setString(2, maNuoc);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    int thang = rs.getInt("Thang");
                    int nam = rs.getInt("Nam");
                    int soDien = rs.getInt("SoDien");
                    int soNuoc = rs.getInt("SoNuoc");
                    double tongTien = rs.getDouble("TongTien");
                    int trangThaiInt = rs.getInt("trangThaiThanhToan");

                    String thangNam = String.format("%02d/%d", thang, nam);
                    String trangThai = (trangThaiInt == 1) ? "Đã thanh toán" : "Chưa thanh toán";

                    Object[] row = new Object[] {
                            maPhong, thangNam, soDien, soNuoc, tongTien, trangThai, ""
                    };

                    listHoaDon.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listHoaDon;
    }
}
