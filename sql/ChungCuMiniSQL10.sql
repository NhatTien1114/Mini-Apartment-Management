USE ChungCuMini;
GO

-- =============================================
-- CLEAR DATA (đúng thứ tự FK)
-- =============================================
DELETE FROM dbo.HoaDonDetail;
DELETE FROM dbo.HoaDon;
DELETE FROM dbo.ChiSoDienNuoc;
DELETE FROM dbo.PhuongTien;
DELETE FROM dbo.HopDongKhachHang;
DELETE FROM dbo.HopDong;
DELETE FROM dbo.KhachHang;
DELETE FROM dbo.PhongDichVu;
UPDATE dbo.Phong SET maGiaDetail = NULL;
UPDATE dbo.DichVu SET maGiaDetail = NULL;
DELETE FROM dbo.GiaDetail;
DELETE FROM dbo.GiaHeader;
DELETE FROM dbo.Phong;
DELETE FROM dbo.Tang;
DELETE FROM dbo.Toa;
DELETE FROM dbo.TaiKhoan WHERE maTaiKhoan <> 'TK00';
GO

-- =============================================
-- 1. TAI KHOAN
-- =============================================
UPDATE dbo.TaiKhoan SET
    hoTen        = N'Nguyễn Văn An',
    soDienThoai  = '0901234567',
    ngaySinh     = '1975-05-15',
    diaChi       = N'123 Đinh Tiên Hoàng, Quận 1, TP.HCM'
WHERE maTaiKhoan = 'TK00';

INSERT INTO dbo.TaiKhoan (maTaiKhoan, tenDangNhap, matKhau, hoTen, soDienThoai, ngaySinh, diaChi, role) VALUES
('TK01','quanly@gmail.com','*Admin123',N'Trần Thị Bích','0912345678','1990-08-20',N'456 Nguyễn Trãi, Quận 5, TP.HCM',1),
('TK02','staff@gmail.com', '*Admin123',N'Lê Minh Tuấn', '0923456789','1995-03-10',N'789 Lê Lợi, Quận 3, TP.HCM',   1);
GO

-- =============================================
-- 2. TOA (maToa = 'TOA1' theo QuanLyPhongDAO)
--    TANG (maTang = 'T1'..'T6', tenTang = 'Tầng 1'..'Tầng 6')
-- =============================================
INSERT INTO dbo.Toa (maToa, tenToa, chuSoHuu) VALUES
('TOA1', N'Tòa A', 'TK00');

INSERT INTO dbo.Tang (maTang, tenTang, maToa) VALUES
('T1', N'Tầng 1', 'TOA1'),
('T2', N'Tầng 2', 'TOA1'),
('T3', N'Tầng 3', 'TOA1'),
('T4', N'Tầng 4', 'TOA1'),
('T5', N'Tầng 5', 'TOA1'),
('T6', N'Tầng 6', 'TOA1');
GO

-- =============================================
-- 3. GIA HEADER + DICH VU + GIA DETAIL
-- =============================================
INSERT INTO dbo.GiaHeader (maGiaHeader, ngayBatDau, ngayKetThuc, moTa, trangThai, loai, ghiChu) VALUES
('GH_PHONG', '2025-01-01', NULL, N'Bảng giá phòng 2025',  1, 0, N'Áp dụng từ đầu năm 2025'),
('GH_DICHVU','2025-01-01', NULL, N'Bảng giá dịch vụ 2025',1, 1, N'Áp dụng từ đầu năm 2025');

-- GiaDetail phòng (loaiPhong: 0=Đơn, 1=Đôi, 2=Studio)
INSERT INTO dbo.GiaDetail (maGiaDetail, maGiaHeader, loaiPhong, maDichVu, donGia) VALUES
('GD_P0', 'GH_PHONG', 0, NULL, 3500000),
('GD_P1', 'GH_PHONG', 1, NULL, 5500000),
('GD_P2', 'GH_PHONG', 2, NULL, 7500000);

