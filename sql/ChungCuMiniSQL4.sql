USE ChungCuMini;
GO

/* Add maGiaDetail column to DichVu table */
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'DichVu' AND COLUMN_NAME = 'maGiaDetail'
)
BEGIN
    ALTER TABLE dbo.DichVu
    ADD maGiaDetail NVARCHAR(20) NULL;
    
    ALTER TABLE dbo.DichVu
    ADD CONSTRAINT FK_DichVu_GiaDetail FOREIGN KEY (maGiaDetail)
        REFERENCES dbo.GiaDetail(maGiaDetail)
        ON UPDATE NO ACTION
        ON DELETE SET NULL;
END
GO

CREATE OR ALTER VIEW dbo.v_DichVu_GiaHienHanh
AS
WITH GiaHienHanh AS (
    SELECT
        gd.maDichVu,
        gd.donGia,
        gd.maGiaDetail
    FROM dbo.GiaDetail AS gd
    INNER JOIN dbo.GiaHeader AS gh
        ON gd.maGiaHeader = gh.maGiaHeader
    WHERE gh.loai = 1
      AND gh.trangThai = 1
)
SELECT
    d.maDichVu,
    d.tenDichVu,
    d.donVi,
    g.donGia AS Gia_Tien_Hien_Hanh,
    g.maGiaDetail
FROM dbo.DichVu AS d
LEFT JOIN GiaHienHanh AS g
    ON d.maDichVu = g.maDichVu;
GO

/* 
  Data Seeding: Khởi tạo tài khoản Chủ/Admin đầu tiên. 
  Tài khoản này được tự động chèn vào database để khởi động dự án.
*/
IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE maTaiKhoan = 'TK00')
BEGIN
    INSERT INTO dbo.TaiKhoan (maTaiKhoan, tenDangNhap, matKhau, hoTen, soDienThoai, ngaySinh, diaChi, role)
    VALUES ('TK00', 'admin@gmail.com', 'Admin@123', N'Administrator', '0123456789', '2000-01-01', N'Hệ Thống Mặc Định', 0);
END
GO