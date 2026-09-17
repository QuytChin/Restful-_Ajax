USE [Muc4_Restful_Ajax];
GO
SELECT * FROM Categories ORDER BY category_id;
SELECT p.product_id,p.product_name,p.unit_price,p.quantity,c.category_name
FROM Products p JOIN Categories c ON p.category_id=c.category_id
ORDER BY p.unit_price ASC,p.product_id ASC;
SELECT COUNT(*) AS SoDanhMuc FROM Categories;
SELECT COUNT(*) AS SoSanPham FROM Products;
