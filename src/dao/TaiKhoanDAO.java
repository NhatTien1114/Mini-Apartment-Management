package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import database.connectDB;
import entity.Chu;
import entity.QuanLy;
import entity.TaiKhoan;

public class TaiKhoanDAO {
    // Kiểm tra Đăng nhập và nạp thông tin người dùng
    public TaiKhoan kiemTraDangNhap(String tenDangNhap, String matKhau) {
        String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            pst.setString(2, matKhau);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int role = rs.getInt("role");
                    TaiKhoan tk;
                    
                    if (role == 0) {
                        Chu c = new Chu();
                        c.setHoTen(rs.getString("hoTen"));
                        c.setSoDienThoai(rs.getString("soDienThoai"));
                        c.setDiaChi(rs.getString("diaChi"));
                        if (rs.getDate("ngaySinh") != null)
                            c.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
                        tk = c;
                    } else {
                        QuanLy q = new QuanLy();
                        q.setHoTen(rs.getString("hoTen"));
                        q.setSoDienThoai(rs.getString("soDienThoai"));
                        q.setDiaChi(rs.getString("diaChi"));
                        if (rs.getDate("ngaySinh") != null)
                            q.setNgaySinh(rs.getDate("ngaySinh").toLocalDate());
                        tk = q;
                    }
                    tk.setMaTaiKhoan(rs.getString("maTaiKhoan"));
                    tk.setEmail(rs.getString("tenDangNhap"));
                    tk.setMatKhau(rs.getString("matKhau"));
                    return tk;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Cập nhật thông tin cá nhân
    public boolean updateThongTinCaNhan(TaiKhoan tk) {
        String sql = "UPDATE TaiKhoan SET hoTen=?, soDienThoai=?, ngaySinh=?, diaChi=? WHERE maTaiKhoan=?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            if (tk instanceof Chu) {
                Chu c = (Chu) tk;
                pst.setString(1, c.getHoTen());
                pst.setString(2, c.getSoDienThoai());
                pst.setDate(3, c.getNgaySinh() != null ? java.sql.Date.valueOf(c.getNgaySinh()) : null);
                pst.setString(4, c.getDiaChi());
            } else if (tk instanceof QuanLy) {
                QuanLy ql = (QuanLy) tk;
                pst.setString(1, ql.getHoTen());
                pst.setString(2, ql.getSoDienThoai());
                pst.setDate(3, ql.getNgaySinh() != null ? java.sql.Date.valueOf(ql.getNgaySinh()) : null);
                pst.setString(4, ql.getDiaChi());
            } else {
                return false;
            }
            pst.setString(5, tk.getMaTaiKhoan());
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra tên đăng nhập
    public boolean kiemTraTonTaiTenDangNhap(String tenDangNhap) {
        String sql = "SELECT 1 FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Insert tài khoản với model Chu hoặc QuanLy
    public boolean insertTaiKhoan(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (maTaiKhoan, tenDangNhap, matKhau, hoTen, soDienThoai, ngaySinh, diaChi, role) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                   
        try (Connection con = connectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, tk.getMaTaiKhoan());
            pst.setString(2, tk.getEmail());
            pst.setString(3, tk.getMatKhau());
            
            if (tk instanceof Chu) {
                Chu c = (Chu) tk;
                pst.setString(4, c.getHoTen());
                pst.setString(5, c.getSoDienThoai());
                pst.setDate(6, java.sql.Date.valueOf(c.getNgaySinh()));
                pst.setString(7, c.getDiaChi());
            } else if (tk instanceof QuanLy) {
                QuanLy ql = (QuanLy) tk;
                pst.setString(4, ql.getHoTen());
                pst.setString(5, ql.getSoDienThoai());
                pst.setDate(6, java.sql.Date.valueOf(ql.getNgaySinh()));
                pst.setString(7, ql.getDiaChi());
            } else {
                pst.setString(4, null);
                pst.setString(5, null);
                pst.setDate(6, null);
                pst.setString(7, null);
            }
            pst.setInt(8, tk.getRole().getValue());
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Phát sinh mã tài khoản (Lấy 2 chữ cái đầu của tên tài khoản + số thứ tự)
    public String phatSinhMaTaiKhoan(String email) {
        String prefix = "TK";
        if (email != null && email.length() >= 2) {
            String cleanStr = email.split("@")[0].replaceAll("[^a-zA-Z]", "");
            if (cleanStr.length() >= 2) {
                prefix = cleanStr.substring(0, 2).toUpperCase();
            } else {
                prefix = email.split("@")[0].substring(0, 2).toUpperCase();
            }
        }
        
        String sql = "SELECT MAX(CAST(SUBSTRING(maTaiKhoan, 3, LEN(maTaiKhoan)-2) AS INT)) "
                   + "FROM TaiKhoan WHERE maTaiKhoan LIKE ?";
        try (Connection con = connectDB.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, prefix + "%");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int maxCount = rs.getInt(1);
                    return String.format("%s%02d", prefix, maxCount + 1);
                }
            }
        } catch (Exception e) {
        }
        return String.format("%s01", prefix);
    }
}
