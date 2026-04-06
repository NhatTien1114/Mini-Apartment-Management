Use ChungCuMini;
-- Tạo bảng LoaiPhong để quản lý loại phòng động
CREATE TABLE LoaiPhong (
    maLoaiPhong INT IDENTITY(0, 1) PRIMARY KEY,
    tenLoaiPhong NVARCHAR(100) NOT NULL
);

-- Seed dữ liệu cũ tương ứng với enum ordinal
SET IDENTITY_INSERT LoaiPhong ON;
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong) VALUES (0, N'Phòng Đơn');
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong) VALUES (1, N'Phòng Đôi');
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong) VALUES (2, N'Phòng Studio');
SET IDENTITY_INSERT LoaiPhong OFF;
