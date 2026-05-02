package database;
import java.sql.*;
import java.time.LocalDate;

public class TestQuery {
    public static void main(String[] args) {
        try (Connection con = connectDB.getConnection()) {
            System.out.println("Connecting...");
            String sql = "SELECT cs.*, hd.maPhong FROM ChiSoDienNuoc cs JOIN HopDong hd ON cs.maHopDong = hd.maHopDong WHERE hd.maPhong = 'P1.01' ORDER BY cs.ngayGhi DESC";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                System.out.println("Results for P1.01:");
                while (rs.next()) {
                    System.out.println("MaCS: " + rs.getInt("maChiSo") + " | MaHD: " + rs.getString("maHopDong") + " | Ngay: " + rs.getDate("ngayGhi") + " | SoDien: " + rs.getInt("soDien") + " | SoNuoc: " + rs.getInt("soNuoc"));
                }
            }
            
            System.out.println("\nTesting layChiSoGanNhatTheoPhongTruocNgay('P1.01', '2026-05-01'):");
            String sql2 = "SELECT TOP 1 cs.soDien, cs.soNuoc FROM ChiSoDienNuoc cs "
                + "JOIN HopDong hd ON cs.maHopDong = hd.maHopDong "
                + "WHERE hd.maPhong = 'P1.01' AND cs.ngayGhi < '2026-05-01' "
                + "ORDER BY cs.ngayGhi DESC";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql2)) {
                if (rs.next()) {
                    System.out.println("Result: SoDien=" + rs.getInt("soDien") + ", SoNuoc=" + rs.getInt("soNuoc"));
                } else {
                    System.out.println("No result found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
