-- Chay trong SSMS bang tai khoan co quyen CREATE DATABASE.
-- Script khong xoa database hay du lieu cu.
USE master;
GO
IF DB_ID(N'Muc4_Restful_Ajax') IS NULL
BEGIN
    CREATE DATABASE [Muc4_Restful_Ajax];
END;
GO
USE [Muc4_Restful_Ajax];
GO
-- Hibernate tao bang khi khoi dong voi profile sqlserver.
-- Du lieu mau chi duoc them khi ca hai bang rong.
