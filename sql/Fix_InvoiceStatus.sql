USE [ChungCuMini];
-- Bước 1: Xóa constraint cũ
ALTER TABLE HoaDon 
DROP CONSTRAINT CK_HoaDon_TrangThaiThanhToan;

-- Bước 2: Thêm lại constraint mới với giá trị 0, 1, và 2
ALTER TABLE HoaDon 
ADD CONSTRAINT CK_HoaDon_TrangThaiThanhToan CHECK (trangThaiThanhToan IN (0, 1, 2));