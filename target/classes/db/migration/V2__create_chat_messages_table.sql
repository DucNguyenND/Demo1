IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='chat_messages' AND xtype='U')
CREATE TABLE chat_messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(MAX) NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT GETDATE(),
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);

-- Tạo index cho sender và receiver
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_messages_sender')
    CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_messages_receiver')
    CREATE INDEX idx_chat_messages_receiver ON chat_messages(receiver_id);
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_chat_messages_timestamp')
    CREATE INDEX idx_chat_messages_timestamp ON chat_messages(timestamp);

-- Trigger cập nhật updated_at khi update
IF OBJECT_ID('trg_update_chat_messages_updated_at', 'TR') IS NOT NULL
    DROP TRIGGER trg_update_chat_messages_updated_at;
GO
CREATE TRIGGER trg_update_chat_messages_updated_at
ON chat_messages
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE chat_messages
    SET updated_at = GETDATE()
    FROM inserted
    WHERE chat_messages.id = inserted.id;
END
GO 