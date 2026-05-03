package entity;

import java.util.Date;
import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private String maHopDong;
    private String maPhong;
    private Date tuNgay;
    private Date denNgay;
    private int trangThaiThanhToan; // 0: Chưa TT, 1: Đã TT, 2: Thanh toán trễ
    private double tienPhat;        // Tiền phạt cộng dồn (tính từ ngày 4 quá hạn)
    private TaiKhoan nguoiLap;
    private Timestamp createdAt;

    public enum TrangThaiThanhToan {
        CHUA_THANH_TOAN("Chưa Thanh Toán"),
        DA_THANH_TOAN("Đã Thanh Toán"),
        THANH_TOAN_TRE("Thanh Toán Trễ");

        private String ten;

        TrangThaiThanhToan(String ten) {
            this.ten = ten;
        }

        public String getTen() {
            return ten;
        }

        public void setTen(String ten) {
            this.ten = ten;
        }
        // Chuyển từ số trong Database (0, 1) sang Enum
        public static TrangThaiThanhToan fromInt(int value) {
            if (value == 1) return DA_THANH_TOAN;
            if (value == 0) return CHUA_THANH_TOAN;
            if (value == 2) return THANH_TOAN_TRE;
            return CHUA_THANH_TOAN; // Mặc định nếu dữ liệu lạ
        }
        // Chuyển từ Enum sang số (0, 1, 2) để lưu xuống Database
        public int toInt() {
            switch (this) {
                case DA_THANH_TOAN:   return 1;
                case THANH_TOAN_TRE:  return 2;
                default:              return 0;
            }
        }
        @Override
        public String toString() {
            return ten;
        }
    }

    // Constructor mặc định
    public HoaDon() {
    }

    // Constructor đầy đủ tham số
    public HoaDon(String maHoaDon, String maHopDong, String maPhong, Date tuNgay, Date denNgay, int trangThaiThanhToan, TaiKhoan nguoiLap, Timestamp createdAt) {
        this.maHoaDon = maHoaDon;
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.nguoiLap = nguoiLap;
        this.createdAt = createdAt;
    }
    public HoaDon(String maHoaDon){
        this.maHoaDon = maHoaDon;
    }

    // Getters và Setters
    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Date getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(Date tuNgay) {
        this.tuNgay = tuNgay;
    }

    public Date getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(Date denNgay) {
        this.denNgay = denNgay;
    }

    public int getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(int trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public double getTienPhat() {
        return tienPhat;
    }

    public void setTienPhat(double tienPhat) {
        this.tienPhat = tienPhat;
    }

    public TaiKhoan getNguoiLap() {
        return nguoiLap;
    }

    public void setNguoiLap(TaiKhoan nguoiLap) {
        this.nguoiLap = nguoiLap;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
