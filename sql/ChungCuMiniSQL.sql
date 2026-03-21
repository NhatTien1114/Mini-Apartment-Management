/* =========================================================
   DATABASE: ChungCuMini (SQL Server) - FIX CASCADE PATHS
   ========================================================= */

IF DB_ID(N'ChungCuMini') IS NULL
BEGIN
    CREATE DATABASE ChungCuMini;
END
GO

USE ChungCuMini;
GO

/* 1) TaiKhoan: role 0=Owner, 1=Admin */
CREATE TABLE dbo.TaiKhoan (
    maTaiKhoan     NVARCHAR(20)  NOT NULL,
    tenDangNhap    NVARCHAR(50)  NOT NULL,
    matKhau        NVARCHAR(255) NOT NULL,
    hoTen          NVARCHAR(100) NULL,
    soDienThoai    NVARCHAR(20)  NULL,
    ngaySinh       DATETIME      NULL,
    diaChi         NVARCHAR(255) NULL,
    role           TINYINT       NOT NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (maTaiKhoan),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap),
    CONSTRAINT CK_TaiKhoan_Role CHECK (role IN (0,1))
);
GO

/* 2) Toa */
CREATE TABLE dbo.Toa (
    maToa    NVARCHAR(20)  NOT NULL,
    tenToa   NVARCHAR(100) NOT NULL,
    chuSoHuu NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_Toa PRIMARY KEY (maToa),
    CONSTRAINT FK_Toa_TaiKhoan FOREIGN KEY (chuSoHuu)
        REFERENCES dbo.TaiKhoan(maTaiKhoan)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
GO

/* 3) Tang */
CREATE TABLE dbo.Tang (
    maTang  NVARCHAR(20)  NOT NULL,
    tenTang NVARCHAR(100) NOT NULL,
    maToa   NVARCHAR(20)  NOT NULL,
    CONSTRAINT PK_Tang PRIMARY KEY (maTang),
    CONSTRAINT FK_Tang_Toa FOREIGN KEY (maToa)
        REFERENCES dbo.Toa(maToa)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
GO

/* 4) Phong: trangThaiPhong 0..3 */
CREATE TABLE dbo.Phong (
    maPhong         NVARCHAR(20)  NOT NULL,
    maTang          NVARCHAR(20)  NOT NULL,
    tenPhong        NVARCHAR(100) NOT NULL,
    dienTich        FLOAT         NULL,
    loaiPhong       TINYINT       NOT NULL,
    trangThaiPhong  TINYINT       NOT NULL,
    soNguoiHienTai  INT           NOT NULL CONSTRAINT DF_Phong_SoNguoi DEFAULT 0,

    CONSTRAINT PK_Phong PRIMARY KEY (maPhong),
    CONSTRAINT FK_Phong_Tang FOREIGN KEY (maTang)
        REFERENCES dbo.Tang(maTang)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_Phong_TrangThaiPhong CHECK (trangThaiPhong IN (0,1,2,3)),
    CONSTRAINT CK_Phong_LoaiPhong CHECK (loaiPhong >= 0 AND loaiPhong <= 20)
);
GO

/* 5) KhachHang */
CREATE TABLE dbo.KhachHang (
    maKhachHang     NVARCHAR(20)  NOT NULL,
    hoTen           NVARCHAR(100) NOT NULL,
    soDienThoai     NVARCHAR(20)  NULL,
    ngaySinh        DATETIME      NULL,
    soCCCD          NVARCHAR(20)  NULL,
    diaChiThuongTru NVARCHAR(255) NULL,
    CONSTRAINT PK_KhachHang PRIMARY KEY (maKhachHang),
    CONSTRAINT UQ_KhachHang_CCCD UNIQUE (soCCCD)
);
GO

/* 6) HopDong: trangThai 0=ConHan, 1=HetHan */
CREATE TABLE dbo.HopDong (
    maHopDong      NVARCHAR(20) NOT NULL,
    maPhong        NVARCHAR(20) NOT NULL,
    ngayBatDau     DATETIME     NOT NULL,
    ngayKetThuc    DATETIME     NULL,
    tienCoc        FLOAT        NOT NULL CONSTRAINT DF_HopDong_TienCoc DEFAULT 0,
    tienThueThang  FLOAT        NOT NULL,
    trangThai      TINYINT      NOT NULL,

    CONSTRAINT PK_HopDong PRIMARY KEY (maHopDong),
    CONSTRAINT FK_HopDong_Phong FOREIGN KEY (maPhong)
        REFERENCES dbo.Phong(maPhong)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_HopDong_TrangThai CHECK (trangThai IN (0,1))
);
GO

/* 7) HopDongKhachHang: vaiTro 0=DaiDien, 1=NguoiOCung */
CREATE TABLE dbo.HopDongKhachHang (
    maHDKT       NVARCHAR(20) NOT NULL,
    maHopDong    NVARCHAR(20) NOT NULL,
    maKhachHang  NVARCHAR(20) NOT NULL,
    vaiTro       TINYINT      NOT NULL,

    CONSTRAINT PK_HopDongKhachHang PRIMARY KEY (maHDKT),
    CONSTRAINT UQ_HopDongKhachHang UNIQUE (maHopDong, maKhachHang),

    CONSTRAINT FK_HDHK_HopDong FOREIGN KEY (maHopDong)
        REFERENCES dbo.HopDong(maHopDong)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_HDHK_KhachHang FOREIGN KEY (maKhachHang)
        REFERENCES dbo.KhachHang(maKhachHang)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_HDHK_VaiTro CHECK (vaiTro IN (0,1))
);
GO

/* 8) ChiSoDienNuoc */
CREATE TABLE dbo.ChiSoDienNuoc (
    maPhong  NVARCHAR(20) NOT NULL,
    thang    INT          NOT NULL,
    nam      INT          NOT NULL,
    soDien   INT          NOT NULL,
    soNuoc   INT          NOT NULL,

    CONSTRAINT PK_ChiSoDienNuoc PRIMARY KEY (maPhong, thang, nam),
    CONSTRAINT FK_CSDN_Phong FOREIGN KEY (maPhong)
        REFERENCES dbo.Phong(maPhong)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_CSDN_Thang CHECK (thang BETWEEN 1 AND 12),
    CONSTRAINT CK_CSDN_SoDien CHECK (soDien >= 0),
    CONSTRAINT CK_CSDN_SoNuoc CHECK (soNuoc >= 0)
);
GO

/* 9) DichVu */
CREATE TABLE dbo.DichVu (
    maDichVu   NVARCHAR(20)  NOT NULL,
    tenDichVu  NVARCHAR(100) NOT NULL,
    donVi      NVARCHAR(50)  NULL,
    CONSTRAINT PK_DichVu PRIMARY KEY (maDichVu)
);
GO

/* 10) HoaDon: trangThaiThanhToan 0=Da, 1=Chua */
CREATE TABLE dbo.HoaDon (
    maHoaDon            NVARCHAR(20) NOT NULL,
    maHopDong           NVARCHAR(20) NULL,
    maPhong             NVARCHAR(20) NOT NULL,
    tuNgay              DATETIME     NOT NULL,
    denNgay             DATETIME     NOT NULL,
    trangThaiThanhToan  TINYINT      NOT NULL,
    nguoiLap            NVARCHAR(20) NOT NULL,
    createdAt           DATETIME     NOT NULL CONSTRAINT DF_HoaDon_CreatedAt DEFAULT GETDATE(),

    CONSTRAINT PK_HoaDon PRIMARY KEY (maHoaDon),

    CONSTRAINT FK_HoaDon_HopDong FOREIGN KEY (maHopDong)
        REFERENCES dbo.HopDong(maHopDong)
        ON UPDATE NO ACTION
        ON DELETE SET NULL,

    CONSTRAINT FK_HoaDon_Phong FOREIGN KEY (maPhong)
        REFERENCES dbo.Phong(maPhong)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_HoaDon_TaiKhoan FOREIGN KEY (nguoiLap)
        REFERENCES dbo.TaiKhoan(maTaiKhoan)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_HoaDon_TrangThaiThanhToan CHECK (trangThaiThanhToan IN (0,1)),
    CONSTRAINT CK_HoaDon_Ngay CHECK (tuNgay <= denNgay)
);
GO

/* 11) HoaDonDetail */
CREATE TABLE dbo.HoaDonDetail (
    maChiTiet   NVARCHAR(20)  NOT NULL,
    maHoaDon    NVARCHAR(20)  NOT NULL,
    maDichVu    NVARCHAR(20)  NOT NULL,
    soLuong     INT           NOT NULL,
    tenKhoan    NVARCHAR(100) NULL,
    donGia      FLOAT         NOT NULL CONSTRAINT DF_HDD_DonGia DEFAULT 0,
    thanhTien   AS (CAST(soLuong * donGia AS FLOAT)) PERSISTED,

    CONSTRAINT PK_HoaDonDetail PRIMARY KEY (maChiTiet),

    CONSTRAINT FK_HDD_HoaDon FOREIGN KEY (maHoaDon)
        REFERENCES dbo.HoaDon(maHoaDon)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT FK_HDD_DichVu FOREIGN KEY (maDichVu)
        REFERENCES dbo.DichVu(maDichVu)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT CK_HDD_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_HDD_DonGia CHECK (donGia >= 0)
);
GO

/* 12) GiaHeader: loai 0=Phong, 1=DichVu */
CREATE TABLE dbo.GiaHeader (
    maGiaHeader  NVARCHAR(20)  NOT NULL,
    ngayBatDau   DATETIME      NOT NULL,
    ngayKetThuc  DATETIME      NULL,
    moTa         NVARCHAR(255) NULL,
    trangThai    TINYINT       NOT NULL,  -- chưa chốt enum => để 0..20
    ghiChu       NVARCHAR(255) NULL,
    loai         TINYINT       NOT NULL,

    CONSTRAINT PK_GiaHeader PRIMARY KEY (maGiaHeader),
    CONSTRAINT CK_GiaHeader_Loai CHECK (loai IN (0,1)),
    CONSTRAINT CK_GiaHeader_TrangThai CHECK (trangThai >= 0 AND trangThai <= 20),
    CONSTRAINT CK_GiaHeader_Ngay CHECK (ngayKetThuc IS NULL OR ngayBatDau <= ngayKetThuc)
);
GO

/* 13) GiaDetail */
CREATE TABLE dbo.GiaDetail (
    maGiaDetail  NVARCHAR(20) NOT NULL,
    maGiaHeader  NVARCHAR(20) NOT NULL,
    maLoai       NVARCHAR(50) NOT NULL,
    donGia       FLOAT        NOT NULL,

    CONSTRAINT PK_GiaDetail PRIMARY KEY (maGiaDetail),

    CONSTRAINT FK_GiaDetail_GiaHeader FOREIGN KEY (maGiaHeader)
        REFERENCES dbo.GiaHeader(maGiaHeader)
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,

    CONSTRAINT UQ_GiaDetail UNIQUE (maGiaHeader, maLoai),
    CONSTRAINT CK_GiaDetail_DonGia CHECK (donGia >= 0)
);
GO

/* Index gợi ý */
CREATE INDEX IX_HopDong_maPhong ON dbo.HopDong(maPhong);
CREATE INDEX IX_HoaDon_maPhong ON dbo.HoaDon(maPhong);
CREATE INDEX IX_HoaDon_maHopDong ON dbo.HoaDon(maHopDong);
CREATE INDEX IX_HoaDonDetail_maHoaDon ON dbo.HoaDonDetail(maHoaDon);
GO
