package entity;

import java.time.LocalDate;

public class ChiSoDienNuoc {
    private int maChiSo;
    private String maHopDong;
    private LocalDate ngayGhi;
    private int soDien;
    private int soNuoc;

    public ChiSoDienNuoc() {
    }

    public ChiSoDienNuoc(String maHopDong, LocalDate ngayGhi, int soDien, int soNuoc) {
        this.maHopDong = maHopDong;
        this.ngayGhi = ngayGhi;
        this.soDien = soDien;
        this.soNuoc = soNuoc;
    }

    public ChiSoDienNuoc(int maChiSo, String maHopDong, LocalDate ngayGhi, int soDien, int soNuoc) {
        this.maChiSo = maChiSo;
        this.maHopDong = maHopDong;
        this.ngayGhi = ngayGhi;
        this.soDien = soDien;
        this.soNuoc = soNuoc;
    }

    public int getMaChiSo() {
        return maChiSo;
    }

    public void setMaChiSo(int maChiSo) {
        this.maChiSo = maChiSo;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
    }

    public LocalDate getNgayGhi() {
        return ngayGhi;
    }

    public void setNgayGhi(LocalDate ngayGhi) {
        this.ngayGhi = ngayGhi;
    }

    public int getSoDien() {
        return soDien;
    }

    public void setSoDien(int soDien) {
        this.soDien = soDien;
    }

    public int getSoNuoc() {
        return soNuoc;
    }

    public void setSoNuoc(int soNuoc) {
        this.soNuoc = soNuoc;
    }

    @Override
    public String toString() {
        return "ChiSoDienNuoc{maChiSo=" + maChiSo + ", maHopDong=" + maHopDong
                + ", ngayGhi=" + ngayGhi + ", soDien=" + soDien + ", soNuoc=" + soNuoc + "}";
    }
}
