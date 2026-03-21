package entity;

import java.time.LocalDate;

public class HopDong {
    private String maHopDong;
    private Phong maPhong;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private double tienCoc;
    private double tienThueThang;
    private TrangThai trangThai;

    public enum TrangThai {
        DANG_HIEU_LUC("Đang Hiệu Lực"), DA_KET_THUC("Đã Kết Thúc");

        private String ten;
        TrangThai(String ten) {
            this.ten = ten;
        }
        public String getTen() {
            return ten;
        }
        public void setTen(String ten) {
            this.ten = ten;
        }
        @Override
        public String toString() {
            return ten;
        }
    }

    public HopDong(String maHopDong, Phong maPhong, LocalDate ngayBatDau, LocalDate ngayKetThuc, double tienCoc, double tienThueThang, TrangThai trangThai) {
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienCoc = tienCoc;
        this.tienThueThang = tienThueThang;
        this.trangThai = trangThai;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public Phong getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(Phong maPhong) {
        this.maPhong = maPhong;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public double getTienThueThang() {
        return tienThueThang;
    }

    public void setTienThueThang(double tienThueThang) {
        this.tienThueThang = tienThueThang;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    } 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HopDong{");
        sb.append("maHopDong=").append(maHopDong);
        sb.append(", maPhong=").append(maPhong);
        sb.append(", ngayBatDau=").append(ngayBatDau);
        sb.append(", ngayKetThuc=").append(ngayKetThuc);
        sb.append(", tienCoc=").append(tienCoc);
        sb.append(", tienThueThang=").append(tienThueThang);
        sb.append(", trangThai=").append(trangThai);
        sb.append('}');
        return sb.toString();
    }
}