-- DichVu (chỉ rõ tên cột vì bảng có thêm cột maGiaDetail)
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DV00')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DV00', N'Tiền phòng', N'tháng');
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DV01')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DV01', N'Điện',       N'kWh');
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DV02')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DV02', N'Nước',       N'm³');
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DV03')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DV03', N'Wifi',       N'tháng');
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DV04')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DV04', N'Rác',        N'tháng');
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu WHERE maDichVu='DVXE')
    INSERT INTO dbo.DichVu (maDichVu, tenDichVu, donVi) VALUES ('DVXE', N'Gửi xe',    N'xe/tháng');

-- GiaDetail dịch vụ
INSERT INTO dbo.GiaDetail (maGiaDetail, maGiaHeader, loaiPhong, maDichVu, donGia) VALUES
('GD_D01', 'GH_DICHVU', NULL, 'DV01',   3500),
('GD_D02', 'GH_DICHVU', NULL, 'DV02',  15000),
('GD_D03', 'GH_DICHVU', NULL, 'DV03', 150000),
('GD_D04', 'GH_DICHVU', NULL, 'DV04',  30000),
('GD_DXE', 'GH_DICHVU', NULL, 'DVXE', 150000);

UPDATE dbo.DichVu SET maGiaDetail = 'GD_D01' WHERE maDichVu = 'DV01';
UPDATE dbo.DichVu SET maGiaDetail = 'GD_D02' WHERE maDichVu = 'DV02';
UPDATE dbo.DichVu SET maGiaDetail = 'GD_D03' WHERE maDichVu = 'DV03';
UPDATE dbo.DichVu SET maGiaDetail = 'GD_D04' WHERE maDichVu = 'DV04';
UPDATE dbo.DichVu SET maGiaDetail = 'GD_DXE' WHERE maDichVu = 'DVXE';
GO

-- =============================================
-- 4. PHONG (36 phòng = 6 tầng x 6 phòng)
-- Mã: P{tầng}.{số 2 chữ số} — khớp ROOM_PATTERN trong QuanLyPhongDAO
-- maTang = 'T{tầng}' — khớp tangFromRoom(maPhong)
-- Phân bổ: 12 thuê | 20 trống | 2 sửa | 2 cọc
-- Tầng 1-2: Đơn (loai=0) | Tầng 3-4: Đôi (loai=1) | Tầng 5-6: Studio (loai=2)
-- =============================================
INSERT INTO dbo.Phong (maPhong, maTang, tenPhong, dienTich, loaiPhong, trangThaiPhong, soNguoiHienTai, maGiaDetail) VALUES
-- Tầng 1 – Phòng Đơn (3 thuê | 2 trống | 1 sửa)
('P1.01','T1',N'P1.01',25.0,0,1,1,'GD_P0'),
('P1.02','T1',N'P1.02',25.0,0,1,2,'GD_P0'),
('P1.03','T1',N'P1.03',25.0,0,1,1,'GD_P0'),
('P1.04','T1',N'P1.04',25.0,0,0,0,'GD_P0'),
('P1.05','T1',N'P1.05',25.0,0,0,0,'GD_P0'),
('P1.06','T1',N'P1.06',25.0,0,2,0,'GD_P0'),
-- Tầng 2 – Phòng Đơn (2 thuê | 3 trống | 1 cọc)
('P2.01','T2',N'P2.01',25.0,0,1,1,'GD_P0'),
('P2.02','T2',N'P2.02',25.0,0,1,2,'GD_P0'),
('P2.03','T2',N'P2.03',25.0,0,0,0,'GD_P0'),
('P2.04','T2',N'P2.04',25.0,0,0,0,'GD_P0'),
('P2.05','T2',N'P2.05',25.0,0,0,0,'GD_P0'),
('P2.06','T2',N'P2.06',25.0,0,3,0,'GD_P0'),
-- Tầng 3 – Phòng Đôi (2 thuê | 3 trống | 1 sửa)
('P3.01','T3',N'P3.01',40.0,1,1,2,'GD_P1'),
('P3.02','T3',N'P3.02',40.0,1,1,3,'GD_P1'),
('P3.03','T3',N'P3.03',40.0,1,0,0,'GD_P1'),
('P3.04','T3',N'P3.04',40.0,1,0,0,'GD_P1'),
('P3.05','T3',N'P3.05',40.0,1,0,0,'GD_P1'),
('P3.06','T3',N'P3.06',40.0,1,2,0,'GD_P1'),
-- Tầng 4 – Phòng Đôi (2 thuê | 4 trống)
('P4.01','T4',N'P4.01',40.0,1,1,2,'GD_P1'),
('P4.02','T4',N'P4.02',40.0,1,1,2,'GD_P1'),
('P4.03','T4',N'P4.03',40.0,1,0,0,'GD_P1'),
('P4.04','T4',N'P4.04',40.0,1,0,0,'GD_P1'),
('P4.05','T4',N'P4.05',40.0,1,0,0,'GD_P1'),
('P4.06','T4',N'P4.06',40.0,1,0,0,'GD_P1'),
-- Tầng 5 – Studio (2 thuê | 3 trống | 1 cọc)
('P5.01','T5',N'P5.01',55.0,2,1,2,'GD_P2'),
('P5.02','T5',N'P5.02',55.0,2,1,3,'GD_P2'),
('P5.03','T5',N'P5.03',55.0,2,0,0,'GD_P2'),
('P5.04','T5',N'P5.04',55.0,2,0,0,'GD_P2'),
('P5.05','T5',N'P5.05',55.0,2,0,0,'GD_P2'),
('P5.06','T5',N'P5.06',55.0,2,3,0,'GD_P2'),
-- Tầng 6 – Studio (1 thuê | 5 trống)
('P6.01','T6',N'P6.01',55.0,2,1,2,'GD_P2'),
('P6.02','T6',N'P6.02',55.0,2,0,0,'GD_P2'),
('P6.03','T6',N'P6.03',55.0,2,0,0,'GD_P2'),
('P6.04','T6',N'P6.04',55.0,2,0,0,'GD_P2'),
('P6.05','T6',N'P6.05',55.0,2,0,0,'GD_P2'),
('P6.06','T6',N'P6.06',55.0,2,0,0,'GD_P2');
GO

