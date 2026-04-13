package entity;

public class HopDongKhachThue {
    private String maHDKT;
    private HopDong hopDong;
    private KhachHang khachHang;
    private VaiTro vaiTro;

    public enum VaiTro {
        DAI_DIEN("Đại Diện"), THANH_VIEN("Thành Viên"), DA_ROI("Đã rời đi");

        private String ten;

        VaiTro(String ten) {
            this.ten = ten;
        }

        public String getTen() {
            return ten;
        }

        public void setTen(String ten) {
            this.ten = ten;
        }

        public static VaiTro fromInt(int value) {
            if (value == 0)
                return DAI_DIEN;
            if (value == 1)
                return THANH_VIEN;
            if (value == 2)
                return DA_ROI;
            return DAI_DIEN;
        }

        public int toInt() {
            if (this == DAI_DIEN)
                return 0;
            if (this == THANH_VIEN)
                return 1;
            return 2;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    public HopDongKhachThue(String maHDKT, HopDong hopDong, KhachHang khachHang, VaiTro vaiTro) {
        this.maHDKT = maHDKT;
        this.hopDong = hopDong;
        this.khachHang = khachHang;
        this.vaiTro = vaiTro;
    }

    public String getMaHDKT() {
        return maHDKT;
    }

    public void setMaHDKT(String maHDKT) {
        this.maHDKT = maHDKT;
    }

    public HopDong getHopDong() {
        return hopDong;
    }

    public void setHopDong(HopDong hopDong) {
        this.hopDong = hopDong;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HopDongKhachThue{");
        sb.append("maHDKT=").append(maHDKT);
        sb.append(", hopDong=").append(hopDong);
        sb.append(", khachHang=").append(khachHang);
        sb.append(", vaiTro=").append(vaiTro);
        sb.append('}');
        return sb.toString();
    }
}
