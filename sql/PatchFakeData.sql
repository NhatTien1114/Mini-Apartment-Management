-- ============================================================
-- PATCH: Sửa fake data bị lỗi ngược giá trị trangThai và tenKhoan
-- Chạy file này 1 lần để fix toàn bộ 4 bug hiển thị trên app
-- ============================================================

USE ChungCuMini;
GO

-- -------------------------------------------------------
-- FIX 1: Sửa trangThai trong HopDong (đang bị đảo ngược)
-- Java code: fromInt(1) = DANG_HIEU_LUC, fromInt(0) = DA_KET_THUC
-- Fake data gốc insert ngược: active = 0, expired = 1
-- -------------------------------------------------------

-- Hợp đồng còn hạn (ngayKetThuc >= hôm nay) → trangThai = 1 (Đang hiệu lực)
UPDATE dbo.HopDong
SET trangThai = 1
WHERE ngayKetThuc >= CAST(GETDATE() AS DATE);

-- Hợp đồng hết hạn (ngayKetThuc < hôm nay) → trangThai = 0 (Đã kết thúc)
UPDATE dbo.HopDong
SET trangThai = 0
WHERE ngayKetThuc < CAST(GETDATE() AS DATE);

PRINT 'FIX 1: Đã sửa trangThai HopDong';

-- -------------------------------------------------------
-- FIX 2: Sửa vaiTro trong HopDongKhachHang
-- Các hợp đồng còn hiệu lực: thành viên nên là 0 (đại diện) hoặc 1 (thành viên)
-- Do bug fake data, capNhatHopDongHetHanTuDong() đã set vaiTro=2 cho
-- thành viên của HD013/HD014 - đây là đúng vì 2 hợp đồng đó thực sự đã hết hạn.
-- Chỉ cần đảm bảo các hợp đồng CÒN hạn không có thành viên bị vaiTro=2.
-- -------------------------------------------------------

-- Reset lại đúng vaiTro cho thành viên thuộc hợp đồng CÒN hiệu lực
-- Dựa trên maHDKT gốc từ fake data (HDKT001-HDKT018 là thành viên hợp đồng đang thuê)
UPDATE dbo.HopDongKhachHang
SET vaiTro = 0
WHERE maHDKT IN (
    'HDKT001','HDKT003','HDKT005','HDKT007','HDKT009',
    'HDKT011','HDKT013','HDKT015','HDKT017'
);  -- Người đại diện (vaiTro = 0)

UPDATE dbo.HopDongKhachHang
SET vaiTro = 1
WHERE maHDKT IN (
    'HDKT002','HDKT004','HDKT006','HDKT008','HDKT010',
    'HDKT012','HDKT014','HDKT016','HDKT018','HDKT019'
);  -- Thành viên (vaiTro = 1)

PRINT 'FIX 2: Đã sửa vaiTro HopDongKhachHang';

-- -------------------------------------------------------
-- FIX 3: Sửa trangThaiPhong trong Phong
-- Các phòng đang cho thuê (có hợp đồng hiệu lực) phải là trangThaiPhong = 1
-- -------------------------------------------------------

UPDATE dbo.Phong
SET trangThaiPhong = 1
WHERE maPhong IN (
    SELECT hd.maPhong FROM dbo.HopDong hd
    WHERE hd.trangThai = 1
);

PRINT 'FIX 3: Đã sửa trangThaiPhong cho phòng đang thuê';

-- -------------------------------------------------------
-- FIX 4: Sửa tenKhoan tiền phòng trong HoaDonDetail
-- DAO dùng: LIKE N'Tiền thuê phòng%'
-- Fake data gốc insert: N'Tiền phòng'
-- -------------------------------------------------------

UPDATE dbo.HoaDonDetail
SET tenKhoan = N'Tiền thuê phòng'
WHERE tenKhoan = N'Tiền phòng';

PRINT 'FIX 4: Đã sửa tenKhoan HoaDonDetail (Tiền phòng → Tiền thuê phòng)';

