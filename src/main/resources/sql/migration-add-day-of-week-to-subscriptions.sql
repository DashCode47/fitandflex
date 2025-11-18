-- ===========================================
-- MIGRATION: Add day_of_week to Class Subscriptions
-- ===========================================
-- Este script agrega el campo day_of_week a la tabla class_subscriptions
-- para poder distinguir suscripciones por día de la semana
-- ===========================================

-- Agregar columna day_of_week
ALTER TABLE class_subscriptions 
ADD COLUMN IF NOT EXISTS day_of_week INTEGER;

-- Actualizar suscripciones existentes:
-- PostgreSQL DOW: 0=Domingo, 1=Lunes, ..., 6=Sábado
-- Necesitamos: 1=Lunes, 2=Martes, ..., 7=Domingo
-- Conversión: Si DOW = 0 (Domingo) → 7, sino DOW
UPDATE class_subscriptions 
SET day_of_week = CASE 
    WHEN date IS NOT NULL THEN 
        CASE WHEN EXTRACT(DOW FROM date) = 0 THEN 7 ELSE EXTRACT(DOW FROM date) END
    ELSE 
        CASE WHEN EXTRACT(DOW FROM created_at) = 0 THEN 7 ELSE EXTRACT(DOW FROM created_at) END
END
WHERE day_of_week IS NULL;

-- Hacer la columna NOT NULL después de actualizar los datos
ALTER TABLE class_subscriptions 
ALTER COLUMN day_of_week SET NOT NULL;

-- Agregar índice para mejorar consultas por día de la semana
CREATE INDEX IF NOT EXISTS idx_subscription_day_of_week ON class_subscriptions(day_of_week);

-- Agregar índice compuesto para consultas por clase, día y horario
CREATE INDEX IF NOT EXISTS idx_subscription_class_day_time ON class_subscriptions(class_id, day_of_week, start_time, end_time);

-- Actualizar constraint único para incluir day_of_week
ALTER TABLE class_subscriptions 
DROP CONSTRAINT IF EXISTS uk_class_subscription_user_class_date_time;

ALTER TABLE class_subscriptions 
ADD CONSTRAINT uk_class_subscription_user_class_day_date_time 
UNIQUE (user_id, class_id, day_of_week, date, start_time, end_time);

-- Comentario en la columna
COMMENT ON COLUMN class_subscriptions.day_of_week IS 'Día de la semana (1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo)';

