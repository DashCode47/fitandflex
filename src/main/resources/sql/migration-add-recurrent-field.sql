-- ===========================================
-- MIGRATION: Agregar campo recurrent a class_schedule_patterns
-- ===========================================
-- Fecha: 2024-01-15
-- Descripción: Agrega el campo 'recurrent' a la tabla class_schedule_patterns
--              para indicar si un patrón de horario es recurrente (se repite cada semana)

-- Verificar si la tabla existe, si no existe crearla
CREATE TABLE IF NOT EXISTS class_schedule_patterns (
    id BIGSERIAL PRIMARY KEY,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    class_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_pattern_class FOREIGN KEY (class_id) REFERENCES classes(id)
);

-- Crear índices si no existen
CREATE INDEX IF NOT EXISTS idx_schedule_pattern_class ON class_schedule_patterns(class_id);
CREATE INDEX IF NOT EXISTS idx_schedule_pattern_day ON class_schedule_patterns(day_of_week);
CREATE INDEX IF NOT EXISTS idx_schedule_pattern_active ON class_schedule_patterns(active);

-- Agregar el campo recurrent si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'class_schedule_patterns' 
        AND column_name = 'recurrent'
    ) THEN
        ALTER TABLE class_schedule_patterns 
        ADD COLUMN recurrent BOOLEAN NOT NULL DEFAULT FALSE;
        
        -- Crear índice para el campo recurrent
        CREATE INDEX IF NOT EXISTS idx_schedule_pattern_recurrent ON class_schedule_patterns(recurrent);
        
        RAISE NOTICE 'Campo recurrent agregado exitosamente a class_schedule_patterns';
    ELSE
        RAISE NOTICE 'El campo recurrent ya existe en class_schedule_patterns';
    END IF;
END $$;

-- Crear la función update_updated_at_column() si no existe
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Agregar trigger para updated_at si no existe
-- Primero eliminar el trigger si existe para evitar duplicados
DROP TRIGGER IF EXISTS update_class_schedule_patterns_updated_at ON class_schedule_patterns;

CREATE TRIGGER update_class_schedule_patterns_updated_at 
BEFORE UPDATE ON class_schedule_patterns 
FOR EACH ROW 
EXECUTE FUNCTION update_updated_at_column();

