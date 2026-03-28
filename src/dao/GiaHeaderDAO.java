package dao;

import database.connectDB;
import entity.GiaHeader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GiaHeaderDAO {

    public List<GiaHeader> layTatCa() {
        String sql = "SELECT maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, ghiChu, loai "
                + "FROM GiaHeader ORDER BY ngayBatDau DESC, maGiaHeader DESC";
        List<GiaHeader> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCa GiaHeader: " + e.getMessage());
        }

        return ds;
    }

    public List<GiaHeader> layTheoLoai(int loai) {
        String sql = "SELECT maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, ghiChu, loai "
                + "FROM GiaHeader WHERE loai = ? ORDER BY ngayBatDau DESC, maGiaHeader DESC";
        List<GiaHeader> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, loai);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ds.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTheoLoai GiaHeader: " + e.getMessage());
        }

        return ds;
    }

    public GiaHeader layTheoMa(String maGiaHeader) {
        String sql = "SELECT maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, ghiChu, loai "
                + "FROM GiaHeader WHERE maGiaHeader = ?";

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, maGiaHeader);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTheoMa GiaHeader: " + e.getMessage());
        }

        return null;
    }

    public boolean them(GiaHeader header) {
        String sql = "INSERT INTO GiaHeader(maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, ghiChu, loai) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                fillParams(ps, header);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi them GiaHeader: " + e.getMessage());
            return false;
        }
    }

    public boolean them(Connection con, GiaHeader header) throws SQLException {
        String sql = "INSERT INTO GiaHeader(maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, ghiChu, loai) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            fillParams(ps, header);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatNgayKetThuc(String maGiaHeader, LocalDate ngayKetThuc, String nguoiCapNhat) {
        String sql = "UPDATE GiaHeader SET ngayKetThuc = ?, ghiChu = ? WHERE maGiaHeader = ?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (ngayKetThuc == null) {
                    ps.setNull(1, java.sql.Types.DATE);
                } else {
                    ps.setDate(1, Date.valueOf(ngayKetThuc));
                }
                ps.setString(2, nguoiCapNhat);
                ps.setString(3, maGiaHeader);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi capNhatNgayKetThuc GiaHeader: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(GiaHeader header) {
        String sql = "UPDATE GiaHeader SET ngayBatDau=?, ngayKetThuc=?, moTa=?, trangThai=?, ghiChu=?, loai=? "
                + "WHERE maGiaHeader=?";
        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(header.getNgayBatDau()));
                if (header.getNgayKetThuc() == null) {
                    ps.setNull(2, java.sql.Types.DATE);
                } else {
                    ps.setDate(2, Date.valueOf(header.getNgayKetThuc()));
                }
                ps.setString(3, header.getMoTa());
                ps.setInt(4, header.getTrangThai());
                ps.setString(5, header.getGhiChu());
                ps.setInt(6, header.getLoai());
                ps.setString(7, header.getMaGiaHeader());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi capNhat GiaHeader: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maGiaHeader) {
        Connection con = null;
        boolean autoCommit = true;
        try {
            con = connectDB.getConnection();
            autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement psDetail = con.prepareStatement("DELETE FROM GiaDetail WHERE maGiaHeader = ?")) {
                psDetail.setString(1, maGiaHeader);
                psDetail.executeUpdate();
            }

            int rows;
            try (PreparedStatement psHeader = con.prepareStatement("DELETE FROM GiaHeader WHERE maGiaHeader = ?")) {
                psHeader.setString(1, maGiaHeader);
                rows = psHeader.executeUpdate();
            }

            con.commit();
            return rows > 0;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            System.err.println("Lỗi xoa GiaHeader: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public String taoMaMoi(int loai) {
        String prefix = loai == 0 ? "GHP" : "GHD";
        String sql = "SELECT MAX(CAST(SUBSTRING(maGiaHeader, ?, LEN(maGiaHeader) - (? - 1)) AS INT)) "
                + "FROM GiaHeader WHERE maGiaHeader LIKE ?";

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int start = prefix.length() + 1;
                ps.setInt(1, start);
                ps.setInt(2, start);
                ps.setString(3, prefix + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    int next = 1;
                    if (rs.next()) {
                        int max = rs.getInt(1);
                        if (!rs.wasNull()) {
                            next = max + 1;
                        }
                    }
                    return String.format("%s%03d", prefix, next);
                }
            }
        } catch (SQLException e) {
            return prefix + System.currentTimeMillis();
        }
    }

    private GiaHeader mapRow(ResultSet rs) throws SQLException {
        Date ketThuc = rs.getDate("ngayKetThuc");
        LocalDate ngayKetThuc = ketThuc == null ? null : ketThuc.toLocalDate();
        return new GiaHeader(
                rs.getString("maGiaHeader"),
                rs.getDate("ngayBatDau").toLocalDate(),
                ngayKetThuc,
                rs.getString("moTa"),
                rs.getInt("trangThai"),
                rs.getString("ghiChu"),
                rs.getInt("loai"));
    }

    private void fillParams(PreparedStatement ps, GiaHeader header) throws SQLException {
        ps.setString(1, header.getMaGiaHeader());
        ps.setDate(2, Date.valueOf(header.getNgayBatDau()));
        if (header.getNgayKetThuc() == null) {
            ps.setNull(3, java.sql.Types.DATE);
        } else {
            ps.setDate(3, Date.valueOf(header.getNgayKetThuc()));
        }
        ps.setString(4, header.getMoTa());
        ps.setInt(5, header.getTrangThai());
        ps.setString(6, header.getGhiChu());
        ps.setInt(7, header.getLoai());
    }

}