-- -------------------------------------------------------
-- Kiểm tra kết quả
-- -------------------------------------------------------

SELECT 'HopDong' AS Bang,
    SUM(CASE WHEN trangThai = 1 THEN 1 ELSE 0 END) AS DangHieuLuc,
    SUM(CASE WHEN trangThai = 0 THEN 1 ELSE 0 END) AS DaKetThuc
FROM dbo.HopDong;

SELECT 'HoaDonDetail - tienPhong' AS Bang, COUNT(*) AS SoBanGhi
FROM dbo.HoaDonDetail
WHERE tenKhoan LIKE N'Tiền thuê phòng%';

PRINT 'Patch hoàn tất. Hãy khởi động lại ứng dụng để kiểm tra.';

-- ============================================================
-- PATCH 2: Thêm KhachHang và HopDongKhachHang còn thiếu
-- Các phòng P1.03, P2.02, P4.02, P6.01 thiếu người đại diện
-- Các phòng P1.04, P3.03 (hết hạn) thiếu khách hàng → không show "Đã rời đi"
-- ============================================================

-- Thêm KhachHang còn thiếu
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH09')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH09', N'Lê Minh Quân', '0901234567', '1990-03-10', '079090003456', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH10')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH10', N'Phạm Thị Hoa', '0912345678', '1992-07-22', '079090004567', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH11')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH11', N'Nguyễn Văn Đức', '0923456789', '1988-11-05', '079090005678', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH12')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH12', N'Trần Thị Mai', '0934567890', '1995-09-18', '079090006789', N'Hà Nội');

-- Khách hàng cho 2 hợp đồng đã hết hạn (vaiTro=2 sau khi hết hạn → show "Đã rời đi")
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH13')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH13', N'Bùi Văn Long', '0945678901', '1985-04-30', '079090007890', N'Hà Nội');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = 'KH14')
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, ngaySinh, soCCCD, diaChiThuongTru)
    VALUES ('KH14', N'Vũ Thị Hương', '0956789012', '1993-12-15', '079090008901', N'Hà Nội');

PRINT 'Đã thêm KH09-KH14';

-- Thêm HopDongKhachHang cho phòng CÒN hiệu lực đang thiếu đại diện (vaiTro=0)
-- P1.03
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P1.03' AND hdkh.vaiTro = 0
)
BEGIN
    DECLARE @hdP103 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P1.03' ORDER BY ngayBatDau DESC);
    IF @hdP103 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT030', @hdP103, 'KH09', 0);
END

-- P2.02
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P2.02' AND hdkh.vaiTro = 0
)
BEGIN
    DECLARE @hdP202 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P2.02' ORDER BY ngayBatDau DESC);
    IF @hdP202 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT031', @hdP202, 'KH10', 0);
END

-- P4.02
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P4.02' AND hdkh.vaiTro = 0
)
BEGIN
    DECLARE @hdP402 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P4.02' ORDER BY ngayBatDau DESC);
    IF @hdP402 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT032', @hdP402, 'KH11', 0);
END

-- P6.01
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P6.01' AND hdkh.vaiTro = 0
)
BEGIN
    DECLARE @hdP601 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P6.01' ORDER BY ngayBatDau DESC);
    IF @hdP601 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT033', @hdP601, 'KH12', 0);
END

PRINT 'Đã thêm HopDongKhachHang cho phòng còn hiệu lực';

-- Thêm HopDongKhachHang cho hợp đồng ĐÃ HẾT HẠN với vaiTro=2
-- → kiemTraDaRoiDi() sẽ trả về true → KhachHangUI hiển thị "Đã rời đi"

-- P1.04 (đã hết hạn)
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P1.04'
)
BEGIN
    DECLARE @hdP104 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P1.04' ORDER BY ngayBatDau DESC);
    IF @hdP104 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT034', @hdP104, 'KH13', 2);
END

