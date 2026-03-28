package entity;

public class DichVu {

    private String maDichVu;
    private String tenDichVu;
    private String donVi;
    private String maGiaDetail;
    private Double donGia;

    public DichVu() {
    }

    public DichVu(String maDichVu, String tenDichVu, String donVi) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donVi = donVi;
    }

    public DichVu(String maDichVu, String tenDichVu, String donVi, String maGiaDetail, Double donGia) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donVi = donVi;
        this.maGiaDetail = maGiaDetail;
        this.donGia = donGia;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public String getDonVi() {
        return donVi;
    }

    public void setDonVi(String donVi) {
        this.donVi = donVi;
    }

    public String getMaGiaDetail() {
        return maGiaDetail;
    }

    public void setMaGiaDetail(String maGiaDetail) {
        this.maGiaDetail = maGiaDetail;
    }

    public Double getDonGia() {
        return donGia;
    }

    public void setDonGia(Double donGia) {
        this.donGia = donGia;
    }

    @Override
    public String toString() {
        if (donVi == null || donVi.isBlank()) {
            return tenDichVu;
        }
        return tenDichVu + " (" + donVi + ")";
    }

}
