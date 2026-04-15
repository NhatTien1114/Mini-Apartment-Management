/* =========================================================
   MIGRATION 8: Thêm dịch vụ ảo DVXE cho phí gửi xe
   ========================================================= */
USE ChungCuMini;
GO

IF NOT EXISTS (SELECT 1 FROM DichVu WHERE maDichVu = 'DVXE')
    INSERT INTO DichVu(maDichVu, tenDichVu, donVi) VALUES ('DVXE', N'Gửi xe', N'xe/tháng');
GO
