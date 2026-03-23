package service;

import dao.GiaHeaderDAO;
import dao.GiaDetailDAO;
import entity.GiaHeader;
import entity.GiaDetail;
import java.util.List;

public class BangGiaService {
    private final GiaHeaderDAO giaHeaderDAO = new GiaHeaderDAO();
    private final GiaDetailDAO giaDetailDAO = new GiaDetailDAO();

    // GiaHeader operations
    public String themGiaHeader(GiaHeader gh) {
        return giaHeaderDAO.them(gh);
    }

    public GiaHeader layGiaHeaderTheoMa(String ma) {
        return giaHeaderDAO.layTheoMa(ma);
    }

    public List<GiaHeader> layTatCaGiaHeader() {
        return giaHeaderDAO.layTatCa();
    }

    public List<GiaHeader> layGiaHeaderTheoLoai(int loai) {
        return giaHeaderDAO.layTheoLoai(loai);
    }

    public String capNhatGiaHeader(GiaHeader gh) {
        return giaHeaderDAO.capNhat(gh);
    }

    public String xoaGiaHeader(String ma) {
        return giaHeaderDAO.xoa(ma);
    }

    // GiaDetail operations
    public String themGiaDetail(GiaDetail gd) {
        return giaDetailDAO.them(gd);
    }

    public GiaDetail layGiaDetailTheoMa(String ma) {
        return giaDetailDAO.layTheoMa(ma);
    }

    public List<GiaDetail> layGiaDetailTheoGiaHeader(String maGiaHeader) {
        return giaDetailDAO.layTheoGiaHeader(maGiaHeader);
    }

    public List<GiaDetail> layTatCaGiaDetail() {
        return giaDetailDAO.layTatCa();
    }

    public String capNhatGiaDetail(GiaDetail gd) {
        return giaDetailDAO.capNhat(gd);
    }

    public String xoaGiaDetail(String ma) {
        return giaDetailDAO.xoa(ma);
    }
}
