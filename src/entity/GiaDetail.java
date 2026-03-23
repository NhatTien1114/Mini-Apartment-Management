package entity;

public class GiaDetail {
    private String maGiaDetail;
    private String maGiaHeader;
    private Integer loaiPhong;          // NULL nếu loai=DichVu, 0-20 nếu loai=Phong
    private String maDichVu;            // NULL nếu loai=Phong
    private double donGia;

    public GiaDetail() {}

    public GiaDetail(String maGiaDetail, String maGiaHeader, Integer loaiPhong, 
                     String maDichVu, double donGia) {
        this.maGiaDetail = maGiaDetail;
        this.maGiaHeader = maGiaHeader;
        this.loaiPhong = loaiPhong;
        this.maDichVu = maDichVu;
        this.donGia = donGia;
    }

    public String getMaGiaDetail() { return maGiaDetail; }
    public void setMaGiaDetail(String maGiaDetail) { this.maGiaDetail = maGiaDetail; }

    public String getMaGiaHeader() { return maGiaHeader; }
    public void setMaGiaHeader(String maGiaHeader) { this.maGiaHeader = maGiaHeader; }

    public Integer getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(Integer loaiPhong) { this.loaiPhong = loaiPhong; }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { this.maDichVu = maDichVu; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public boolean isPhongType() {
        return loaiPhong != null && maDichVu == null;
    }

    public boolean isDichVuType() {
        return loaiPhong == null && maDichVu != null;
    }
}
