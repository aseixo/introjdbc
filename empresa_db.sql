/* Script para a creación das tabelas para a relación 1:N (Oficina -> Empregado) */

-- Seleccionar a base de datos (asumindo que xa está creada ou faise CREATE DATABASE IF NOT EXISTS empresa_db;)
DROP DATABASE IF EXISTS empresa_db;
CREATE DATABASE empresa_db;
USE empresa_db;

-- -----------------------------------------------------
-- tabela`oficina` (O LADO 'UN')
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS oficina (
  codigo VARCHAR(10) NOT NULL,
  localidade VARCHAR(255) NOT NULL,
  PRIMARY KEY (codigo)
);

-- -----------------------------------------------------
-- tabela`empregado` (O LADO 'MOITOS' - Contén a Chave Foránea)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS empregado (
  dni VARCHAR(9) NOT NULL,
  nome VARCHAR(255) NOT NULL,
  
  -- Columna para a chave foránea (FK)
  oficina_codigo VARCHAR(10), 
  
  PRIMARY KEY (dni),
  
  -- Definición da Chave Foránea (FK)
  CONSTRAINT fk_empregado_oficina
    FOREIGN KEY (oficina_codigo)
    REFERENCES oficina (codigo)
    ON DELETE SET NULL    -- Se se elimina a oficina, a columna de código do empregado queda a NULL
    ON UPDATE CASCADE     -- Se o código da oficina cambia, actualízase a FK en empregado
);

DESCRIBE empregado;
DESCRIBE oficina;
