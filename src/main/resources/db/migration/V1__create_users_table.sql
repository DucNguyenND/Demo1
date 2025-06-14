CREATE TABLE IF NOT EXISTS test123.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    last_login TIMESTAMP,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index cho các cột thường xuyên tìm kiếm
CREATE INDEX IF NOT EXISTS idx_users_username ON test123.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON test123.users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON test123.users(role);
CREATE INDEX IF NOT EXISTS idx_users_phone ON test123.users(phone);
CREATE INDEX IF NOT EXISTS idx_users_last_login ON test123.users(last_login);

-- Tạo trigger để tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION test123.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON test123.users
    FOR EACH ROW
    EXECUTE FUNCTION test123.update_updated_at_column(); 