package service;

import dao.DichVuDAO;
import dao.GiaDetailDAO;
import dao.GiaHeaderDAO;
import database.connectDB;
import entity.DichVu;
import entity.GiaDetail;
import entity.GiaHeader;
import entity.Phong;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BangGiaService {

    public static class LuuChiTietItem {
        private final String maDinhDanh;
        private final double donGia;

        public LuuChiTietItem(String maDinhDanh, double donGia) {
            this.maDinhDanh = maDinhDanh;
            this.donGia = donGia;
        }

        public String getMaDinhDanh() {
            return maDinhDanh;
        }

        public double getDonGia() {
            return donGia;
        }
    }

    public static class LoaiPhongItem {
        private final int id;
        private final String ten;

        public LoaiPhongItem(int id, String ten) {
            this.id = id;
            this.ten = ten;
        }

        public int getId() {
            return id;
        }

        public String getTen() {
            return ten;
        }
    }

    private final GiaHeaderDAO headerDAO = new GiaHeaderDAO();
    private final GiaDetailDAO detailDAO = new GiaDetailDAO();
    private final DichVuDAO dichVuDAO = new DichVuDAO();

    public List<GiaHeader> layTatCaHeader() {
        return headerDAO.layTatCa();
    }

    public List<GiaHeader> layHeaderTheoLoai(int loai) {
        return headerDAO.layTheoLoai(loai);
    }

    public GiaHeader layHeaderTheoMa(String maGiaHeader) {
        return headerDAO.layTheoMa(maGiaHeader);
    }

    public List<GiaDetail> layDetailTheoHeader(String maGiaHeader) {
        return detailDAO.layTheoHeader(maGiaHeader);
    }

    public List<LoaiPhongItem> layDanhSachLoaiPhong() {
        List<LoaiPhongItem> ds = new ArrayList<>();
        Phong.LoaiPhong[] values = Phong.LoaiPhong.values();
        for (int i = 0; i < values.length; i++) {
            ds.add(new LoaiPhongItem(i, values[i].getTen()));
        }
        return ds;
    }

    public List<DichVu> layDanhSachDichVu() {
        return dichVuDAO.layTatCa();
    }

    public String themHeader(int loai, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String moTa, int trangThai, String nguoiCapNhat) {
        if (ngayBatDau == null) {
            return "Ngày bắt đầu không được để trống";
        }
        if (ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu";
        }

        String maMoi = headerDAO.taoMaMoi(loai);
        GiaHeader h = new GiaHeader(maMoi, ngayBatDau, ngayKetThuc, moTa, trangThai, nguoiCapNhat, loai);
        boolean ok = headerDAO.them(h);
        return ok ? null : "Không thể thêm bảng giá";
    }

    public String capNhatNgayKetThuc(String maGiaHeader, LocalDate ngayKetThuc, String nguoiCapNhat) {
        GiaHeader current = headerDAO.layTheoMa(maGiaHeader);
        if (current == null) {
            return "Không tìm thấy bảng giá";
        }
        if (ngayKetThuc != null && ngayKetThuc.isBefore(current.getNgayBatDau())) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu";
        }

        boolean ok = headerDAO.capNhatNgayKetThuc(maGiaHeader, ngayKetThuc, nguoiCapNhat);
        return ok ? null : "Không thể cập nhật ngày kết thúc";
    }

    private boolean updateActiveKeys(Connection con, int loai, List<GiaDetail> details) throws SQLException {
        if (loai == 0) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE Phong SET maGiaDetail = ? WHERE loaiPhong = ?")) {
                for (GiaDetail d : details) {
                    if (d.getLoaiPhong() != null) {
                        ps.setString(1, d.getMaGiaDetail());
                        ps.setInt(2, d.getLoaiPhong());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement("UPDATE DichVu SET maGiaDetail = ? WHERE maDichVu = ?")) {
                for (GiaDetail d : details) {
                    if (d.getMaDichVu() != null) {
                        ps.setString(1, d.getMaGiaDetail());
                        ps.setString(2, d.getMaDichVu());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
        }
        return true;
    }

    public String taoBangGiaDayDu(int loai, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String moTa, int trangThai, List<LuuChiTietItem> items, String nguoiCapNhat) {
        if (ngayBatDau == null) {
            return "Ngày bắt đầu không được để trống";
        }
        if (ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu";
        }
        if (items == null || items.isEmpty()) {
            return "Cần ít nhất một dòng chi tiết";
        }

        Set<String> seen = new HashSet<>();
        List<GiaDetail> details = new ArrayList<>();
        for (LuuChiTietItem item : items) {
            String ma = item.getMaDinhDanh();
            if (ma == null || ma.isBlank()) {
                return "Thiếu thông tin loại ở một dòng chi tiết";
            }
            if (!seen.add(ma)) {
                return "Không được trùng loại trong cùng một bảng giá";
            }
            if (item.getDonGia() < 0) {
                return "Đơn giá phải lớn hơn hoặc bằng 0";
            }

            GiaDetail d = new GiaDetail();
            d.setDonGia(item.getDonGia());
            if (loai == 0) {
                d.setLoaiPhong(Integer.valueOf(ma));
                d.setMaDichVu(null);
            } else {
                d.setLoaiPhong(null);
                d.setMaDichVu(ma);
            }
            details.add(d);
        }

        String maHeader = headerDAO.taoMaMoi(loai);
        GiaHeader header = new GiaHeader(maHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, nguoiCapNhat, loai);

        Connection con = null;
        boolean autoCommit = true;
        try {
            con = connectDB.getConnection();
            autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            if (!headerDAO.them(con, header)) {
                con.rollback();
                return "Không thể thêm bảng giá";
            }

            for (GiaDetail d : details) {
                d.setMaGiaHeader(maHeader);
            }
            if (!detailDAO.thayTheChiTiet(con, maHeader, details)) {
                con.rollback();
                return "Không thể thêm chi tiết bảng giá";
            }

            if (trangThai == 1) {
                if (!updateActiveKeys(con, loai, details)) {
                    con.rollback();
                    return "Không thể cập nhật giá trị kích hoạt cho Dịch vụ / Phòng";
                }
            }

            con.commit();
            return null;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            return "Lỗi tạo bảng giá: " + e.getMessage();
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public String capNhatHeader(String maGiaHeader, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String moTa, int trangThai, String nguoiCapNhat) {
        GiaHeader current = headerDAO.layTheoMa(maGiaHeader);
        if (current == null) {
            return "Không tìm thấy bảng giá cần sửa";
        }
        if (ngayBatDau == null) {
            return "Ngày bắt đầu không được để trống";
        }
        if (ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu";
        }

        current.setNgayBatDau(ngayBatDau);
        current.setNgayKetThuc(ngayKetThuc);
        current.setMoTa(moTa);
        current.setTrangThai(trangThai);
        current.setGhiChu(nguoiCapNhat);

        boolean ok = headerDAO.capNhat(current);
        return ok ? null : "Không thể cập nhật bảng giá";
    }

    public String xoaHeader(String maGiaHeader) {
        boolean ok = headerDAO.xoa(maGiaHeader);
        return ok ? null : "Không thể xóa bảng giá";
    }

    public String luuChiTiet(String maGiaHeader, int loai, List<LuuChiTietItem> items, String nguoiCapNhat) {
        if (items == null || items.isEmpty()) {
            return "Cần ít nhất một dòng chi tiết";
        }

        Set<String> seen = new HashSet<>();
        List<GiaDetail> details = new ArrayList<>();
        for (LuuChiTietItem item : items) {
            String ma = item.getMaDinhDanh();
            if (ma == null || ma.isBlank()) {
                return "Thiếu thông tin loại ở một dòng chi tiết";
            }
            if (item.getDonGia() < 0) {
                return "Đơn giá phải lớn hơn hoặc bằng 0";
            }
            if (!seen.add(ma)) {
                return "Không được trùng loại trong cùng một bảng giá";
            }

            GiaDetail detail = new GiaDetail();
            detail.setMaGiaHeader(maGiaHeader);
            detail.setDonGia(item.getDonGia());
            if (loai == 0) {
                detail.setLoaiPhong(Integer.valueOf(ma));
                detail.setMaDichVu(null);
            } else {
                detail.setLoaiPhong(null);
                detail.setMaDichVu(ma);
            }
            details.add(detail);
        }

        Connection con = null;
        boolean autoCommit = true;
        try {
            con = connectDB.getConnection();
            autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            if (!detailDAO.thayTheChiTiet(con, maGiaHeader, details)) {
                con.rollback();
                return "Không thể lưu chi tiết bảng giá";
            }

            GiaHeader header = headerDAO.layTheoMa(maGiaHeader);
            if (header == null) {
                con.rollback();
                return "Không tìm thấy bảng giá";
            }
            header.setGhiChu(nguoiCapNhat);
            if (!headerDAO.capNhat(header)) {
                con.rollback();
                return "Không thể cập nhật người sửa";
            }

            if (header.getTrangThai() == 1) {
                if (!updateActiveKeys(con, loai, details)) {
                    con.rollback();
                    return "Không thể cập nhật giá trị kích hoạt cho Dịch vụ / Phòng";
                }
            }

            con.commit();
            return null;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignored) {
                }
            }
            return "Lỗi lưu chi tiết: " + e.getMessage();
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(autoCommit);
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
