package dao;

import database.connectDB;
import entity.HopDong;
import entity.Phong;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import ui.main.HopDongUI;

public class HopDongDAO {
    public String maHopDong;
    public String maPhong;
    public Date ngayBatDau;
    public Date ngayKetThuc;
    public double tienCoc;
    public double tienThueThang;
    public String trangThai;


    public ArrayList<HopDong> getAllHopDong() {
        String sql = "SELECT maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai FROM HopDong ORDER BY maHopDong";
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

                    int trangThaiInt = rs.getInt("trangThai");
                    HopDong.TrangThai trangThai = HopDong.TrangThai.fromInt(trangThaiInt);

                    entity.Phong phong = new Phong(maPhong);

                    listHD.add(new HopDong(maHopDong,phong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai));
                }
            }
            return listHD;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách hợp đồng.", e);
        }
    }

    public boolean luuHopDongMoi(HopDongUI.ContractDraft draft) {
        Connection con = null;
        try {
            con = connectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction để đảm bảo an toàn dữ liệu

            // Bước 1: Thêm Khách Hàng (Dùng MERGE hoặc kiểm tra EXISTS để tránh trùng mã)
            String sqlKH = "IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE maKhachHang = ?) " +
                    "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, soCCCD, diaChiThuongTru) VALUES (?,?,?,?,?)";
            String maKH = "KH" + System.currentTimeMillis(); // Tạo mã tạm hoặc dùng CCCD làm mã
            try (PreparedStatement psKH = con.prepareStatement(sqlKH)) {
                psKH.setString(1, draft.cccd); // Giả sử dùng CCCD để kiểm tra trùng
                psKH.setString(2, maKH);
                psKH.setString(3, draft.hoTen);
                psKH.setString(4, draft.soDienThoai);
                psKH.setString(5, draft.cccd);
                psKH.setString(6, draft.diaChi);
                psKH.executeUpdate();
            }

            // Bước 2: Thêm Hợp Đồng
            String maHD = "HD" + System.currentTimeMillis();
            String sqlHD = "INSERT INTO HopDong (maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai) VALUES (?,?,?,?,?,?,1)";
            try (PreparedStatement psHD = con.prepareStatement(sqlHD)) {
                psHD.setString(1, maHD);
                psHD.setString(2, draft.phong);
                // Chuyển dd/MM/yyyy sang yyyy-MM-dd cho SQL
                psHD.setDate(3, java.sql.Date.valueOf(convertToSqlDate(draft.ngayBatDau)));
                psHD.setDate(4, java.sql.Date.valueOf(convertToSqlDate(draft.ngayKetThuc)));
                psHD.setDouble(5, Double.parseDouble(draft.tienCocRaw));
                psHD.setDouble(6, Double.parseDouble(draft.giaThueRaw));
                psHD.executeUpdate();
            }

            // Bước 3: Thêm liên kết HopDongKhachHang (Người đại diện vaiTro = 0)
            String sqlHDKH = "INSERT INTO HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro) VALUES (?,?,?,0)";
            try (PreparedStatement psHDKH = con.prepareStatement(sqlHDKH)) {
                psHDKH.setString(1, "HDKT" + System.currentTimeMillis());
                psHDKH.setString(2, maHD);
                psHDKH.setString(3, maKH);
                psHDKH.executeUpdate();
            }

            con.commit(); // Hoàn tất
            return true;
        } catch (Exception e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        }
    }

    // Hàm phụ chuyển định dạng ngày
    private String convertToSqlDate(String dateStr) {
        String[] p = dateStr.split("/");
        return p[2] + "-" + p[1] + "-" + p[0];
    }
}
