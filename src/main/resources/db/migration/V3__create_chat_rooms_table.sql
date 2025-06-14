IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='chat_rooms' AND xtype='U')
CREATE TABLE chat_rooms (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    chat_id VARCHAR(255) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    last_message VARCHAR(MAX),
    unread_count BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Tạo index cho chat_id và các khóa ngoại
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_rooms_chat_id')
    CREATE INDEX idx_chat_rooms_chat_id ON chat_rooms(chat_id);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_rooms_sender')
    CREATE INDEX idx_chat_rooms_sender ON chat_rooms(sender_id);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_rooms_receiver')
    CREATE INDEX idx_chat_rooms_receiver ON chat_rooms(receiver_id);

-- Trigger cập nhật updated_at khi update
IF OBJECT_ID('trg_update_chat_rooms_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER trg_update_chat_rooms_updated_at;
GO
CREATE TRIGGER trg_update_chat_rooms_updated_at
ON chat_rooms
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE chat_rooms
    SET updated_at = GETDATE()
    FROM inserted
    WHERE chat_rooms.id = inserted.id;
END
GO 