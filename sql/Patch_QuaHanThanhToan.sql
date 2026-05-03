-- === PATCH: Hệ thống phạt thanh toán trễ ===
-- Chạy script này một lần trên database để thêm cột và bảng cấu hình phạt.

-- 1. Thêm cột tienPhat vào bảng HoaDon (nếu chưa có)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'HoaDon' AND COLUMN_NAME = 'tienPhat'
)
BEGIN
    ALTER TABLE HoaDon ADD tienPhat FLOAT NOT NULL DEFAULT 0;
    PRINT 'Đã thêm cột tienPhat vào bảng HoaDon.';
END
ELSE
    PRINT 'Cột tienPhat đã tồn tại, bỏ qua.';

-- 2. Tạo bảng CauHinhPhat (nếu chưa có)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CauHinhPhat'
)
BEGIN
    CREATE TABLE CauHinhPhat (
        id               INT  PRIMARY KEY DEFAULT 1,
        soNgayAnHan      INT  NOT NULL DEFAULT 3,     -- Ngày ân hạn (1-3 ngày)
        mucPhatNgay      FLOAT NOT NULL DEFAULT 0.0005, -- 0.05%/ngày
        ngayHanThanhToan INT  NOT NULL DEFAULT 5      -- Hạn thanh toán: ngày 5 hàng tháng
    );
    INSERT INTO CauHinhPhat (id, soNgayAnHan, mucPhatNgay, ngayHanThanhToan)
    VALUES (1, 3, 0.0005, 5);
    PRINT 'Đã tạo bảng CauHinhPhat với cấu hình mặc định.';
END
ELSE
    PRINT 'Bảng CauHinhPhat đã tồn tại, bỏ qua.';
GO
