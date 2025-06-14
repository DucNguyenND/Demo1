IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    last_login DATETIME,
    enabled BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Tạo index cho các cột thường xuyên tìm kiếm
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_username')
    CREATE INDEX idx_users_username ON users(username);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_email')
    CREATE INDEX idx_users_email ON users(email);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_role')
    CREATE INDEX idx_users_role ON users(role);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_phone')
    CREATE INDEX idx_users_phone ON users(phone);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_users_last_login')
    CREATE INDEX idx_users_last_login ON users(last_login);

-- Trigger cập nhật updated_at khi update
IF OBJECT_ID('trg_update_users_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER trg_update_users_updated_at;
GO
CREATE TRIGGER trg_update_users_updated_at
ON users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE users
    SET updated_at = GETDATE()
    FROM inserted
    WHERE users.id = inserted.id;
END
GO 