-- =============================================
-- 5. PHONG DICH VU
-- Điện (DV01) và Nước (DV02) là mặc định → KHÔNG cần lưu vào PhongDichVu
-- Chỉ lưu dịch vụ tùy chọn: Wifi, Rác (và Gửi xe cho phòng có xe)
-- =============================================
INSERT INTO dbo.PhongDichVu (maPhong, maDichVu) VALUES
('P1.01','DV03'),('P1.01','DV04'),
('P1.02','DV03'),('P1.02','DV04'),
('P1.03','DV03'),('P1.03','DV04'),('P1.03','DVXE'),
('P2.01','DV03'),('P2.01','DV04'),
('P2.02','DV03'),('P2.02','DV04'),
('P3.01','DV03'),('P3.01','DV04'),
('P3.02','DV03'),('P3.02','DV04'),
('P4.01','DV03'),('P4.01','DV04'),('P4.01','DVXE'),
('P4.02','DV03'),('P4.02','DV04'),
('P5.01','DV03'),('P5.01','DV04'),('P5.01','DVXE'),
('P5.02','DV03'),('P5.02','DV04'),
('P6.01','DV03'),('P6.01','DV04'),('P6.01','DVXE');
GO

-- =============================================
-- 6. KHACH HANG (15 người)
-- =============================================
INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru) VALUES
('KH01',N'Nguyễn Thị Mai',  '0901111111','1995-04-12','079095001234',N'12 Trần Hưng Đạo, Quận 1, TP.HCM'),
('KH02',N'Trần Văn Bình',   '0902222222','1992-07-23','079092002345',N'34 Lý Thường Kiệt, Quận 10, TP.HCM'),
('KH03',N'Lê Thị Hoa',      '0903333333','1998-01-05','079098003456',N'56 Nguyễn Huệ, Quận 1, TP.HCM'),
('KH04',N'Phạm Văn Cường',  '0904444444','1990-09-18','079090004567',N'78 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM'),
('KH05',N'Hoàng Thị Lan',   '0905555555','1997-06-30','079097005678',N'90 Võ Thị Sáu, Quận 3, TP.HCM'),
('KH06',N'Đặng Minh Khoa',  '0906666666','1993-11-14','079093006789',N'102 Cách Mạng Tháng 8, Quận 3, TP.HCM'),
('KH07',N'Vũ Thị Thu',      '0907777777','1996-02-28','079096007890',N'15 Phan Xích Long, Phú Nhuận, TP.HCM'),
('KH08',N'Bùi Văn Hải',     '0908888888','1989-12-01','079089008901',N'23 Hoàng Diệu, Quận 4, TP.HCM'),
('KH09',N'Đinh Thị Ngọc',   '0909999999','1994-08-15','079094009012',N'45 Lê Văn Sỹ, Quận 3, TP.HCM'),
('KH10',N'Ngô Văn Đức',     '0910000000','1991-03-22','079091010123',N'67 Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM'),
('KH11',N'Lý Thị Hương',    '0911000001','1999-10-08','079099011234',N'89 Trường Chinh, Tân Bình, TP.HCM'),
('KH12',N'Tô Văn Phúc',     '0912000002','1988-05-17','079088012345',N'11 Đường 3/2, Quận 10, TP.HCM'),
('KH13',N'Cao Thị Thanh',   '0913000003','2000-07-04','079200013456',N'33 Sư Vạn Hạnh, Quận 10, TP.HCM'),
('KH14',N'Mai Văn Long',     '0914000004','1987-01-19','079087014567',N'55 Tân Kỳ Tân Quý, Tân Phú, TP.HCM'),
('KH15',N'Hồ Thị Kim',      '0915000005','1996-09-25','079096015678',N'77 Lê Đức Thọ, Gò Vấp, TP.HCM');
GO

