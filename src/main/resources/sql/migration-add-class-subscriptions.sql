-- ===========================================
-- MIGRATION: Add Class Subscriptions Table
-- ===========================================
-- Este script crea la tabla class_subscriptions para gestionar
-- las reservas/suscripciones de usuarios a clases
-- ===========================================

-- Crear tabla class_subscriptions
CREATE TABLE IF NOT EXISTS class_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    date DATE,
    recurrent BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- Crear índices para mejorar el rendimiento de las consultas
CREATE INDEX IF NOT EXISTS idx_subscription_user ON class_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscription_class ON class_subscriptions(class_id);
CREATE INDEX IF NOT EXISTS idx_subscription_date ON class_subscriptions(date);
CREATE INDEX IF NOT EXISTS idx_subscription_active ON class_subscriptions(active);

-- Crear índice compuesto para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_subscription_user_class ON class_subscriptions(user_id, class_id);
CREATE INDEX IF NOT EXISTS idx_subscription_class_active ON class_subscriptions(class_id, active);

-- Crear la función update_updated_at_column() si no existe
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Crear trigger para actualizar updated_at automáticamente
CREATE TRIGGER update_class_subscriptions_updated_at 
    BEFORE UPDATE ON class_subscriptions 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Comentarios en la tabla y columnas
COMMENT ON TABLE class_subscriptions IS 'Tabla que almacena las suscripciones/reservas de usuarios a clases';
COMMENT ON COLUMN class_subscriptions.user_id IS 'ID del usuario que se suscribe';
COMMENT ON COLUMN class_subscriptions.class_id IS 'ID de la clase a la que se suscribe';
COMMENT ON COLUMN class_subscriptions.start_time IS 'Hora de inicio del rango de horas';
COMMENT ON COLUMN class_subscriptions.end_time IS 'Hora de fin del rango de horas';
COMMENT ON COLUMN class_subscriptions.date IS 'Fecha específica (opcional). Si es NULL, la suscripción es recurrente';
COMMENT ON COLUMN class_subscriptions.recurrent IS 'Indica si la suscripción es recurrente (se repite cada semana)';
COMMENT ON COLUMN class_subscriptions.active IS 'Indica si la suscripción está activa';

