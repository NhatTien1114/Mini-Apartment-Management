-- 1. Thêm cột maGiaDetail vào bảng Phong
ALTER TABLE dbo.Phong
ADD maGiaDetail NVARCHAR(20); 
GO

-- 2. Tạo khóa ngoại liên kết sang bảng GiaDetail
ALTER TABLE dbo.Phong
ADD CONSTRAINT FK_Phong_GiaDetail 
FOREIGN KEY (maGiaDetail) REFERENCES dbo.GiaDetail(maGiaDetail);
GO