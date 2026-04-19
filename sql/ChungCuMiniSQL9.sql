/* =========================================================
   MIGRATION 9: Thêm cột ngay vào ChiSoDienNuoc
   Mục đích: Cho phép ghi nhiều chỉ số trong cùng 1 tháng
   (khi có người cũ chuyển đi và người mới vào cùng tháng)
   ========================================================= */
USE ChungCuMini;
GO

-- Bước 1: Xoá PRIMARY KEY cũ (maPhong, thang, nam)
ALTER TABLE dbo.ChiSoDienNuoc DROP CONSTRAINT PK_ChiSoDienNuoc;
GO

-- Bước 2: Thêm cột ngay với DEFAULT = 1 (cho dữ liệu cũ)
ALTER TABLE dbo.ChiSoDienNuoc
    ADD ngay INT NOT NULL CONSTRAINT DF_CSDN_Ngay DEFAULT 1;
GO

-- Bước 3: Thêm PRIMARY KEY mới gồm cả ngay
ALTER TABLE dbo.ChiSoDienNuoc
    ADD CONSTRAINT PK_ChiSoDienNuoc PRIMARY KEY (maPhong, thang, nam, ngay);
GO

-- Bước 4: Thêm CHECK constraint cho ngay
ALTER TABLE dbo.ChiSoDienNuoc
    ADD CONSTRAINT CK_CSDN_Ngay CHECK (ngay BETWEEN 1 AND 31);
GO
