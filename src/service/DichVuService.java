package service;

import dao.DichVuDAO;
import entity.DichVu;
import java.util.List;

public class DichVuService {
    private final DichVuDAO dao = new DichVuDAO();

    public List<DichVu> layTatCa() {
        return dao.layTatCa();
    }

    public boolean them(DichVu dv) {
        return dao.insertDichVu(dv);
    }

    public boolean capNhat(DichVu dv) {
        return dao.updateDichVu(dv);
    }

    public boolean xoa(String maDichVu) {
        return dao.deleteDichVu(maDichVu);
    }
}
