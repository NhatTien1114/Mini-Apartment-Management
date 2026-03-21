package dao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DAO quản lý phòng — lưu in-memory, dễ swap sang DB sau.
 * Format phòng hợp lệ: T{tầng}.{số phòng 2 chữ số}  VD: T1.01, T2.06, T6.04
 */
public class PhongDAO {

    // ── Model nội bộ ────────────────────────────────────────────────────────
    public static class Phong {
        public String  maPhong;       // VD: T2.05
        public long    giaThue;       // VNĐ/tháng
        public String  trangThai;     // "Trống" | "Đã thuê" | "Đang sửa"
        public List<String> dichVu;   // danh sách tên dịch vụ đã chọn

        public Phong(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
            this.maPhong   = maPhong;
            this.giaThue   = giaThue;
            this.trangThai = trangThai;
            this.dichVu    = dichVu != null ? new ArrayList<>(dichVu) : new ArrayList<>();
        }

        @Override public String toString() {
            return maPhong + " [" + trangThai + "] " + giaThue + "đ";
        }
    }

    // ── Format hợp lệ: T{1-6}.{01-09} ──────────────────────────────────────
    private static final Pattern ROOM_PATTERN =
            Pattern.compile("^T[1-6]\\.(0[1-9]|[1-9]\\d)$");

    // ── Store (LinkedHashMap giữ thứ tự thêm vào) ───────────────────────────
    private final Map<String, Phong> store = new LinkedHashMap<>();

    // ── Dữ liệu mẫu ─────────────────────────────────────────────────────────
    public PhongDAO() {
        List<String> dvMac = List.of("Điện", "Nước", "Internet", "Rác");
        String[][] defaults = {
                {"T1.01"},{"T1.02"},{"T1.03"},{"T1.04"},{"T1.05"},
                {"T2.01"},{"T2.02"},{"T2.03"},{"T2.04"},{"T2.05"},{"T2.06"},
                {"T3.01"},{"T3.02"},{"T3.03"},{"T3.04"},{"T3.05"},
                {"T4.01"},{"T4.02"},{"T4.03"},{"T4.04"},{"T4.05"},
                {"T5.01"},{"T5.02"},{"T5.03"},{"T5.04"},{"T5.05"},
                {"T6.01"},{"T6.02"},{"T6.03"},{"T6.04"},
        };
        for (String[] r : defaults)
            store.put(r[0], new Phong(r[0], 3_000_000, "Trống", dvMac));
        // Demo: 1 phòng đã thuê
        store.get("T6.01").trangThai = "Đã thuê";
        store.get("T5.03").trangThai = "Đang sửa";
    }

    // ── Validate format ──────────────────────────────────────────────────────
    public boolean isValidFormat(String maPhong) {
        return maPhong != null && ROOM_PATTERN.matcher(maPhong.trim().toUpperCase()).matches();
    }

    public String normalise(String maPhong) {
        return maPhong == null ? "" : maPhong.trim().toUpperCase();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String them(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        if (!isValidFormat(ma))
            return "Tên phòng không đúng định dạng (VD: T1.01, T2.06)";
        if (store.containsKey(ma))
            return "Phòng \"" + ma + "\" đã tồn tại";
        if (giaThue <= 0)
            return "Giá thuê phải lớn hơn 0";
        store.put(ma, new Phong(ma, giaThue, trangThai, dichVu));
        return null;
    }

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Phong> layTatCa() {
        return new ArrayList<>(store.values());
    }

    public Phong layTheoMa(String maPhong) {
        return store.get(normalise(maPhong));
    }

    public boolean tonTai(String maPhong) {
        return store.containsKey(normalise(maPhong));
    }

    /** Lấy danh sách phòng theo tầng, VD tầng "T3" */
    public List<Phong> layTheoTang(String tang) {
        List<Phong> result = new ArrayList<>();
        for (Phong p : store.values())
            if (p.maPhong.startsWith(tang + "."))
                result.add(p);
        return result;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String capNhat(String maPhong, long giaThue, String trangThai, List<String> dichVu) {
        String ma = normalise(maPhong);
        if (!store.containsKey(ma))
            return "Không tìm thấy phòng \"" + ma + "\"";
        if (giaThue <= 0)
            return "Giá thuê phải lớn hơn 0";
        Phong p = store.get(ma);
        p.giaThue   = giaThue;
        p.trangThai = trangThai;
        p.dichVu    = dichVu != null ? new ArrayList<>(dichVu) : new ArrayList<>();
        return null;
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    /**
     * @return null nếu thành công, chuỗi lỗi nếu thất bại
     */
    public String xoa(String maPhong) {
        String ma = normalise(maPhong);
        if (!store.containsKey(ma))
            return "Không tìm thấy phòng \"" + ma + "\"";
        String tt = store.get(ma).trangThai;
        if ("Đã thuê".equals(tt) || "Đã cọc".equals(tt))
            return "Không thể xóa phòng đang có người thuê hoặc đã cọc";
        store.remove(ma);
        return null;
    }
}