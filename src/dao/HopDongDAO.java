package dao;

import database.connectDB;
import entity.HopDong;
import entity.Phong;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import ui.main.HopDongUI;

public class HopDongDAO {
    public String maHopDong;
    public String maPhong;
    public Date ngayBatDau;
    public Date ngayKetThuc;
    public double tienCoc;
    public double tienThueThang;
    public String trangThai;
    private String lastError;

    public String getLastError() {
        return lastError;
    }

    private String taoMaTheoThoiGian(String prefix) {
        long millis = System.currentTimeMillis() % 1_000_000L;
        int random = (int) (Math.random() * 1000);
        return prefix + String.format("%012d%03d", millis, random);
    }

    private String taoMaKhachHangMoi(Connection con) throws SQLException {
        // Sử dụng TRY_CAST để nếu gặp mã lỗi (như timestamp) nó sẽ trả về NULL thay vì
        // văng lỗi Overflow
        // Lọc thêm điều kiện LEN < 10 để đảm bảo chỉ lấy các mã KH thực tế
        String sql = "SELECT MAX(TRY_CAST(SUBSTRING(maKhachHang, 3, LEN(maKhachHang) - 2) AS BIGINT)) AS maxSo "
                + "FROM KhachHang "
                + "WHERE maKhachHang LIKE 'KH%' AND LEN(maKhachHang) < 10";

        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            long soTiepTheo = 1; // Khởi tạo mặc định là 1

            if (rs.next()) {
                long maxSo = rs.getLong("maxSo");
                if (!rs.wasNull()) {
                    soTiepTheo = maxSo + 1;
                }
            }

            // %02d: Đảm bảo ít nhất 2 chữ số (01, 02...).
            // Nếu số là 100, nó tự động thành 100 (3 chữ số) không cần sửa thêm.
            return "KH" + String.format("%02d", soTiepTheo);
        }
    }

    private java.sql.Date parseNgaySinhNullable(String ngaySinh) {
        if (ngaySinh == null || ngaySinh.trim().isEmpty()) {
            return null;
        }
        String[] p = ngaySinh.trim().split("/");
        if (p.length != 3) {
            return null;
        }
        return java.sql.Date.valueOf(p[2] + "-" + p[1] + "-" + p[0]);
    }

    private String timMaKhachHangTheoCCCD(Connection con, String cccd) throws SQLException {
        if (cccd == null || cccd.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT TOP 1 maKhachHang FROM KhachHang WHERE soCCCD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cccd.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("maKhachHang");
                }
            }
        }
        return null;
    }

    private void capNhatKhachHang(Connection con, String maKH, HopDongUI.ContractDraft draft) throws SQLException {
        String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, ngaySinh = ?, diaChiThuongTru = ? WHERE maKhachHang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, draft.hoTen);
            ps.setString(2, draft.soDienThoai);
            ps.setDate(3, parseNgaySinhNullable(draft.ngaySinh));
            ps.setString(4, draft.diaChi);
            ps.setString(5, maKH);
            ps.executeUpdate();
        }
    }

    public ArrayList<HopDong> getAllHopDongDangHieuLuc() {
        String sql = "SELECT maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang FROM HopDong WHERE trangThai = 1 ORDER BY maHopDong";
        ArrayList<HopDong> listHD = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHopDong = rs.getString("maHopDong");
                    String maPhong = rs.getString("maPhong");
                    LocalDate ngayBatDau = rs.getObject("ngayBatDau", LocalDate.class);
                    LocalDate ngayKetThuc = rs.getObject("ngayKetThuc", LocalDate.class);
                    double tienCoc = rs.getDouble("tienCoc");
                    double tienThueThang = rs.getDouble("tienThueThang");

                    int trangThaiInt = 1;
                    HopDong.TrangThai trangThai = HopDong.TrangThai.fromInt(trangThaiInt);

                    entity.Phong phong = new Phong(maPhong);

                    listHD.add(
                            new HopDong(maHopDong, phong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai));
                }
            }
            return listHD;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách hợp đồng.", e);
        }
    }

    public boolean luuHopDongMoi(HopDongUI.ContractDraft draft) {
        Connection con = null;
        lastError = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            String maKH = timMaKhachHangTheoCCCD(con, draft.cccd);
            if (maKH == null) {
                maKH = taoMaKhachHangMoi(con);
                String sqlKH = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru) VALUES (?,?,?,?,?,?)";
                try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                    psKH.setString(1, maKH);
                    psKH.setString(2, draft.hoTen);
                    psKH.setString(3, draft.soDienThoai);
                    psKH.setDate(4, parseNgaySinhNullable(draft.ngaySinh));
                    psKH.setString(5, draft.cccd);
                    psKH.setString(6, draft.diaChi);
                    if (psKH.executeUpdate() == 0) {
                        throw new SQLException("Khong the tao khach hang dai dien.");
                    }
                }
            } else {
                capNhatKhachHang(con, maKH, draft);
            }

            String maHD = taoMaTheoThoiGian("HD");
            String sqlHD = "INSERT INTO HopDong (maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai) "
                    + "VALUES (?,?,?,?,?,?,1)";
            try (PreparedStatement psHD = con.prepareStatement(sqlHD)) {
                psHD.setString(1, maHD);
                psHD.setString(2, draft.phong);
                psHD.setDate(3, java.sql.Date.valueOf(convertToSqlDate(draft.ngayBatDau)));
                psHD.setDate(4, java.sql.Date.valueOf(convertToSqlDate(draft.ngayKetThuc)));
                psHD.setDouble(5, parseMoneyToDouble(draft.tienCocRaw));
                psHD.setDouble(6, parseMoneyToDouble(draft.giaThueRaw));
                if (psHD.executeUpdate() == 0) {
                    throw new SQLException("Khong the tao hop dong moi.");
                }
            }

            String sqlHDKH = "INSERT INTO HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro) VALUES (?,?,?,0)";
            try (PreparedStatement psHDKH = con.prepareStatement(sqlHDKH)) {
                psHDKH.setString(1, taoMaTheoThoiGian("HDKT"));
                psHDKH.setString(2, maHD);
                psHDKH.setString(3, maKH);
                if (psHDKH.executeUpdate() == 0) {
                    throw new SQLException("Khong the tao lien ket hop dong-khach hang.");
                }
            }

            String sqlPhong = "UPDATE Phong SET trangThaiPhong = 1, soNguoiHienTai = ISNULL(soNguoiHienTai, 0) + 1 WHERE maPhong = ?";
            try (PreparedStatement psPhong = con.prepareStatement(sqlPhong)) {
                psPhong.setString(1, draft.phong);
                if (psPhong.executeUpdate() == 0) {
                    throw new SQLException("Khong tim thay phong de cap nhat thong tin cu tru.");
                }
            }

            con.commit();

            try {
                new DichVuDAO().ganTatCaDichVuChoPhongNeuChuaCo(draft.phong);
            } catch (RuntimeException ex) {
                System.err.println("Canh bao gan dich vu mac dinh that bai: " + ex.getMessage());
            }

            return true;
        } catch (SQLException | RuntimeException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            lastError = e.getMessage();
            System.err.println("Loi luu hop dong moi: " + e.getMessage());
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

    public boolean xoaHopDongVaKhachHangLienQuan(String maHopDong) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            String maPhong;
            String sqlPhong = "SELECT maPhong FROM HopDong WHERE maHopDong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPhong)) {
                ps.setString(1, maHopDong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Khong tim thay hop dong can xoa.");
                    }
                    maPhong = rs.getString("maPhong");
                }
            }

            List<String> dsKhachHang = new ArrayList<>();
            String sqlLayKhach = "SELECT maKhachHang FROM HopDongKhachHang WHERE maHopDong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlLayKhach)) {
                ps.setString(1, maHopDong);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dsKhachHang.add(rs.getString("maKhachHang"));
                    }
                }
            }

            String sqlXoaLienKet = "DELETE FROM HopDongKhachHang WHERE maHopDong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlXoaLienKet)) {
                ps.setString(1, maHopDong);
                ps.executeUpdate();
            }

            String sqlXoaHopDong = "DELETE FROM HopDong WHERE maHopDong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlXoaHopDong)) {
                ps.setString(1, maHopDong);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Khong xoa duoc hop dong.");
                }
            }

            String sqlXoaKhach = "DELETE FROM KhachHang WHERE maKhachHang = ? "
                    + "AND NOT EXISTS (SELECT 1 FROM HopDongKhachHang WHERE maKhachHang = ?)";
            try (PreparedStatement ps = con.prepareStatement(sqlXoaKhach)) {
                for (String maKh : dsKhachHang) {
                    ps.setString(1, maKh);
                    ps.setString(2, maKh);
                    ps.executeUpdate();
                }
            }

            int soNguoiGiam = dsKhachHang.size();
            String sqlGiamSoNguoi = "UPDATE Phong SET soNguoiHienTai = CASE "
                    + "WHEN ISNULL(soNguoiHienTai, 0) - ? < 0 THEN 0 "
                    + "ELSE ISNULL(soNguoiHienTai, 0) - ? END, "
                    + "trangThaiPhong = CASE WHEN ISNULL(soNguoiHienTai, 0) - ? <= 0 THEN 0 ELSE trangThaiPhong END "
                    + "WHERE maPhong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlGiamSoNguoi)) {
                ps.setInt(1, soNguoiGiam);
                ps.setInt(2, soNguoiGiam);
                ps.setInt(3, soNguoiGiam);
                ps.setString(4, maPhong);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("Khong cap nhat duoc so nguoi hien tai cua phong.");
                }
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
            System.err.println("Loi xoa hop dong: " + e.getMessage());
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

    public HopDong getHopDongByMaPhong(String maPhong) {
        String sql = "SELECT maHopDong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai FROM HopDong WHERE maPhong = ?";

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maPhong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        return null;
                    String maHopDong = rs.getString("maHopDong");
                    LocalDate ngayBatDau = rs.getObject("ngayBatDau", LocalDate.class);
                    LocalDate ngayKetThuc = rs.getObject("ngayKetThuc", LocalDate.class);
                    Double tienCoc = rs.getDouble("tienCoc");
                    Double tienThang = rs.getDouble("tienThueThang");
                    int tt = rs.getInt("trangThai");

                    return new HopDong(maHopDong, new Phong(maPhong), ngayBatDau, ngayKetThuc, tienCoc, tienThang,
                            HopDong.TrangThai.fromInt(tt));
                }
            }
        } catch (SQLException e) {
            System.err.println("layTheoMa lỗi: " + e.getMessage());
        }
        return null;
    }

    public HopDong getHopDongByMaHopDong(String maHopDong) {
        String sql = "SELECT maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai FROM HopDong WHERE maHopDong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maHopDong);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next())
                        return null;
                    String maPhong = rs.getString("maPhong");
                    LocalDate ngayBatDau = rs.getObject("ngayBatDau", LocalDate.class);
                    LocalDate ngayKetThuc = rs.getObject("ngayKetThuc", LocalDate.class);
                    Double tienCoc = rs.getDouble("tienCoc");
                    Double tienThang = rs.getDouble("tienThueThang");
                    int tt = rs.getInt("trangThai");
                    return new HopDong(maHopDong, new Phong(maPhong), ngayBatDau, ngayKetThuc, tienCoc, tienThang,
                            HopDong.TrangThai.fromInt(tt));
                }
            }
        } catch (SQLException e) {
            System.err.println("getHopDongByMaHopDong lỗi: " + e.getMessage());
        }
        return null;
    }

    // Hàm phụ chuyển định dạng ngày
    private String convertToSqlDate(String dateStr) {
        if (dateStr == null) {
            throw new IllegalArgumentException("Ngay khong hop le");
        }
        String[] p = dateStr.trim().split("/");
        if (p.length != 3) {
            throw new IllegalArgumentException("Ngay khong dung dinh dang dd/MM/yyyy");
        }
        return p[2] + "-" + p[1] + "-" + p[0];
    }

    private double parseMoneyToDouble(String rawMoney) {
        if (rawMoney == null) {
            throw new IllegalArgumentException("Tien khong hop le");
        }
        String digits = rawMoney.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Tien khong hop le");
        }
        return Double.parseDouble(digits);
    }

    public boolean ketThucHopDong(String maHopDong, String maPhong) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false);

            String sqlHD = "UPDATE HopDong SET trangThai = 0 WHERE maHopDong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlHD)) {
                ps.setString(1, maHopDong);
                ps.executeUpdate();
            }

            String sqlPhong = "UPDATE Phong SET trangThaiPhong = 0, soNguoiHienTai = 0 WHERE maPhong = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlPhong)) {
                ps.setString(1, maPhong);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null)
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            lastError = e.getMessage();
            return false;
        } finally {
            if (con != null)
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
        }
    }
}