-- =============================================
-- 7. HOP DONG (12 đang thuê + 2 hết hạn)
-- =============================================
INSERT INTO dbo.HopDong (maHopDong, maPhong, ngayBatDau, ngayKetThuc, tienCoc, tienThueThang, trangThai) VALUES
('HD001','P1.01','2025-01-01','2026-06-30',3500000,3500000,0),
('HD002','P1.02','2025-03-01','2026-08-31',3500000,3500000,0),
('HD003','P1.03','2025-05-01','2026-10-31',3500000,3500000,0),
('HD004','P2.01','2025-02-01','2026-07-31',3500000,3500000,0),
('HD005','P2.02','2025-06-01','2026-11-30',3500000,3500000,0),
('HD006','P3.01','2025-01-15','2026-07-14',5500000,5500000,0),
('HD007','P3.02','2025-04-01','2026-09-30',5500000,5500000,0),
('HD008','P4.01','2025-07-01','2027-06-30',5500000,5500000,0),
('HD009','P4.02','2025-09-01','2026-08-31',5500000,5500000,0),
('HD010','P5.01','2025-02-15','2026-08-14',7500000,7500000,0),
('HD011','P5.02','2025-10-01','2026-09-30',7500000,7500000,0),
('HD012','P6.01','2025-12-01','2026-11-30',7500000,7500000,0),
-- Hết hạn (lịch sử demo)
('HD013','P1.04','2024-01-01','2025-12-31',3500000,3500000,1),
('HD014','P3.03','2024-06-01','2025-11-30',5500000,5500000,1);
GO

-- =============================================
-- 8. HOP DONG KHACH HANG
-- =============================================
INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro) VALUES
('HDKT001','HD001','KH01',0),
('HDKT002','HD002','KH02',0),('HDKT003','HD002','KH03',1),
('HDKT004','HD003','KH04',0),
('HDKT005','HD004','KH05',0),
('HDKT006','HD005','KH06',0),
('HDKT007','HD006','KH07',0),('HDKT008','HD006','KH08',1),
('HDKT009','HD007','KH09',0),('HDKT010','HD007','KH10',1),('HDKT011','HD007','KH11',1),
('HDKT012','HD008','KH12',0),('HDKT013','HD008','KH13',1),
('HDKT014','HD009','KH14',0),
('HDKT015','HD010','KH15',0),('HDKT016','HD010','KH01',1),
('HDKT017','HD011','KH02',0),('HDKT018','HD011','KH03',1),
('HDKT019','HD012','KH04',0),('HDKT020','HD012','KH05',1),
-- Hết hạn
('HDKT021','HD013','KH06',0),
('HDKT022','HD014','KH07',0),('HDKT023','HD014','KH08',2);
GO

