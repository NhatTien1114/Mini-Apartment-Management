package ui.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý điều khoản hợp đồng.
 * Lưu trữ dạng file text đơn giản tại data/dieukhoan.txt.
 * Mỗi điều khoản nằm trên một dòng.
 */
public class DieuKhoanManager {
    private static final String FILE_PATH = "data/dieukhoan.txt";
    private static final DieuKhoanManager INSTANCE = new DieuKhoanManager();

    private final List<String> dieuKhoans = new ArrayList<>();

    private DieuKhoanManager() {
        load();
    }

    public static DieuKhoanManager getInstance() {
        return INSTANCE;
    }

    /**
     * Đọc điều khoản từ file. Nếu file chưa tồn tại, tạo mặc định.
     */
    private void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            createDefaults();
            save();
            return;
        }
        dieuKhoans.clear();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    dieuKhoans.add(trimmed);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file điều khoản: " + e.getMessage());
            if (dieuKhoans.isEmpty()) {
                createDefaults();
            }
        }
    }

    /**
     * Lưu điều khoản ra file.
     */
    public void save() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String dk : dieuKhoans) {
                writer.write(dk);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file điều khoản: " + e.getMessage());
        }
    }

    /**
     * Tạo danh sách điều khoản mặc định.
     */
    private void createDefaults() {
        dieuKhoans.clear();
        dieuKhoans.add("Bên B cam kết đến kí hợp đồng thuê nhà và dọn vào ở không quá 2 ngày so với ngày dọn vào dự kiến.");
        dieuKhoans.add("Bên B cam kết đến kí hợp đồng theo thời gian đã nêu, đóng đủ tiền nhà và các phí dịch vụ của tòa nhà tháng đầu tiên.");
        dieuKhoans.add("Nếu quá thời hạn nêu trên mà bên B vẫn không Ký hợp đồng thì bên B sẽ mất toàn bộ số tiền đặt cọc.");
        dieuKhoans.add("Trong trường hợp tòa nhà có quy định khách hàng được sang nhượng hợp đồng thuê thì bên B có thể tự sang nhượng hoặc nhờ bên A tìm khách sang nhượng trên tinh thần tự nguyện. Bên A không có trách nhiệm bắt buộc sang nhượng cho bên B.");
        dieuKhoans.add("Thỏa thuận này có hiệu lực từ thời điểm hai bên ký kết. Trường hợp khách hàng đặt cọc bằng hình thức chuyển khoản, hợp đồng này có hiệu lực khi Bên B đã đồng ý đặt cọc cho bên A và đã chuyển khoản tiền cọc cho bên A mà không cần phải có chữ ký của Bên B.");
        dieuKhoans.add("Thỏa thuận này sẽ chấm dứt hiệu lực trong các trường hợp sau: Bên B không ký hợp đồng và dọn vào ở theo quy định.");
        dieuKhoans.add("Bên A không cho bất kì một bên nào khác bên B đặt cọc hoặc thuê phòng là đối tượng được quy định trên hợp đồng này trong thời hạn thỏa thuận.");
        dieuKhoans.add("Ngay khi bên B ký hợp đồng thuê với chủ đầu tư, toàn bộ số tiền cọc sẽ được chuyển thành tiền ký quỹ trong hợp đồng thuê của Bên B.");
    }

    public List<String> getAll() {
        return new ArrayList<>(dieuKhoans);
    }

    public void add(String dieuKhoan) {
        dieuKhoans.add(dieuKhoan);
        save();
    }

    public void update(int index, String newContent) {
        if (index >= 0 && index < dieuKhoans.size()) {
            dieuKhoans.set(index, newContent);
            save();
        }
    }

    public void remove(int index) {
        if (index >= 0 && index < dieuKhoans.size()) {
            dieuKhoans.remove(index);
            save();
        }
    }

    public void moveUp(int index) {
        if (index > 0 && index < dieuKhoans.size()) {
            String item = dieuKhoans.remove(index);
            dieuKhoans.add(index - 1, item);
            save();
        }
    }

    public void moveDown(int index) {
        if (index >= 0 && index < dieuKhoans.size() - 1) {
            String item = dieuKhoans.remove(index);
            dieuKhoans.add(index + 1, item);
            save();
        }
    }

    /**
     * Reload lại từ file.
     */
    public void reload() {
        load();
    }
}
