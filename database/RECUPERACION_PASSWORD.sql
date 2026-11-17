CREATE TABLE IF NOT EXISTS recuperacion_password (
    TokenId BIGINT AUTO_INCREMENT PRIMARY KEY,
    UsuarioId INT NOT NULL,
    TokenHash CHAR(64) NOT NULL,
    FechaCreacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FechaExpiracion DATETIME NOT NULL,
    FechaUso DATETIME NULL,
    CONSTRAINT fk_recuperacion_usuario
        FOREIGN KEY (UsuarioId)
        REFERENCES usuariosistema(UsuarioId)
        ON DELETE CASCADE,
    UNIQUE KEY uk_recuperacion_token (TokenHash),
    INDEX idx_recuperacion_usuario (UsuarioId),
    INDEX idx_recuperacion_expiracion (FechaExpiracion)
);