-- =============================================
-- 9. CHI SO DIEN NUOC (T2→T4/2026) – WHILE loop
-- =============================================
DECLARE @thang INT = 2;
DECLARE @nam   INT = 2026;
WHILE @thang <= 4
BEGIN
    DECLARE @seed INT = @thang * 7;
    INSERT INTO dbo.ChiSoDienNuoc (maPhong, thang, nam, ngay, soDien, soNuoc)
    SELECT
        p.maPhong, @thang, @nam, 1,
        200 + (p.loaiPhong * 150) + (ABS(CHECKSUM(p.maPhong)) % 80) + @seed,
        15  + (p.loaiPhong * 10)  + (ABS(CHECKSUM(p.maPhong + 'n')) % 10) + (@seed / 5)
    FROM dbo.Phong p
    WHERE p.trangThaiPhong = 1
      AND NOT EXISTS (
          SELECT 1 FROM dbo.ChiSoDienNuoc c
          WHERE c.maPhong = p.maPhong AND c.thang = @thang AND c.nam = @nam AND c.ngay = 1
      );
    SET @thang = @thang + 1;
END;
GO

-- =============================================
-- 10. PHUONG TIEN
-- =============================================
INSERT INTO dbo.PhuongTien (bienSo, loaiXe, maKhachHang, maPhong, mucPhi) VALUES
('51A-111.11',N'Xe máy','KH01','P1.01', 150000),
('51B-222.22',N'Xe máy','KH02','P1.02', 150000),
('51C-333.33',N'Ô tô',  'KH04','P1.03',1200000),
('51D-444.44',N'Xe máy','KH07','P3.01', 150000),
('51E-555.55',N'Ô tô',  'KH12','P4.01',1200000),
('51F-666.66',N'Xe máy','KH12','P4.01', 150000),
('51G-777.77',N'Ô tô',  'KH15','P5.01',1200000),
('51H-888.88',N'Xe máy','KH15','P5.01', 150000),
('51K-999.99',N'Ô tô',  'KH04','P6.01',1200000);
GO

-- =============================================
-- 11. HOA DON + CHI TIET – WHILE loop
-- T10/2025 → T3/2026 : đã thanh toán (trangThai=1)
-- T4/2026            : chưa thanh toán (trangThai=0)
-- =============================================
DECLARE @loopThang INT = 10;
DECLARE @loopNam   INT = 2025;

