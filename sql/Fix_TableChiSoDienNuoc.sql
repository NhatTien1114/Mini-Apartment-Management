USE ChungCuMini;
GO

-- =====================================================
-- BƯỚC 1: BACKUP
-- =====================================================
SELECT * INTO ChiSoDienNuoc_Backup FROM ChiSoDienNuoc;

-- =====================================================
-- BƯỚC 2: TẠO BẢNG MỚI
-- =====================================================
DROP TABLE IF EXISTS ChiSoDienNuoc_New;

CREATE TABLE ChiSoDienNuoc_New (
    maChiSo     INT IDENTITY(1,1) PRIMARY KEY,
    maHopDong   NVARCHAR(20) NOT NULL,    -- ⬅ khớp với HopDong.maHopDong
    ngayGhi     DATE NOT NULL,
    soDien      INT NOT NULL DEFAULT 0,
    soNuoc      INT NOT NULL DEFAULT 0,
    
    CONSTRAINT fk_chiso_hopdong_new 
        FOREIGN KEY (maHopDong) REFERENCES HopDong(maHopDong)
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    CONSTRAINT uq_chiso_hopdong_ngay_new 
        UNIQUE (maHopDong, ngayGhi)
);

CREATE INDEX idx_maHopDong_new ON ChiSoDienNuoc_New(maHopDong);
CREATE INDEX idx_ngayGhi_new ON ChiSoDienNuoc_New(ngayGhi);

-- =====================================================
-- BƯỚC 3: MIGRATE DỮ LIỆU
-- =====================================================
INSERT INTO ChiSoDienNuoc_New (maHopDong, ngayGhi, soDien, soNuoc)
SELECT 
    hd.maHopDong,
    DATEFROMPARTS(cs.nam, cs.thang, cs.ngay) AS ngayGhi,
    cs.soDien,
    cs.soNuoc
FROM ChiSoDienNuoc cs
INNER JOIN HopDong hd 
    ON hd.maPhong = cs.maPhong
    AND hd.ngayBatDau <= DATEFROMPARTS(cs.nam, cs.thang, cs.ngay)
    AND (hd.ngayKetThuc IS NULL 
         OR hd.ngayKetThuc >= DATEFROMPARTS(cs.nam, cs.thang, cs.ngay));


-- Xem dữ liệu đã migrate
SELECT 
    n.maChiSo,
    n.maHopDong,
    hd.maPhong,
    n.ngayGhi,
    n.soDien,
    n.soNuoc
FROM ChiSoDienNuoc_New n
JOIN HopDong hd ON hd.maHopDong = n.maHopDong
ORDER BY hd.maPhong, n.ngayGhi;

-- So sánh số dòng
SELECT COUNT(*) AS so_dong_cu FROM ChiSoDienNuoc;
SELECT COUNT(*) AS so_dong_moi FROM ChiSoDienNuoc_New;

-- Kiểm tra dòng nào không migrate được
SELECT cs.*
FROM ChiSoDienNuoc cs
LEFT JOIN HopDong hd 
    ON hd.maPhong = cs.maPhong
    AND hd.ngayBatDau <= DATEFROMPARTS(cs.nam, cs.thang, cs.ngay)
    AND (hd.ngayKetThuc IS NULL 
         OR hd.ngayKetThuc >= DATEFROMPARTS(cs.nam, cs.thang, cs.ngay))
WHERE hd.maHopDong IS NULL;

-- Đổi tên
EXEC sp_rename 'ChiSoDienNuoc', 'ChiSoDienNuoc_Old';
EXEC sp_rename 'ChiSoDienNuoc_New', 'ChiSoDienNuoc';

-- (Tùy chọn) Đổi tên các constraint/index cho gọn
EXEC sp_rename 'fk_chiso_hopdong_new', 'fk_chiso_hopdong';
EXEC sp_rename 'uq_chiso_hopdong_ngay_new', 'uq_chiso_hopdong_ngay';
EXEC sp_rename 'ChiSoDienNuoc.idx_maHopDong_new', 'idx_maHopDong', 'INDEX';
EXEC sp_rename 'ChiSoDienNuoc.idx_ngayGhi_new', 'idx_ngayGhi', 'INDEX';

-- 1. Bảng mới có dữ liệu
SELECT COUNT(*) FROM ChiSoDienNuoc;
