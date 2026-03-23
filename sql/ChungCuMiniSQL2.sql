USE ChungCuMini;
GO

/* Drop 2 bảng giá cũ */
IF OBJECT_ID('dbo.GiaDetail', 'U') IS NOT NULL DROP TABLE dbo.GiaDetail;
IF OBJECT_ID('dbo.GiaHeader', 'U') IS NOT NULL DROP TABLE dbo.GiaHeader;
GO

/* =========================================================
   GiaHeader
   loai: 0=Phong, 1=DichVu
   ========================================================= */
CREATE TABLE dbo.GiaHeader (
    maGiaHeader  NVARCHAR(20)  NOT NULL,
    ngayBatDau   DATE          NOT NULL,
    ngayKetThuc  DATE          NULL,
    moTa         NVARCHAR(255) NULL,
    trangThai    TINYINT       NOT NULL,  -- ví dụ: 1=Active, 0=Inactive (bạn tự map)
    loai         TINYINT       NOT NULL,  -- 0=Phong, 1=DichVu
    ghiChu       NVARCHAR(255) NULL,

    CONSTRAINT PK_GiaHeader PRIMARY KEY (maGiaHeader),
    CONSTRAINT CK_GiaHeader_Loai CHECK (loai IN (0,1)),
    CONSTRAINT CK_GiaHeader_TrangThai CHECK (trangThai >= 0 AND trangThai <= 20),
    CONSTRAINT CK_GiaHeader_Ngay CHECK (ngayKetThuc IS NULL OR ngayBatDau <= ngayKetThuc)
);
GO

/* =========================================================
   GiaDetail
   - Nếu header.loai=0 (Phong): dùng loaiPhong (enum)
   - Nếu header.loai=1 (DichVu): dùng maDichVu (FK -> DichVu)
   ========================================================= */
CREATE TABLE dbo.GiaDetail (
    maGiaDetail  NVARCHAR(20) NOT NULL,
    maGiaHeader  NVARCHAR(20) NOT NULL,

    loaiPhong    TINYINT      NULL,        -- chỉ dùng khi GiaHeader.loai=0
    maDichVu     NVARCHAR(20) NULL,        -- chỉ dùng khi GiaHeader.loai=1

    donGia       FLOAT        NOT NULL,

    CONSTRAINT PK_GiaDetail PRIMARY KEY (maGiaDetail),

    CONSTRAINT FK_GiaDetail_GiaHeader FOREIGN KEY (maGiaHeader)
        REFERENCES dbo.GiaHeader(maGiaHeader)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_GiaDetail_DichVu FOREIGN KEY (maDichVu)
        REFERENCES dbo.DichVu(maDichVu)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_GiaDetail_DonGia CHECK (donGia >= 0),

    /* XOR: chỉ được set 1 trong 2 cột */
    CONSTRAINT CK_GiaDetail_Xor CHECK (
        (loaiPhong IS NOT NULL AND maDichVu IS NULL)
        OR
        (loaiPhong IS NULL AND maDichVu IS NOT NULL)
    ),

    /* Nếu bạn chốt enum loaiPhong, đổi thành IN (...) */
    CONSTRAINT CK_GiaDetail_LoaiPhong_Range CHECK (
        loaiPhong IS NULL OR (loaiPhong >= 0 AND loaiPhong <= 20)
    )
);
GO

/* Unique: 1 header không được trùng loại phòng */
CREATE UNIQUE INDEX UX_GiaDetail_Header_LoaiPhong
ON dbo.GiaDetail(maGiaHeader, loaiPhong)
WHERE loaiPhong IS NOT NULL;
GO

/* Unique: 1 header không được trùng dịch vụ */
CREATE UNIQUE INDEX UX_GiaDetail_Header_DichVu
ON dbo.GiaDetail(maGiaHeader, maDichVu)
WHERE maDichVu IS NOT NULL;
GO

/* =========================================================
   Trigger enforce theo GiaHeader.loai
   ========================================================= */
CREATE OR ALTER TRIGGER dbo.TR_GiaDetail_EnforceByHeaderType
ON dbo.GiaDetail
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    /* Header=Phong (0) => phải có loaiPhong, maDichVu NULL
       Header=DichVu (1) => phải có maDichVu, loaiPhong NULL */
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.GiaHeader h ON h.maGiaHeader = i.maGiaHeader
        WHERE
            (h.loai = 0 AND NOT (i.loaiPhong IS NOT NULL AND i.maDichVu IS NULL))
         OR (h.loai = 1 AND NOT (i.maDichVu IS NOT NULL AND i.loaiPhong IS NULL))
    )
    BEGIN
        RAISERROR (N'GiaDetail không khớp loại của GiaHeader (Phong/DichVu).', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
END;
GO