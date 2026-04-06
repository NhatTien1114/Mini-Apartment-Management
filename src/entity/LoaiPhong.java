package entity;

public class LoaiPhong {
    private int maLoaiPhong;
    private String tenLoaiPhong;

    public LoaiPhong() {
    }

    public LoaiPhong(int maLoaiPhong, String tenLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public int getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(int maLoaiPhong) {
        this.maLoaiPhong = maLoaiPhong;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String tenLoaiPhong) {
        this.tenLoaiPhong = tenLoaiPhong;
    }

    public String getTen() {
        return tenLoaiPhong;
    }

    public int ordinal() {
        return maLoaiPhong;
    }

    @Override
    public String toString() {
        return tenLoaiPhong;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return maLoaiPhong == ((LoaiPhong) o).maLoaiPhong;
    }

    @Override
    public int hashCode() {
        return maLoaiPhong;
    }
}
