package dao;

import database.connectDB;
import entity.Phong;
import entity.Phong.LoaiPhong;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QuanLyPhongDAO {

    private static final Pattern ROOM_PATTERN =
            Pattern.compile("^P[1-6]\\.(0[1-9]|[1-9]\\d)$");

    private final Map<String, List<String>> serviceCache = new HashMap<>();
    private static final List<String> DEFAULT_SERVICES = List.of("Điện", "Nước", "Internet", "Rác");

    public QuanLyPhongDAO() {}

    public boolean isValidFormat(String maPhong) {
        return maPhong != null && ROOM_PATTERN.matcher(maPhong.trim().toUpperCase()).matches();
    }

    public static String normalise(String maPhong) {
        return maPhong == null ? "" : maPhong.trim().toUpperCase();
    }

    private static String tangFromRoom(String maPhong) {
        if (maPhong == null || maPhong.length() < 2) return "";
        return "T" + maPhong.charAt(1);
    }

    private static int toTrangThai(String trangThai) {
        if ("Đã thuê".equals(trangThai)) return 1;
        if ("Đang sửa".equals(trangThai)) return 2;
        if ("Đã cọc".equals(trangThai))   return 3;
        return 0;
    }

    // FIX: nhận thêm loaiPhong từ DB thay vì hardcode null
    private Phong buildPhong(String maPhong, int trangThaiCode, Integer loaiPhongCode, String maGiaDetail) {
        // 1. Chuyển đổi mã trạng thái (int -> Enum TrangThai)
        Phong.TrangThai tt = switch (trangThaiCode) {
            case 1 -> Phong.TrangThai.THUE;
            case 2 -> Phong.TrangThai.SUA;
            case 3 -> Phong.TrangThai.COC;
            default -> Phong.TrangThai.TRONG;
        };

        // 2. Chuyển đổi mã loại phòng (int -> Enum LoaiPhong)
        Phong.LoaiPhong lp = null;
        if (loaiPhongCode != null) {
            Phong.LoaiPhong[] vals = Phong.LoaiPhong.values();
            if (loaiPhongCode >= 0 && loaiPhongCode < vals.length) {
                lp = vals[loaiPhongCode];
            }
        }

        // 3. Tạo đối tượng Phong mới
        // Giả sử Constructor của ông là: Phong(maPhong, maTang, tenPhong, dienTich, loaiPhong, trangThai, soNguoi, maGiaDetail)
        // Nếu ông chưa cập nhật Constructor trong Entity Phong, hãy thêm field maGiaDetail vào đó.
        Phong p = new Phong();
        p.setMaPhong(maPhong);
        p.setTenPhong(maPhong);
        p.setLoaiPhong(lp);
        p.setTrangThai(tt);
        p.setMaGiaDetail(maGiaDetail); // Cột mới ông vừa muốn thêm

        return p;
    }

    private String findChuSoHuuMacDinh(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 maTaiKhoan FROM TaiKhoan WHERE role = 0 ORDER BY maTaiKhoan";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString("maTaiKhoan") : null;
        }
    }

    private void ensureDefaultToaTang(Connection con) throws SQLException {
        String ownerId = findChuSoHuuMacDinh(con);
        if (ownerId == null || ownerId.isBlank()) return;

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

    // ── CREATE ──
    public String them(String maPhong, String maTang, int loaiPhong, String maGiaDetail, int trangThai) {
        String sql = "INSERT INTO Phong (maPhong, maTang, tenPhong, loaiPhong, maGiaDetail, trangThaiPhong, soNguoiHienTai) "
                + "VALUES (?, ?, ?, ?, ?, ?, 0)";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setString(2, maTang);
            ps.setString(3, maPhong);
            ps.setInt(4, loaiPhong);
            ps.setString(5, maGiaDetail); // Lưu mã giá vào đây
            ps.setInt(6, trangThai);

            return ps.executeUpdate() > 0 ? null : "Lỗi thêm phòng";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    // ── READ ──
    public List<Phong> layTatCa() {
        // 1. Cập nhật SQL: SELECT thêm cột maGiaDetail
        String sql = "SELECT maPhong, loaiPhong, trangThaiPhong, maGiaDetail FROM Phong ORDER BY maPhong";
        List<Phong> result = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String ma = rs.getString("maPhong");

                    // 2. Lấy loaiPhong (nullable)
                    Integer lp = rs.getObject("loaiPhong") == null
                            ? null : rs.getInt("loaiPhong");

                    // 3. Lấy trạng thái phòng
                    int tt = rs.getInt("trangThaiPhong");

                    // 4. Lấy maGiaDetail (khóa ngoại tới bảng giá)
                    String maGia = rs.getString("maGiaDetail");

                    // 5. Gọi buildPhong với đủ 4 tham số:
                    // (maPhong, trangThaiCode, loaiPhongCode, maGiaDetail)
                    result.add(buildPhong(ma, tt, lp, maGia));
                }
            }
        } catch (SQLException e) {
            System.err.println("layTatCa lỗi: " + e.getMessage());
        }
        return result;
    }

    public Phong layTheoMa(String maPhong) {
        String ma = normalise(maPhong);
        // 1. Cập nhật SELECT: lấy thêm cột maGiaDetail
        String sql = "SELECT maPhong, loaiPhong, trangThaiPhong, maGiaDetail FROM Phong WHERE maPhong = ?";

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ma);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;

                    // 2. Lấy loaiPhong (check null bằng getObject)
                    Integer lp = rs.getObject("loaiPhong") == null ? null : rs.getInt("loaiPhong");

                    // 3. Lấy trạng thái
                    int tt = rs.getInt("trangThaiPhong");

                    // 4. Lấy maGiaDetail mới thêm
                    String maGia = rs.getString("maGiaDetail");

                    // 5. Gọi buildPhong với đủ 4 tham số: maPhong, trangThaiCode, loaiPhongCode, maGiaDetail
                    return buildPhong(ma, tt, lp, maGia);
                }
            }
        } catch (SQLException e) {
            System.err.println("layTheoMa lỗi: " + e.getMessage());
        }
        return null;
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
            // lỗi kết nối → coi như chưa tồn tại
        }
        return false;
    }

    public List<Phong> layTheoTang(String tang) {
        // 1. SELECT thêm maGiaDetail để đồng bộ với hàm buildPhong mới
        String sql = "SELECT maPhong, loaiPhong, trangThaiPhong, maGiaDetail FROM Phong WHERE maTang = ? ORDER BY maPhong";
        List<Phong> result = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maPhong = rs.getString("maPhong");

                        // 2. Lấy loaiPhong (nullable)
                        Integer lp = rs.getObject("loaiPhong") == null
                                ? null : rs.getInt("loaiPhong");

                        // 3. Lấy trạng thái (mặc định 0 nếu null)
                        int tt = rs.getInt("trangThaiPhong");

                        // 4. Lấy maGiaDetail (cột mới ông vừa thêm vào DB)
                        String maGia = rs.getString("maGiaDetail");

                        // 5. Gọi buildPhong với đầy đủ 4 tham số
                        result.add(buildPhong(maPhong, tt, lp, maGia));
                    }
                }
            }
        } catch (SQLException e) {
            // In lỗi ra console để ông dễ debug nếu câu SQL sai tên cột
            System.err.println("Lỗi layTheoTang: " + e.getMessage());
        }
        return result;
    }

    // ── UPDATE ──
    // FIX: cập nhật cả loaiPhong vào DB
    public String capNhat(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, toTrangThai(trangThai));
                ps.setString(2, ma);
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy phòng \"" + ma + "\"";
            }
            serviceCache.put(ma, dichVu == null ? Collections.emptyList() : new ArrayList<>(dichVu));
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi cập nhật phòng: " + e.getMessage();
        }
    }

    // FIX: overload mới nhận thêm loaiPhong để lưu vào DB
    public String capNhat(String maPhong, LoaiPhong loaiPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        String sql = "UPDATE Phong SET loaiPhong = ?, trangThaiPhong = ? WHERE maPhong = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (loaiPhong == null) {
                    ps.setNull(1, java.sql.Types.TINYINT);
                } else {
                    ps.setInt(1, loaiPhong.ordinal());
                }
                ps.setInt(2, toTrangThai(trangThai));
                ps.setString(3, ma);
                int rows = ps.executeUpdate();
                if (rows == 0)
                    return "Không tìm thấy phòng \"" + ma + "\"";
            }
            serviceCache.put(ma, dichVu == null ? Collections.emptyList() : new ArrayList<>(dichVu));
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi cập nhật phòng: " + e.getMessage();
        }
    }

    // ── GIÁ THUÊ ──
    public long layGiaThueMoiNhat(LoaiPhong loaiPhong) {
        if (loaiPhong == null) return -1;

        String sql = "SELECT TOP 1 d.donGia "
                + "FROM dbo.GiaDetail d "
                + "JOIN dbo.GiaHeader h ON h.maGiaHeader = d.maGiaHeader "
                + "WHERE h.loai      = 0 "
                + "  AND h.trangThai = 1 "
                + "  AND d.loaiPhong = ? "
                + "ORDER BY "
                + "    CASE WHEN h.ngayKetThuc IS NULL THEN 0 ELSE 1 END, "
                + "    h.ngayBatDau DESC";

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, loaiPhong.ordinal());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return (long) rs.getDouble("donGia");
                }
            }
        } catch (SQLException e) {
            System.err.println("QuanLyPhongDAO.layGiaThueMoiNhat lỗi: " + e.getMessage());
        }
        return -1;
    }

    // ── DELETE ──
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
                if (rows == 0)
                    return "Không thể xóa phòng \"" + ma + "\"";
            }
            serviceCache.remove(ma);
            return null;
        } catch (SQLException e) {
            return "Lỗi database khi xóa phòng: " + e.getMessage();
        }
    }

    public ArrayList<Phong> getAllPhongTrong(){
        String sql = "SELECT maPhong, maTang, tenPhong, dienTich, loaiPhong, soNguoiHienTai, maGiaDetail FROM Phong WHERE trangThaiPhong = ? ORDER BY maPhong";
        ArrayList<Phong> result = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, 0);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String maPhong = rs.getString("maPhong");

                        String maTang = rs.getString("maTang");
                        String tenPhong = rs.getString("tenPhong");
                        double dienTich = rs.getDouble("dienTich");
                        int loaiPhong = rs.getInt("loaiPhong");
                        int trangThaiPhong = 0;
                        int soNguoiHienTai = rs.getInt("soNguoiHienTai");
                        String maGiaDetail = rs.getString("maGiaDetail");

                        result.add(buildPhong(maPhong,trangThaiPhong,loaiPhong,maGiaDetail));
                    }
                }
            }
        } catch (SQLException e) {
            // In lỗi ra console để ông dễ debug nếu câu SQL sai tên cột
            System.err.println("Lỗi load ds phong trong " + e.getMessage());
        }
        return result;
    }

    public boolean updateTrangThaiPhong(String maPhong, String trangThai){
        String sql = "UPDATE Phong SET trangThaiPhong = ? WHERE maPhong = ?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, toTrangThai(trangThai));
            ps.setString(2, maPhong);
            int rowAffected = ps.executeUpdate();
            con.commit();
            return rowAffected > 0;

        }catch (SQLException e) {
            System.err.println("Lỗi khi thay đổi trạng thái phòng " + e.getMessage());
        }
        return false;
    }
}