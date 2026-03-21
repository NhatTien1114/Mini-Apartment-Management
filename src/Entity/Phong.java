package entity;

public class Phong {
    private String maPhong;
    private Tang maTang;
    private String tenPhong;
    private double dienTich;
    private LoaiPhong loaiPhong;
    private TrangThai trangThai;
    private int soNguoiHienTai;

    public enum LoaiPhong {
		DON("Phòng Đơn"), DOI("Phòng Đôi"), STUDIO("Phòng Studio");

		private String ten;

		LoaiPhong(String ten) {
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

    public enum TrangThai {
		TRONG("Trống"), THUE("Đã Thuê"), SUA("Sửa Chữa"), COC("Đã Cọc");

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
    
    public Phong(double dienTich, LoaiPhong loaiPhong, String maPhong, Tang maTang, int soNguoiHienTai, String tenPhong, TrangThai trangThai) {
        this.dienTich = dienTich;
        this.loaiPhong = loaiPhong;
        this.maPhong = maPhong;
        this.maTang = maTang;
        this.soNguoiHienTai = soNguoiHienTai;
        this.tenPhong = tenPhong;
        this.trangThai = trangThai;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Tang getMaTang() {
        return maTang;
    }

    public void setMaTang(Tang maTang) {
        this.maTang = maTang;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public double getDienTich() {
        return dienTich;
    }

    public void setDienTich(double dienTich) {
        this.dienTich = dienTich;
    }

    public LoaiPhong getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(LoaiPhong loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public TrangThai getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThai trangThai) {
        this.trangThai = trangThai;
    }

    public int getSoNguoiHienTai() {
        return soNguoiHienTai;
    }

    public void setSoNguoiHienTai(int soNguoiHienTai) {
        this.soNguoiHienTai = soNguoiHienTai;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Phong{");
        sb.append("maPhong=").append(maPhong);
        sb.append(", maTang=").append(maTang);
        sb.append(", tenPhong=").append(tenPhong);
        sb.append(", dienTich=").append(dienTich);
        sb.append(", loaiPhong=").append(loaiPhong);
        sb.append(", trangThai=").append(trangThai);
        sb.append(", soNguoiHienTai=").append(soNguoiHienTai);
        sb.append('}');
        return sb.toString();
    }
}
