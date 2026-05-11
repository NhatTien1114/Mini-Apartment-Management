package ui.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Tiện ích định dạng tiền tệ và ngày tháng dùng chung toàn dự án.
 * Thay thế các đoạn inline lặp lại trong BangGiaUI, DoanhThuUI, HoaDonUI,
 * HopDongUI, PhongInfo, AccountInfoDialog.
 */
public final class FormatUtils {

    public static final DateTimeFormatter DATE_DMY   = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATE_YMD   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("'Tháng' MM, yyyy");

    private static final NumberFormat VN_NUMBER =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private FormatUtils() {}

    /** Định dạng số tiền theo locale Việt Nam, thêm ký hiệu "đ". */
    public static String formatCurrency(double amount) {
        return VN_NUMBER.format((long) amount) + "đ";
    }

    /** Định dạng LocalDate sang "dd/MM/yyyy". */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_DMY);
    }

    /**
     * Chuyển chuỗi "yyyy-MM-dd" sang "dd/MM/yyyy".
     * Trả về chuỗi gốc nếu không parse được.
     */
    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "";
        try {
            return LocalDate.parse(isoDate.trim(), DATE_YMD).format(DATE_DMY);
        } catch (DateTimeParseException e) {
            return isoDate;
        }
    }

    /**
     * Parse chuỗi "dd/MM/yyyy" thành LocalDate.
     * Trả về null nếu không parse được.
     */
    public static LocalDate parseDate(String dmyDate) {
        if (dmyDate == null || dmyDate.isBlank()) return null;
        try {
            return LocalDate.parse(dmyDate.trim(), DATE_DMY);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
