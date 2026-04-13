/* =========================================================
   MIGRATION 7: Thêm vaiTro = 2 (Đã rời đi)
   ========================================================= */
USE ChungCuMini;
GO

-- Xóa constraint cũ
ALTER TABLE dbo.HopDongKhachHang DROP CONSTRAINT CK_HDHK_VaiTro;
GO

-- Thêm constraint mới cho phép 0=DaiDien, 1=ThanhVien, 2=DaRoiDi
ALTER TABLE dbo.HopDongKhachHang ADD CONSTRAINT CK_HDHK_VaiTro CHECK (vaiTro IN (0,1,2));
GO
