package entity;

public class DichVu {

    private String maDichVu;
    private String tenDichVu;
    private String donVi;

    public DichVu() {
    }

    public DichVu(String maDichVu, String tenDichVu, String donVi) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donVi = donVi;
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

    @Override
    public String toString() {
        if (donVi == null || donVi.isBlank()) {
            return tenDichVu;
        }
        return tenDichVu + " (" + donVi + ")";
    }

}
