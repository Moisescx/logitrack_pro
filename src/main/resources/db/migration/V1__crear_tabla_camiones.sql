CREATE TABLE camiones (
                          id SERIAL PRIMARY KEY,
                          patente VARCHAR(20) NOT NULL UNIQUE,
                          modelo VARCHAR(100),
                          capacidad_carga DOUBLE PRECISION,
                          fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);