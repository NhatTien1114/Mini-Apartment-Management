package dao;

import database.connectDB;
import entity.Phong;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ui.main.HoaDonUI;

public class HoaDonDAO {
    HopDongDAO hdDAO = new HopDongDAO();
    QuanLyPhongDAO phongDAO = new QuanLyPhongDAO();

    private String resolveNguoiLapHopLe(Connection con, String nguoiLap) throws SQLException {
        String sqlCheck = "SELECT TOP 1 maTaiKhoan FROM TaiKhoan WHERE maTaiKhoan = ?";
        if (nguoiLap != null && !nguoiLap.trim().isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                ps.setString(1, nguoiLap.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("maTaiKhoan");
                    }
                }
            }
        }

        String sqlFallback = "SELECT TOP 1 maTaiKhoan FROM TaiKhoan ORDER BY maTaiKhoan";
        try (PreparedStatement ps = con.prepareStatement(sqlFallback);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("maTaiKhoan");
            }
        }

        throw new SQLException("Không có tài khoản hợp lệ để lưu người lập hóa đơn.");
    }

    private String taoMaTheoThoiGian(String prefix) {
        long millis = System.currentTimeMillis() % 1_000_000L;
        int random = (int) (Math.random() * 1000);
        return prefix + String.format("%012d%03d", millis, random);
    }

    private void ensureTienPhongServiceExists(Connection con) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DV00') "
                + "INSERT INTO DichVu(maDichVu, tenDichVu, donVi) VALUES ('DV00', N'Tiền phòng', N'tháng')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        }
    }

    private void ensureGuiXeServiceExists(Connection con) throws SQLException {
        String sql = "IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DVXE') "
                + "INSERT INTO DichVu(maDichVu, tenDichVu, donVi) VALUES ('DVXE', N'Gửi xe', N'xe/tháng')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.execute();
        }
    }

    public boolean luuNhieuHoaDonMoi(ArrayList<HoaDonUI.Bill> danhSachBill, String nguoiLap) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);
            ensureTienPhongServiceExists(con);
            ensureGuiXeServiceExists(con);
            String nguoiLapHopLe = resolveNguoiLapHopLe(con, nguoiLap);

            String sqlHD = "INSERT INTO HoaDon (maHoaDon, maHopDong, maPhong, tuNgay, denNgay, trangThaiThanhToan, nguoiLap, createdAt) "
                    + "VALUES (?,?,?,?,?,?,?,GETDATE())";

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
                    entity.HopDong hd = hdDAO.getHopDongByMaPhong(bill.phong);
                    if (hd == null) {
                        throw new SQLException("Không tìm thấy hợp đồng hiệu lực của phòng " + bill.phong);
                    }
                    psHD.setString(2, hd.getMaHopDong());
                    psHD.setString(3, bill.phong);
                    psHD.setDate(4, java.sql.Date.valueOf(ngayDauThang));
                    psHD.setDate(5, java.sql.Date.valueOf(ngayCuoiThang));
                    psHD.setInt(6, bill.daThanhToan ? 1 : 0);
                    psHD.setString(7, nguoiLapHopLe);

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

                    // 3. LƯU CHI TIẾT: TIỀN ĐIỆN (bỏ qua nếu tiêu thụ = 0)
                    if (bill.tongTieuThuD > 0) {
                        psDetail.setString(1, taoMaTheoThoiGian("CT"));
                        psDetail.setString(2, maHoaDon);
                        psDetail.setString(3, bill.maDichVuDien);
                        psDetail.setInt(4, bill.tongTieuThuD);
                        psDetail.setString(5, "Tiền điện");
                        psDetail.setDouble(6, bill.donGiaDien);
                        psDetail.addBatch();
                    }

                    // 4. LƯU CHI TIẾT: TIỀN NƯỚC (bỏ qua nếu tiêu thụ = 0)
                    if (bill.tongTieuThuN > 0) {
                        psDetail.setString(1, taoMaTheoThoiGian("CT"));
                        psDetail.setString(2, maHoaDon);
                        psDetail.setString(3, bill.maDichVuNuoc);
                        psDetail.setInt(4, bill.tongTieuThuN);
                        psDetail.setString(5, "Tiền nước");
                        psDetail.setDouble(6, bill.donGiaNuoc);
                        psDetail.addBatch();
                    }

                    // 5. LƯU CHI TIẾT: DỊCH VỤ KHÁC (wifi, rác, ...)
                    if (bill.dichVuKhac != null) {
                        for (HoaDonUI.BillServiceItem item : bill.dichVuKhac) {
                            if (item == null || item.maDichVu == null || item.maDichVu.trim().isEmpty()) {
                                continue;
                            }
                            int soLuong = Math.max(1, item.soLuong);
                            double donGia = Math.max(0, item.donGia);
                            String tenKhoan = (item.tenKhoan == null || item.tenKhoan.trim().isEmpty())
                                    ? "Dịch vụ"
                                    : item.tenKhoan.trim();

                            psDetail.setString(1, taoMaTheoThoiGian("CT"));
                            psDetail.setString(2, maHoaDon);
                            psDetail.setString(3, item.maDichVu);
                            psDetail.setInt(4, soLuong);
                            psDetail.setString(5, tenKhoan);
                            psDetail.setDouble(6, donGia);
                            psDetail.addBatch();
                        }
                    }
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
            System.err.println("Lỗi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return listHoaDon;
    }

    public ArrayList<Object[]> getLichSuHoaDonTongHop() {
        ArrayList<Object[]> list = new ArrayList<>();
        String sql = "SELECT CAST(MONTH(x.tuNgay) AS VARCHAR(2)) + '/' + CAST(YEAR(x.tuNgay) AS VARCHAR(4)) AS thangNam, "
                + "COUNT(*) AS soPhong, SUM(x.tongTien) AS tongDoanhThu, MAX(CAST(x.createdAt AS DATE)) AS ngayTao "
                + "FROM ( "
                + "  SELECT hd.maHoaDon, hd.tuNgay, hd.createdAt, SUM(ISNULL(ct.thanhTien,0)) AS tongTien "
                + "  FROM HoaDon hd "
                + "  LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "  GROUP BY hd.maHoaDon, hd.tuNgay, hd.createdAt "
                + ") x "
                + "GROUP BY MONTH(x.tuNgay), YEAR(x.tuNgay) "
                + "ORDER BY YEAR(x.tuNgay) DESC, MONTH(x.tuNgay) DESC";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String monthYear = rs.getString("thangNam");
                int soPhong = rs.getInt("soPhong");
                double tongDoanhThu = rs.getDouble("tongDoanhThu");
                Date ngay = rs.getDate("ngayTao");
                LocalDate ngayTao = ngay == null ? null : ngay.toLocalDate();
                list.add(new Object[] { monthYear, soPhong, tongDoanhThu, ngayTao });
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử hóa đơn tổng hợp: " + e.getMessage());
        }
        return list;
    }

    public ArrayList<HoaDonUI.MonthDetailRow> getChiTietHoaDonTheoThang(int month, int year) {
        ArrayList<HoaDonUI.MonthDetailRow> rows = new ArrayList<>();
        String sql = "SELECT hd.maPhong, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS tienPhong, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền điện' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS tienDien, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền nước' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS tienNuoc, "
                + "SUM(CASE WHEN ct.tenKhoan NOT IN (N'Tiền thuê phòng', N'Tiền điện', N'Tiền nước') THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS tienDichVu, "
                + "SUM(ISNULL(ct.thanhTien,0)) AS tongTien "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE MONTH(hd.tuNgay) = ? AND YEAR(hd.tuNgay) = ? "
                + "GROUP BY hd.maPhong "
                + "ORDER BY hd.maPhong";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonUI.MonthDetailRow row = new HoaDonUI.MonthDetailRow();
                    row.maPhong = rs.getString("maPhong");
                    row.tienPhong = rs.getDouble("tienPhong");
                    row.tienDien = rs.getDouble("tienDien");
                    row.tienNuoc = rs.getDouble("tienNuoc");
                    row.tienDichVu = rs.getDouble("tienDichVu");
                    row.tong = rs.getDouble("tongTien");
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy chi tiết hóa đơn tháng: " + e.getMessage());
        }
        return rows;
    }

    public List<HoaDonUI.RoomMonthSummary> getRoomSummariesTheoThang(int month, int year) {
        Map<String, HoaDonUI.RoomMonthSummary> byRoom = new LinkedHashMap<>();

        String sqlMain = "SELECT hd.maPhong, hd.trangThaiThanhToan, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS tienPhong, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền điện' THEN ct.soLuong ELSE 0 END) AS tieuThuDien, "
                + "MAX(CASE WHEN ct.tenKhoan = N'Tiền điện' THEN ct.donGia ELSE 0 END) AS donGiaDien, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền nước' THEN ct.soLuong ELSE 0 END) AS tieuThuNuoc, "
                + "MAX(CASE WHEN ct.tenKhoan = N'Tiền nước' THEN ct.donGia ELSE 0 END) AS donGiaNuoc "
                + "FROM HoaDon hd LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE MONTH(hd.tuNgay) = ? AND YEAR(hd.tuNgay) = ? "
                + "GROUP BY hd.maPhong, hd.maHoaDon, hd.trangThaiThanhToan "
                + "ORDER BY hd.maPhong";

        String sqlServices = "SELECT hd.maPhong, ct.maDichVu, ct.tenKhoan, ct.soLuong, ct.donGia "
                + "FROM HoaDon hd JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE MONTH(hd.tuNgay) = ? AND YEAR(hd.tuNgay) = ? "
                + "AND ct.tenKhoan NOT IN (N'Tiền thuê phòng', N'Tiền điện', N'Tiền nước') "
                + "ORDER BY hd.maPhong";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlMain)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonUI.RoomMonthSummary s = new HoaDonUI.RoomMonthSummary();
                    s.maPhong = rs.getString("maPhong");
                    s.tienPhong = rs.getDouble("tienPhong");
                    s.tieuThuDien = rs.getInt("tieuThuDien");
                    s.donGiaDien = rs.getDouble("donGiaDien");
                    s.tieuThuNuoc = rs.getInt("tieuThuNuoc");
                    s.donGiaNuoc = rs.getDouble("donGiaNuoc");
                    s.daThanhToan = rs.getInt("trangThaiThanhToan") == 1;
                    byRoom.put(s.maPhong, s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi load hóa đơn tháng: " + e.getMessage());
        }

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlServices)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    HoaDonUI.RoomMonthSummary s = byRoom.get(maPhong);
                    if (s != null) {
                        HoaDonUI.BillServiceItem si = new HoaDonUI.BillServiceItem();
                        si.maDichVu = rs.getString("maDichVu");
                        si.tenKhoan = rs.getString("tenKhoan");
                        si.soLuong = rs.getInt("soLuong");
                        si.donGia = rs.getDouble("donGia");
                        s.services.add(si);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi load dịch vụ hóa đơn tháng: " + e.getMessage());
        }

        return new ArrayList<>(byRoom.values());
    }

    public boolean daCoHoaDonThang(int month, int year) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE MONTH(tuNgay) = ? AND YEAR(tuNgay) = ?";
        ArrayList<Phong> dsPhong = phongDAO.getAllPhongDaThue();
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == dsPhong.size();
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra hóa đơn tháng: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTrangThaiThanhToan(String maPhong, int thang, int nam, boolean daThanhToan) {
        String sql = "UPDATE HoaDon SET trangThaiThanhToan = ? WHERE maPhong = ? AND MONTH(tuNgay) = ? AND YEAR(tuNgay) = ?";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, daThanhToan ? 1 : 0);
            ps.setString(2, maPhong);
            ps.setInt(3, thang);
            ps.setInt(4, nam);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái thanh toán: " + e.getMessage());
            return false;
        }
    }

    public List<Integer> getDanhSachNamHoaDon() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(tuNgay) AS nam FROM HoaDon ORDER BY nam";
        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("nam"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách năm hóa đơn: " + e.getMessage());
        }
        return years;
    }

    public Object[] getThongKeDoanhThuTheoNam(int year) {
        long[] doanhThuTheoThang = new long[12];
        long tongDoanhThu = 0;
        int soHoaDon = 0;

        String sqlThang = "SELECT MONTH(hd.tuNgay) AS thang, "
                + "COUNT(DISTINCT hd.maHoaDon) AS soHoaDon, "
                + "SUM(ISNULL(ct.thanhTien,0)) AS doanhThu "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE YEAR(hd.tuNgay) = ? "
                + "GROUP BY MONTH(hd.tuNgay)";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sqlThang)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("thang");
                    int idx = thang - 1;
                    if (idx < 0 || idx > 11) {
                        continue;
                    }
                    long doanhThu = Math.round(rs.getDouble("doanhThu"));
                    doanhThuTheoThang[idx] = doanhThu;
                    tongDoanhThu += doanhThu;
                    soHoaDon += rs.getInt("soHoaDon");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thống kê doanh thu theo năm: " + e.getMessage());
        }

        return new Object[] { doanhThuTheoThang, tongDoanhThu, soHoaDon };
    }

    /**
     * Thống kê doanh thu theo khoảng ngày, tách riêng phòng và dịch vụ.
     * Returns Object[]: long doanhThuPhong, long doanhThuDichVu, long tongDoanhThu,
     * int soHoaDon
     */
    public Object[] getThongKeDoanhThuTheoKhoangNgay(LocalDate from, LocalDate to) {
        long doanhThuPhong = 0, doanhThuDichVu = 0;
        int soHoaDon = 0;

        String sql = "SELECT "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtPhong, "
                + "SUM(CASE WHEN ct.tenKhoan <> N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtDichVu, "
                + "COUNT(DISTINCT hd.maHoaDon) AS soHD "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE hd.tuNgay >= ? AND hd.tuNgay <= ?";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doanhThuPhong = Math.round(rs.getDouble("dtPhong"));
                    doanhThuDichVu = Math.round(rs.getDouble("dtDichVu"));
                    soHoaDon = rs.getInt("soHD");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thống kê doanh thu theo khoảng ngày: " + e.getMessage());
        }
        long tong = doanhThuPhong + doanhThuDichVu;
        return new Object[] { doanhThuPhong, doanhThuDichVu, tong, soHoaDon };
    }

    /**
     * Thống kê doanh thu theo tháng trong khoảng ngày, tách phòng/dịch vụ.
     * Returns List<Object[]>: mỗi phần tử = {String label "T1/2025", long
     * doanhThuPhong, long doanhThuDichVu}
     */
    public List<Object[]> getDoanhThuTheoThangTrongKhoang(LocalDate from, LocalDate to) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT MONTH(hd.tuNgay) AS thang, YEAR(hd.tuNgay) AS nam, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtPhong, "
                + "SUM(CASE WHEN ct.tenKhoan <> N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtDichVu "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE hd.tuNgay >= ? AND hd.tuNgay <= ? "
                + "GROUP BY MONTH(hd.tuNgay), YEAR(hd.tuNgay) "
                + "ORDER BY nam, thang";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("thang");
                    int nam = rs.getInt("nam");
                    long dtPhong = Math.round(rs.getDouble("dtPhong"));
                    long dtDichVu = Math.round(rs.getDouble("dtDichVu"));
                    String label = "T" + thang + "/" + nam;
                    result.add(new Object[] { label, dtPhong, dtDichVu });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDoanhThuTheoThangTrongKhoang: " + e.getMessage());
        }
        return result;
    }

    /**
     * Thống kê doanh thu theo quý trong khoảng ngày, tách phòng/dịch vụ.
     */
    public List<Object[]> getDoanhThuTheoQuyTrongKhoang(LocalDate from, LocalDate to) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT DATEPART(QUARTER, hd.tuNgay) AS quy, YEAR(hd.tuNgay) AS nam, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtPhong, "
                + "SUM(CASE WHEN ct.tenKhoan <> N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtDichVu "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE hd.tuNgay >= ? AND hd.tuNgay <= ? "
                + "GROUP BY DATEPART(QUARTER, hd.tuNgay), YEAR(hd.tuNgay) "
                + "ORDER BY nam, quy";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int quy = rs.getInt("quy");
                    int nam = rs.getInt("nam");
                    long dtPhong = Math.round(rs.getDouble("dtPhong"));
                    long dtDichVu = Math.round(rs.getDouble("dtDichVu"));
                    String label = "Q" + quy + "/" + nam;
                    result.add(new Object[] { label, dtPhong, dtDichVu });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDoanhThuTheoQuyTrongKhoang: " + e.getMessage());
        }
        return result;
    }

    /**
     * Thống kê doanh thu theo năm trong khoảng ngày, tách phòng/dịch vụ.
     */
    public List<Object[]> getDoanhThuTheoNamTrongKhoang(LocalDate from, LocalDate to) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT YEAR(hd.tuNgay) AS nam, "
                + "SUM(CASE WHEN ct.tenKhoan = N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtPhong, "
                + "SUM(CASE WHEN ct.tenKhoan <> N'Tiền thuê phòng' THEN ISNULL(ct.thanhTien,0) ELSE 0 END) AS dtDichVu "
                + "FROM HoaDon hd "
                + "LEFT JOIN HoaDonDetail ct ON hd.maHoaDon = ct.maHoaDon "
                + "WHERE hd.tuNgay >= ? AND hd.tuNgay <= ? "
                + "GROUP BY YEAR(hd.tuNgay) "
                + "ORDER BY nam";

        try (Connection con = connectDB.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int nam = rs.getInt("nam");
                    long dtPhong = Math.round(rs.getDouble("dtPhong"));
                    long dtDichVu = Math.round(rs.getDouble("dtDichVu"));
                    String label = String.valueOf(nam);
                    result.add(new Object[] { label, dtPhong, dtDichVu });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDoanhThuTheoNamTrongKhoang: " + e.getMessage());
        }
        return result;
    }

    public boolean luuHoaDonKetThucHopDong(HoaDonUI.Bill bill, java.time.LocalDate tuNgay,
            java.time.LocalDate denNgay, String maHopDong) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);
            ensureTienPhongServiceExists(con);
            ensureGuiXeServiceExists(con);
            String nguoiLap = resolveNguoiLapHopLe(con, "AD01");

            String maHoaDon = taoMaTheoThoiGian("HD");
            String sqlHD = "INSERT INTO HoaDon (maHoaDon, maHopDong, maPhong, tuNgay, denNgay, "
                    + "trangThaiThanhToan, nguoiLap, createdAt) VALUES (?,?,?,?,?,0,?,GETDATE())";
            try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
                ps.setString(1, maHoaDon);
                ps.setString(2, maHopDong);
                ps.setString(3, bill.phong);
                ps.setDate(4, java.sql.Date.valueOf(tuNgay));
                ps.setDate(5, java.sql.Date.valueOf(denNgay));
                ps.setString(6, nguoiLap);
                ps.executeUpdate();
            }

            String sqlDetail = "INSERT INTO HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia) "
                    + "VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlDetail)) {
                ps.setString(1, taoMaTheoThoiGian("CT")); ps.setString(2, maHoaDon);
                ps.setString(3, "DV00"); ps.setInt(4, 1);
                ps.setString(5, "Tiền thuê phòng (theo ngày)"); ps.setDouble(6, bill.tienPhong);
                ps.addBatch();

                if (bill.tongTieuThuD > 0) {
                    ps.setString(1, taoMaTheoThoiGian("CT")); ps.setString(2, maHoaDon);
                    ps.setString(3, bill.maDichVuDien); ps.setInt(4, bill.tongTieuThuD);
                    ps.setString(5, "Tiền điện"); ps.setDouble(6, bill.donGiaDien);
                    ps.addBatch();
                }

                if (bill.tongTieuThuN > 0) {
                    ps.setString(1, taoMaTheoThoiGian("CT")); ps.setString(2, maHoaDon);
                    ps.setString(3, bill.maDichVuNuoc); ps.setInt(4, bill.tongTieuThuN);
                    ps.setString(5, "Tiền nước"); ps.setDouble(6, bill.donGiaNuoc);
                    ps.addBatch();
                }

                if (bill.dichVuKhac != null) {
                    for (HoaDonUI.BillServiceItem item : bill.dichVuKhac) {
                        if (item == null || item.maDichVu == null || item.maDichVu.isEmpty()) continue;
                        ps.setString(1, taoMaTheoThoiGian("CT")); ps.setString(2, maHoaDon);
                        ps.setString(3, item.maDichVu); ps.setInt(4, Math.max(1, item.soLuong));
                        ps.setString(5, item.tenKhoan == null ? "Dịch vụ" : item.tenKhoan);
                        ps.setDouble(6, item.donGia);
                        ps.addBatch();
                    }
                }

                ps.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            System.err.println("Lỗi lưu hóa đơn kết thúc hợp đồng: " + e.getMessage());
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
}
