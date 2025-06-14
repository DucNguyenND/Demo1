CREATE TABLE IF NOT EXISTS test123.chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    chat_id VARCHAR(255) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    last_message TEXT,
    unread_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES test123.users(id),
    FOREIGN KEY (receiver_id) REFERENCES test123.users(id)
);

-- Tạo index cho chat_id và các khóa ngoại
CREATE INDEX IF NOT EXISTS idx_chat_rooms_chat_id ON test123.chat_rooms(chat_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_sender ON test123.chat_rooms(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_rooms_receiver ON test123.chat_rooms(receiver_id);

-- Tạo trigger để tự động cập nhật updated_at
CREATE TRIGGER update_chat_rooms_updated_at
    BEFORE UPDATE ON test123.chat_rooms
    FOR EACH ROW
    EXECUTE FUNCTION test123.update_updated_at_column(); 