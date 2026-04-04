package entity;

public class ChiSoDienNuoc {
    private String maPhong;
    private int thang;
    private int nam;
    private int soDien;
    private int soNuoc;

    public ChiSoDienNuoc() {}

    public ChiSoDienNuoc(String maPhong, int thang, int nam, int soDien, int soNuoc) {
        this.maPhong = maPhong;
        this.thang = thang;
        this.nam = nam;
        this.soDien = soDien;
        this.soNuoc = soNuoc;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public int getThang() { return thang; }
    public void setThang(int thang) { this.thang = thang; }

    public int getNam() { return nam; }
    public void setNam(int nam) { this.nam = nam; }

    public int getSoDien() { return soDien; }
    public void setSoDien(int soDien) { this.soDien = soDien; }

    public int getSoNuoc() { return soNuoc; }
    public void setSoNuoc(int soNuoc) { this.soNuoc = soNuoc; }

    @Override
    public String toString() {
        return "ChiSoDienNuoc{maPhong=" + maPhong + ", thang=" + thang
                + ", nam=" + nam + ", soDien=" + soDien + ", soNuoc=" + soNuoc + "}";
    }
}