USE ChungCuMini;
GO

-- Xóa bảng nếu đã tồn tại để tránh lỗi
IF OBJECT_ID('dbo.PhuongTien', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.PhuongTien;
END
GO

-- Khởi tạo bảng PhuongTien
CREATE TABLE dbo.PhuongTien (
    bienSo NVARCHAR(20) PRIMARY KEY,
    loaiXe NVARCHAR(50) NOT NULL,
    maKhachHang NVARCHAR(20) NULL,
    maPhong NVARCHAR(20) NULL,
    mucPhi DECIMAL(18,2) NOT NULL,
    CONSTRAINT FK_PhuongTien_KhachHang FOREIGN KEY (maKhachHang) REFERENCES dbo.KhachHang(maKhachHang) ON DELETE SET NULL,
    CONSTRAINT FK_PhuongTien_Phong FOREIGN KEY (maPhong) REFERENCES dbo.Phong(maPhong) ON DELETE SET NULL
);
GO

-- Seed dữ liệu mẫu cho Phương tiện
INSERT INTO dbo.PhuongTien (bienSo, loaiXe, maKhachHang, maPhong, mucPhi)
VALUES 
    ('29A-123.45', N'Ô tô', NULL, NULL, 1200000),
    ('51G-999.99', N'Xe máy', NULL, NULL, 150000);
GO
