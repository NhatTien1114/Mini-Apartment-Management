package entity;

public class Tang {
    private String maTang;
    private String tenTang;
    private Toa toa;

    public Tang() {
    }

    public Tang(String maTang, String tenTang, Toa toa) {
        this.maTang = maTang;
        this.tenTang = tenTang;
        this.toa = toa;
    }

    public String getMaTang() {
        return maTang;
    }

    public void setMaTang(String maTang) {
        this.maTang = maTang;
    }

    public String getTenTang() {
        return tenTang;
    }

    public void setTenTang(String tenTang) {
        this.tenTang = tenTang;
    }

    public Toa getToa() {
        return toa;
    }

    public void setToa(Toa toa) {
        this.toa = toa;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tang{");
        sb.append("maTang=").append(maTang);
        sb.append(", tenTang=").append(tenTang);
        sb.append(", toa=").append(toa);
        sb.append('}');
        return sb.toString();
    }

    
}
