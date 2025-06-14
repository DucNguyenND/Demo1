CREATE TABLE IF NOT EXISTS test123.chat_messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES test123.users(id),
    FOREIGN KEY (receiver_id) REFERENCES test123.users(id)
);

-- Tạo index cho sender và receiver
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender ON test123.chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_receiver ON test123.chat_messages(receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_timestamp ON test123.chat_messages(timestamp);

-- Tạo trigger để tự động cập nhật updated_at
CREATE TRIGGER update_chat_messages_updated_at
    BEFORE UPDATE ON test123.chat_messages
    FOR EACH ROW
    EXECUTE FUNCTION test123.update_updated_at_column(); 