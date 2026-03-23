package entity;

public class Toa {
    private String maToa;
    private String tenToa;
    private Chu chuHo;

    public Toa() {
    }

    public Toa(String maToa, String tenToa, Chu chuHo) {
        this.maToa = maToa;
        this.tenToa = tenToa;
        this.chuHo = chuHo;
    }

    public String getMaToa() {
        return maToa;
    }

    public void setMaToa(String maToa) {
        this.maToa = maToa;
    }

    public String getTenToa() {
        return tenToa;
    }

    public void setTenToa(String tenToa) {
        this.tenToa = tenToa;
    }

    public Chu getChuHo() {
        return chuHo;
    }

    public void setChuHo(Chu chuHo) {
        this.chuHo = chuHo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Toa{");
        sb.append("maToa=").append(maToa);
        sb.append(", tenToa=").append(tenToa);
        sb.append(", chuHo=").append(chuHo);
        sb.append('}');
        return sb.toString();
    }
}
