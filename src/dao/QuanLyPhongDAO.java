package dao;

import database.connectDB;
import entity.Phong;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QuanLyPhongDAO {

    // ── Format hợp lệ: T{1-6}.{01-09} ──
    private static final Pattern ROOM_PATTERN =
            Pattern.compile("^T[1-6]\\.(0[1-9]|[1-9]\\d)$");

    // Cache dịch vụ theo phòng để không mất trạng thái trong cùng phiên UI.
    // Hiện tại schema chưa có bảng liên kết phòng-dịch vụ.
    private final Map<String, List<String>> serviceCache = new HashMap<>();

    private static final List<String> DEFAULT_SERVICES = List.of("Điện", "Nước", "Internet", "Rác");

    public QuanLyPhongDAO() {}

    // ── Validate format ───
    public boolean isValidFormat(String maPhong) {
        return maPhong != null && ROOM_PATTERN.matcher(maPhong.trim().toUpperCase()).matches();
    }

    public static String normalise(String maPhong) {
        return maPhong == null ? "" : maPhong.trim().toUpperCase();
    }

    private static String tangFromRoom(String maPhong) {
        int dot = maPhong.indexOf('.');
        return dot > 0 ? maPhong.substring(0, dot) : "";
    }

    private static int toTrangThai(String trangThai) {
        if ("Đã thuê".equals(trangThai)) return 1;
        if ("Đang sửa".equals(trangThai)) return 2;
        if ("Đã cọc".equals(trangThai)) return 3;
        return 0;
    }

    private Phong buildPhong(String maPhong, int code) {
        Phong.TrangThai tt = switch (code) {
            case 1 -> Phong.TrangThai.THUE;
            case 2 -> Phong.TrangThai.SUA;
            case 3 -> Phong.TrangThai.COC;
            default -> Phong.TrangThai.TRONG;
        };
        Phong p = new Phong(0, null, maPhong, null, 0, maPhong, tt);
        p.setGiaThue(3_000_000L);
        p.setDichVu(getServicesByRoom(maPhong));
        return p;
    }

    private String findChuSoHuuMacDinh(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 maTaiKhoan FROM TaiKhoan WHERE role = 0 ORDER BY maTaiKhoan";
        try (PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("maTaiKhoan");
            }
            return null;
        }
    }

    private void ensureDefaultToaTang(Connection con) throws SQLException {
        String ownerId = findChuSoHuuMacDinh(con);
        if (ownerId == null || ownerId.isBlank()) {
            return;
        }

        String sqlInsertToa = "IF NOT EXISTS (SELECT 1 FROM Toa WHERE maToa = 'TOA1') "
                + "INSERT INTO Toa(maToa, tenToa, chuSoHuu) VALUES ('TOA1', N'Tòa A', ?)";
        try (PreparedStatement ps = con.prepareStatement(sqlInsertToa)) {
            ps.setString(1, ownerId);
            ps.executeUpdate();
        }

        String sqlInsertTang = "IF NOT EXISTS (SELECT 1 FROM Tang WHERE maTang = ?) "
                + "INSERT INTO Tang(maTang, tenTang, maToa) VALUES (?, ?, 'TOA1')";
        for (int i = 1; i <= 6; i++) {
            String maTang = "T" + i;
            try (PreparedStatement ps = con.prepareStatement(sqlInsertTang)) {
                ps.setString(1, maTang);
                ps.setString(2, maTang);
                ps.setString(3, "Tầng " + i);
                ps.executeUpdate();
            }
        }
    }

    private boolean tonTaiTang(Connection con, String maTang) throws SQLException {
        String sql = "SELECT 1 FROM Tang WHERE maTang = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<String> getServicesByRoom(String maPhong) {
        String key = normalise(maPhong);
        List<String> cached = serviceCache.get(key);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        return new ArrayList<>(DEFAULT_SERVICES);
    }

    // ── CREATE ───
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String them(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        if (!isValidFormat(ma))
            return "Tên phòng không đúng định dạng (VD: T1.01, T2.06)";
        if (giaThue <= 0)
            return "Giá thuê phải lớn hơn 0";

        String maTang = tangFromRoom(ma);
        String sql = "INSERT INTO Phong (maPhong, maTang, tenPhong, dienTich, loaiPhong, trangThaiPhong, soNguoiHienTai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = connectDB.getConnection();
            ensureDefaultToaTang(con);
            if (tonTai(ma)) {
                return "Phòng \"" + ma + "\" đã tồn tại";
            }
            if (!tonTaiTang(con, maTang)) {
                return "Tầng \"" + maTang + "\" chưa tồn tại trong hệ thống";
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                ps.setString(2, maTang);
                ps.setString(3, ma);
                ps.setObject(4, null);
                ps.setInt(5, 0);
                ps.setInt(6, toTrangThai(trangThai));
                ps.setInt(7, 0);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    return "Không thể thêm phòng vào database";
                }
            }

            serviceCache.put(ma, dichVu == null ? Collections.emptyList() : new ArrayList<>(dichVu));
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi thêm phòng: " + e.getMessage();
        }
    }

    // ── READ ───
    public List<Phong> layTatCa() {
        String sql = "SELECT maPhong, trangThaiPhong FROM Phong ORDER BY maPhong";
        List<Phong> result = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maPhong = rs.getString("maPhong");
                    result.add(buildPhong(maPhong, rs.getInt("trangThaiPhong")));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách phòng.", e);
        }
    }

    public Phong layTheoMa(String maPhong) {
        String ma = normalise(maPhong);
        String sql = "SELECT maPhong, trangThaiPhong FROM Phong WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    return buildPhong(ma, rs.getInt("trangThaiPhong"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy phòng theo mã.", e);
        }
    }

    public boolean tonTai(String maPhong) {
        String sql = "SELECT 1 FROM Phong WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, normalise(maPhong));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra tồn tại phòng.", e);
        }
    }

    /** Lấy danh sách phòng theo tầng, VD tầng "T3" */
    public List<Phong> layTheoTang(String tang) {
        String sql = "SELECT maPhong, trangThaiPhong FROM Phong WHERE maTang = ? ORDER BY maPhong";
        List<Phong> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maPhong = rs.getString("maPhong");
                        result.add(buildPhong(maPhong, rs.getInt("trangThaiPhong")));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy phòng theo tầng.", e);
        }
    }

    /** Lấy danh sách phòng theo tòa thông qua quan hệ Tang -> Toa */
    public List<Phong> layTheoToa(String maToa) {
        String sql = "SELECT p.maPhong, p.trangThaiPhong "
                + "FROM Phong p "
                + "INNER JOIN Tang t ON p.maTang = t.maTang "
                + "WHERE t.maToa = ? "
                + "ORDER BY p.maPhong";
        List<Phong> result = new ArrayList<>();
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maToa);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maPhong = rs.getString("maPhong");
                        result.add(buildPhong(maPhong, rs.getInt("trangThaiPhong")));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy phòng theo tòa.", e);
        }
    }

    // ── UPDATE ───
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String capNhat(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        if (giaThue <= 0)
            return "Giá thuê phải lớn hơn 0";

        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, toTrangThai(trangThai));
                ps.setString(2, ma);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    return "Không tìm thấy phòng \"" + ma + "\"";
                }
            }
            serviceCache.put(ma, dichVu == null ? Collections.emptyList() : new ArrayList<>(dichVu));
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi cập nhật phòng: " + e.getMessage();
        }
    }

    // ── DELETE ───
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String xoa(String maPhong) {
        String ma = normalise(maPhong);
        Phong p = layTheoMa(ma);
        if (p == null)
            return "Không tìm thấy phòng \"" + ma + "\"";
        if (p.getTrangThai() == Phong.TrangThai.THUE || p.getTrangThai() == Phong.TrangThai.COC)
            return "Không thể xóa phòng đang có người thuê hoặc đã cọc";

        String sql = "DELETE FROM Phong WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    return "Không thể xóa phòng \"" + ma + "\"";
                }
            }
            serviceCache.remove(ma);
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi xóa phòng: " + e.getMessage();
        }
    }
}