-- P3.03 (đã hết hạn)
IF NOT EXISTS (
    SELECT 1 FROM dbo.HopDongKhachHang hdkh
    JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
    WHERE hd.maPhong = 'P3.03'
)
BEGIN
    DECLARE @hdP303 VARCHAR(50) = (SELECT TOP 1 maHopDong FROM dbo.HopDong WHERE maPhong = 'P3.03' ORDER BY ngayBatDau DESC);
    IF @hdP303 IS NOT NULL
        INSERT INTO dbo.HopDongKhachHang (maHDKT, maHopDong, maKhachHang, vaiTro)
        VALUES ('HDKT035', @hdP303, 'KH14', 2);
END

PRINT 'Đã thêm HopDongKhachHang cho hợp đồng đã hết hạn (vaiTro=2)';

-- Kiểm tra
SELECT hd.maPhong, hd.trangThai,
    k.hoTen AS nguoiDaiDien,
    hdkh.vaiTro
FROM dbo.HopDong hd
LEFT JOIN dbo.HopDongKhachHang hdkh ON hdkh.maHopDong = hd.maHopDong AND hdkh.vaiTro IN (0, 2)
LEFT JOIN dbo.KhachHang k ON hdkh.maKhachHang = k.maKhachHang
ORDER BY hd.maPhong;

PRINT 'PATCH 2 hoàn tất.';

-- ============================================================
-- PATCH 3: Fix vaiTro cho thành viên hợp đồng đã hết hạn
-- KH06 (Đặng Minh Khoa) bị gán nhầm vào hợp đồng đang hiệu lực
-- → kiemTraDaRoiDi() trả về false → hiện "Đang ở" thay vì "Đã rời đi"
-- ============================================================

-- Xóa bản ghi HDKH sai: KH06 bị thêm nhầm vào hợp đồng đang hiệu lực với vaiTro=1
-- (KH06 chỉ nên thuộc hợp đồng đã hết hạn)
DELETE FROM dbo.HopDongKhachHang
WHERE maKhachHang = 'KH06'
  AND vaiTro = 1
  AND maHopDong IN (SELECT maHopDong FROM dbo.HopDong WHERE trangThai = 1);

PRINT 'Đã xóa bản ghi HDKH sai của KH06 trong hợp đồng đang hiệu lực';

-- Cập nhật vaiTro=2 (đã rời đi) cho tất cả thành viên của hợp đồng đã hết hạn
-- mà họ không còn tham gia vào bất kỳ hợp đồng đang hiệu lực nào
UPDATE hdkh
SET hdkh.vaiTro = 2
FROM dbo.HopDongKhachHang hdkh
JOIN dbo.HopDong hd ON hdkh.maHopDong = hd.maHopDong
WHERE hd.trangThai = 0          -- hợp đồng đã kết thúc
  AND hdkh.vaiTro <> 2          -- chưa được đánh dấu rời đi
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.HopDongKhachHang hdkh2
      JOIN dbo.HopDong hd2 ON hdkh2.maHopDong = hd2.maHopDong
      WHERE hdkh2.maKhachHang = hdkh.maKhachHang
        AND hd2.trangThai = 1   -- đang có hợp đồng hiệu lực khác
        AND hdkh2.vaiTro <> 2
  );

PRINT 'Đã update vaiTro=2 cho thành viên hợp đồng hết hạn (không còn HD hiệu lực)';

-- Kiểm tra
SELECT k.maKhachHang, k.hoTen,
    hdkh.vaiTro,
    hd.maPhong,
    hd.trangThai AS trangThaiHD
FROM dbo.KhachHang k
JOIN dbo.HopDongKhachHang hdkh ON hdkh.maKhachHang = k.maKhachHang
JOIN dbo.HopDong hd ON hd.maHopDong = hdkh.maHopDong
ORDER BY k.maKhachHang, hd.trangThai DESC;

PRINT 'PATCH 3 hoàn tất.';