WHILE (@loopNam < 2026) OR (@loopNam = 2026 AND @loopThang <= 4)
BEGIN
    DECLARE @trangThai  TINYINT      = CASE WHEN @loopNam = 2026 AND @loopThang = 4 THEN 0 ELSE 1 END;
    DECLARE @nguoiLap   NVARCHAR(20) = CASE WHEN @loopNam = 2025 THEN 'TK00' ELSE 'TK01' END;
    DECLARE @ngayDau    DATE         = DATEFROMPARTS(@loopNam, @loopThang, 1);
    DECLARE @ngayCuoi   DATE         = EOMONTH(@ngayDau);
    DECLARE @thangStr   VARCHAR(2)   = RIGHT('0' + CAST(@loopThang AS VARCHAR), 2);
    DECLARE @namStr     VARCHAR(2)   = RIGHT(CAST(@loopNam AS VARCHAR), 2);

    -- HoaDon
    INSERT INTO dbo.HoaDon (maHoaDon, maHopDong, maPhong, tuNgay, denNgay, trangThaiThanhToan, nguoiLap, createdAt)
    SELECT
        'HD_' + p.maPhong + '_' + @thangStr + @namStr,
        hd.maHopDong, p.maPhong, @ngayDau, @ngayCuoi, @trangThai, @nguoiLap,
        DATEADD(DAY, 5, @ngayDau)
    FROM dbo.Phong p
    JOIN dbo.HopDong hd ON hd.maPhong = p.maPhong AND hd.trangThai = 0
    WHERE p.trangThaiPhong = 1
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon x WHERE x.maPhong = p.maPhong AND x.tuNgay = @ngayDau);

    -- Chi tiết: Tiền phòng (DV00)
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_P',
        h.maHoaDon, 'DV00', 1, N'Tiền phòng', gd.donGia
    FROM dbo.HoaDon h
    JOIN dbo.Phong     p  ON p.maPhong     = h.maPhong
    JOIN dbo.GiaDetail gd ON gd.maGiaDetail = p.maGiaDetail
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DV00');

    -- Chi tiết: Điện (DV01)
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_D',
        h.maHoaDon, 'DV01',
        50 + (p.loaiPhong * 45) + (ABS(CHECKSUM(p.maPhong + CAST(@loopThang AS VARCHAR))) % 40),
        N'Tiền điện', 3500
    FROM dbo.HoaDon h
    JOIN dbo.Phong p ON p.maPhong = h.maPhong
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DV01');

    -- Chi tiết: Nước (DV02)
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_N',
        h.maHoaDon, 'DV02',
        4 + (p.loaiPhong * 4) + (ABS(CHECKSUM(p.maPhong + CAST(@loopThang AS VARCHAR) + 'n')) % 5),
        N'Tiền nước', 15000
    FROM dbo.HoaDon h
    JOIN dbo.Phong p ON p.maPhong = h.maPhong
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DV02');

    -- Chi tiết: Wifi (DV03) – chỉ phòng đã đăng ký trong PhongDichVu
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_W',
        h.maHoaDon, 'DV03', 1, N'Wifi', 150000
    FROM dbo.HoaDon h
    JOIN dbo.PhongDichVu pdv ON pdv.maPhong = h.maPhong AND pdv.maDichVu = 'DV03'
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DV03');

    -- Chi tiết: Rác (DV04) – chỉ phòng đã đăng ký trong PhongDichVu
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_R',
        h.maHoaDon, 'DV04', 1, N'Rác', 30000
    FROM dbo.HoaDon h
    JOIN dbo.PhongDichVu pdv ON pdv.maPhong = h.maPhong AND pdv.maDichVu = 'DV04'
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DV04');

    -- Chi tiết: Gửi xe (DVXE) – chỉ phòng đã đăng ký trong PhongDichVu
    INSERT INTO dbo.HoaDonDetail (maChiTiet, maHoaDon, maDichVu, soLuong, tenKhoan, donGia)
    SELECT
        'CT_' + h.maPhong + '_' + @thangStr + @namStr + '_X',
        h.maHoaDon, 'DVXE',
        (SELECT COUNT(*) FROM dbo.PhuongTien WHERE maPhong = h.maPhong),
        N'Gửi xe', 150000
    FROM dbo.HoaDon h
    JOIN dbo.PhongDichVu pdv ON pdv.maPhong = h.maPhong AND pdv.maDichVu = 'DVXE'
    WHERE h.tuNgay = @ngayDau
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDonDetail WHERE maHoaDon = h.maHoaDon AND maDichVu = 'DVXE');

    SET @loopThang = @loopThang + 1;
    IF @loopThang > 12
    BEGIN
        SET @loopThang = 1;
        SET @loopNam   = @loopNam + 1;
    END;
END;
GO

PRINT N'✅ Seeding hoàn tất!';
PRINT N'   1 Tòa (TOA1) | 6 Tầng | 36 Phòng (12 thuê / 20 trống / 2 sửa / 2 cọc)';
PRINT N'   15 Khách hàng | 12 HĐ đang thuê + 2 HĐ hết hạn';
PRINT N'   PhongDichVu: Wifi+Rác cho tất cả phòng đang thuê, Gửi xe cho 4 phòng';
PRINT N'   HoaDon: T10/2025 → T4/2026 | ChiSo: T2→T4/2026';
GO