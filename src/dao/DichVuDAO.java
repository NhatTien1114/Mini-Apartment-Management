package dao;

import database.connectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVu> layTatCa() {
        String sql = "SELECT maDichVu, tenDichVu, donVi FROM DichVu ORDER BY tenDichVu";
        List<DichVu> ds = new ArrayList<>();

        try {
            Connection con = connectDB.getConnection();
            try (PreparedStatement ps = con.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getString("donVi")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi layTatCa DichVu: " + e.getMessage());
        }

        return ds;
    }

}
