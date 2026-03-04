package ui.util;
import java.awt.Font;
import java.io.InputStream;

public class FontUtils {
    private static Font beVietnamPro;

    static {
        try {
            InputStream is = FontUtils.class.getResourceAsStream(
                "/font/BeVietnamPro-Regular.ttf"
            );

            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            beVietnamPro = baseFont.deriveFont(14f);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Font getFont(float size) {
        return beVietnamPro.deriveFont(size);
    }
}
