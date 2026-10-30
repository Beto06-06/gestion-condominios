-- Ejecutar una sola vez para crear la base de datos
CREATE DATABASE IF NOT EXISTS incondo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE incondo;

-- Usuario de la aplicacion
CREATE USER IF NOT EXISTS 'usuario_incondo'@'localhost' IDENTIFIED BY 'la_Clave';
GRANT SELECT, INSERT, UPDATE, DELETE ON incondo.* TO 'usuario_incondo'@'localhost';

CREATE TABLE persona (
  id_persona INT NOT NULL AUTO_INCREMENT,
  cedula VARCHAR(30) NOT NULL,
  nombre_completo VARCHAR(150) NOT NULL,
  telefono VARCHAR(30) NULL,
  correo VARCHAR(150) NULL,
  PRIMARY KEY (id_persona), UNIQUE (cedula),
  CHECK (CHAR_LENGTH(TRIM(cedula)) > 0),
  CHECK (cedula = REPLACE(REPLACE(cedula,'-',''),' ','')),
  CHECK (CHAR_LENGTH(TRIM(nombre_completo)) > 0)
) ENGINE=InnoDB;

CREATE TABLE rol (
  id_rol INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(40) NOT NULL,
  PRIMARY KEY (id_rol), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT, id_persona INT NOT NULL,
  correo VARCHAR(150) NOT NULL, contrasena VARCHAR(255) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE CHECK (activo IN (0,1)),
  PRIMARY KEY (id_usuario), UNIQUE (id_persona), UNIQUE (correo),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
  CHECK (CHAR_LENGTH(TRIM(correo)) > 0), CHECK (CHAR_LENGTH(contrasena) > 0)
) ENGINE=InnoDB;

CREATE TABLE usuario_rol (
  id_usuario_rol INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL, id_rol INT NOT NULL,
  PRIMARY KEY (id_usuario_rol), UNIQUE (id_usuario,id_rol),
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE puesto (
  id_puesto INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(60) NOT NULL,
  PRIMARY KEY (id_puesto), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE turno (
  id_turno INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(60) NOT NULL,
  hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL,
  PRIMARY KEY (id_turno), UNIQUE (nombre), CHECK (hora_inicio <> hora_fin)
) ENGINE=InnoDB;

CREATE TABLE empleado (
  id_empleado INT NOT NULL AUTO_INCREMENT, id_persona INT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE CHECK (activo IN (0,1)),
  PRIMARY KEY (id_empleado), UNIQUE (id_persona),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

CREATE TABLE empleado_puesto (
  id_empleado_puesto INT NOT NULL AUTO_INCREMENT,
  id_empleado INT NOT NULL, id_puesto INT NOT NULL, id_turno INT NOT NULL,
  PRIMARY KEY (id_empleado_puesto), UNIQUE (id_empleado,id_puesto,id_turno),
  FOREIGN KEY (id_empleado) REFERENCES empleado(id_empleado),
  FOREIGN KEY (id_puesto) REFERENCES puesto(id_puesto),
  FOREIGN KEY (id_turno) REFERENCES turno(id_turno)
) ENGINE=InnoDB;

CREATE TABLE torre (
  id_torre INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(60) NOT NULL,
  PRIMARY KEY (id_torre), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE tipo_filial (
  id_tipo_filial INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(80) NOT NULL,
  PRIMARY KEY (id_tipo_filial), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE filial (
  id_filial INT NOT NULL AUTO_INCREMENT, numero VARCHAR(30) NOT NULL,
  id_torre INT NOT NULL, id_tipo_filial INT NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE CHECK (activo IN (0,1)),
  PRIMARY KEY (id_filial), UNIQUE (numero),
  FOREIGN KEY (id_torre) REFERENCES torre(id_torre),
  FOREIGN KEY (id_tipo_filial) REFERENCES tipo_filial(id_tipo_filial),
  CHECK (CHAR_LENGTH(TRIM(numero)) > 0)
) ENGINE=InnoDB;

CREATE TABLE tipo_relacion (
  id_tipo_relacion INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(40) NOT NULL,
  PRIMARY KEY (id_tipo_relacion), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE persona_filial (
  id_persona_filial INT NOT NULL AUTO_INCREMENT,
  id_persona INT NOT NULL, id_filial INT NOT NULL,
  id_tipo_relacion INT NOT NULL, vive_en_filial BOOLEAN NOT NULL DEFAULT FALSE CHECK (vive_en_filial IN (0,1)),
  PRIMARY KEY (id_persona_filial), UNIQUE (id_persona,id_filial),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial),
  FOREIGN KEY (id_tipo_relacion) REFERENCES tipo_relacion(id_tipo_relacion)
) ENGINE=InnoDB;

CREATE TABLE alquiler (
  id_alquiler INT NOT NULL AUTO_INCREMENT, id_filial INT NOT NULL,
  fecha_inicio DATE NOT NULL, fecha_fin DATE NOT NULL,
  fecha_salida DATE NULL, monto_mensual DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (id_alquiler), UNIQUE (id_filial,fecha_inicio),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial),
  CHECK (fecha_fin > fecha_inicio),
  CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_inicio),
  CHECK (monto_mensual > 0)
) ENGINE=InnoDB;

CREATE TABLE alquiler_inquilino (
  id_alquiler_inquilino INT NOT NULL AUTO_INCREMENT,
  id_alquiler INT NOT NULL, id_persona INT NOT NULL,
  PRIMARY KEY (id_alquiler_inquilino), UNIQUE (id_alquiler,id_persona),
  FOREIGN KEY (id_alquiler) REFERENCES alquiler(id_alquiler),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

CREATE TABLE vehiculo (
  id_vehiculo INT NOT NULL AUTO_INCREMENT, placa VARCHAR(20) NOT NULL,
  marca VARCHAR(60) NULL, modelo VARCHAR(60) NULL, color VARCHAR(40) NULL,
  PRIMARY KEY (id_vehiculo), UNIQUE (placa), CHECK (CHAR_LENGTH(TRIM(placa)) > 0)
) ENGINE=InnoDB;

CREATE TABLE vehiculo_filial (
  id_vehiculo_filial INT NOT NULL AUTO_INCREMENT,
  id_vehiculo INT NOT NULL, id_persona INT NOT NULL, id_filial INT NOT NULL,
  PRIMARY KEY (id_vehiculo_filial), UNIQUE (id_vehiculo),
  FOREIGN KEY (id_vehiculo) REFERENCES vehiculo(id_vehiculo),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial)
) ENGINE=InnoDB;

CREATE TABLE visitante (
  id_visitante INT NOT NULL AUTO_INCREMENT, id_persona INT NOT NULL,
  PRIMARY KEY (id_visitante), UNIQUE (id_persona),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
) ENGINE=InnoDB;

CREATE TABLE visita (
  id_visita INT NOT NULL AUTO_INCREMENT, id_visitante INT NOT NULL,
  id_filial INT NOT NULL, id_empleado_puesto INT NOT NULL,
  id_vehiculo INT NULL,
  fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_salida DATETIME NULL,
  PRIMARY KEY (id_visita), UNIQUE (id_visitante,fecha_ingreso),
  FOREIGN KEY (id_visitante) REFERENCES visitante(id_visitante),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial),
  FOREIGN KEY (id_empleado_puesto) REFERENCES empleado_puesto(id_empleado_puesto),
  FOREIGN KEY (id_vehiculo) REFERENCES vehiculo(id_vehiculo),
  CHECK (fecha_salida IS NULL OR fecha_salida >= fecha_ingreso)
) ENGINE=InnoDB;

CREATE TABLE tipo_parqueo (
  id_tipo_parqueo INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_tipo_parqueo), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE parqueo (
  id_parqueo INT NOT NULL AUTO_INCREMENT, codigo VARCHAR(30) NOT NULL,
  id_tipo_parqueo INT NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE CHECK (activo IN (0,1)),
  PRIMARY KEY (id_parqueo), UNIQUE (codigo),
  FOREIGN KEY (id_tipo_parqueo) REFERENCES tipo_parqueo(id_tipo_parqueo)
) ENGINE=InnoDB;

CREATE TABLE parqueo_filial (
  id_parqueo_filial INT NOT NULL AUTO_INCREMENT,
  id_parqueo INT NOT NULL, id_filial INT NOT NULL,
  PRIMARY KEY (id_parqueo_filial), UNIQUE (id_parqueo),
  FOREIGN KEY (id_parqueo) REFERENCES parqueo(id_parqueo),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial)
) ENGINE=InnoDB;

CREATE TABLE visita_parqueo (
  id_visita_parqueo INT NOT NULL AUTO_INCREMENT,
  id_visita INT NOT NULL, id_parqueo INT NOT NULL,
  PRIMARY KEY (id_visita_parqueo), UNIQUE (id_visita),
  FOREIGN KEY (id_visita) REFERENCES visita(id_visita),
  FOREIGN KEY (id_parqueo) REFERENCES parqueo(id_parqueo)
) ENGINE=InnoDB;

CREATE TABLE area_comun (
  id_area_comun INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL,
  descripcion TEXT NOT NULL, capacidad INT NOT NULL,
  hora_apertura TIME NOT NULL, hora_cierre TIME NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE CHECK (activo IN (0,1)),
  PRIMARY KEY (id_area_comun), UNIQUE (nombre), CHECK (capacidad > 0),
  CHECK (hora_cierre > hora_apertura)
) ENGINE=InnoDB;

CREATE TABLE estado_reporte (
  id_estado_reporte INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_estado_reporte), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE reporte_mantenimiento (
  id_reporte_mantenimiento INT NOT NULL AUTO_INCREMENT, id_persona INT NOT NULL,
  id_filial INT NULL, id_area_comun INT NULL,
  descripcion TEXT NOT NULL, ruta_imagen VARCHAR(1024) NULL,
  fecha_reporte DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_estado_reporte INT NOT NULL DEFAULT 1, id_empleado_puesto INT NULL,
  fecha_cierre DATETIME NULL, solucion TEXT NULL,
  PRIMARY KEY (id_reporte_mantenimiento),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
  FOREIGN KEY (id_filial) REFERENCES filial(id_filial),
  FOREIGN KEY (id_area_comun) REFERENCES area_comun(id_area_comun),
  FOREIGN KEY (id_estado_reporte) REFERENCES estado_reporte(id_estado_reporte),
  FOREIGN KEY (id_empleado_puesto) REFERENCES empleado_puesto(id_empleado_puesto),
  CHECK ((id_filial IS NOT NULL AND id_area_comun IS NULL) OR
         (id_filial IS NULL AND id_area_comun IS NOT NULL)),
  CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_reporte)
) ENGINE=InnoDB;

CREATE TABLE estado_reserva (
  id_estado_reserva INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_estado_reserva), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE reserva (
  id_reserva INT NOT NULL AUTO_INCREMENT, id_persona INT NOT NULL,
  id_area_comun INT NOT NULL, fecha DATE NOT NULL,
  hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL,
  id_estado_reserva INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id_reserva),
  FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
  FOREIGN KEY (id_area_comun) REFERENCES area_comun(id_area_comun),
  FOREIGN KEY (id_estado_reserva) REFERENCES estado_reserva(id_estado_reserva),
  CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;

CREATE TABLE prioridad_nota (
  id_prioridad_nota INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(30) NOT NULL,
  PRIMARY KEY (id_prioridad_nota), UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE nota_guarda (
  id_nota_guarda INT NOT NULL AUTO_INCREMENT, id_empleado_puesto INT NOT NULL,
  texto TEXT NOT NULL, id_prioridad_nota INT NOT NULL,
  fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_empleado_atendio INT NULL, fecha_atencion DATETIME NULL,
  PRIMARY KEY (id_nota_guarda),
  FOREIGN KEY (id_empleado_puesto) REFERENCES empleado_puesto(id_empleado_puesto),
  FOREIGN KEY (id_prioridad_nota) REFERENCES prioridad_nota(id_prioridad_nota),
  FOREIGN KEY (id_empleado_atendio) REFERENCES empleado_puesto(id_empleado_puesto),
  CHECK ((id_empleado_atendio IS NULL AND fecha_atencion IS NULL) OR
         (id_empleado_atendio IS NOT NULL AND fecha_atencion IS NOT NULL)),
  CHECK (fecha_atencion IS NULL OR fecha_atencion >= fecha_registro)
) ENGINE=InnoDB;

-- Indices de busqueda
CREATE INDEX ix_persona_nombre ON persona(nombre_completo);

CREATE INDEX ix_alquiler_periodo ON alquiler(id_filial,fecha_inicio,fecha_fin);

CREATE INDEX ix_visita_dentro ON visita(id_visitante,fecha_salida);

CREATE INDEX ix_reserva_horario ON reserva(id_area_comun,fecha,hora_inicio,hora_fin);

-- Catalogos
INSERT INTO rol(nombre) VALUES ('Administrador'),('Guarda'),('Residente');

INSERT INTO puesto(nombre) VALUES ('Seguridad'),('Limpieza'),('Mantenimiento'),('Administracion');

INSERT INTO turno(nombre,hora_inicio,hora_fin) VALUES
 ('Diurno','06:00','14:00'),('Tarde','14:00','22:00'),('Noche','22:00','06:00');

INSERT INTO torre(nombre) VALUES ('Torre A'),('Torre B'),('Torre C');

INSERT INTO tipo_filial(nombre) VALUES
 ('Apartamento 2 habitaciones'),('Apartamento 3 habitaciones'),('Estudio'),('Casa');

INSERT INTO tipo_relacion(nombre) VALUES ('Propietario'),('Residente');

INSERT INTO tipo_parqueo(nombre) VALUES ('Residente'),('Visita');

INSERT INTO estado_reporte(nombre) VALUES ('Pendiente'),('En proceso'),('Finalizado');

INSERT INTO estado_reserva(nombre) VALUES ('Confirmada'),('Cancelada');

INSERT INTO prioridad_nota(nombre) VALUES ('Normal'),('Urgente');

-- Datos de prueba
INSERT INTO persona(cedula,nombre_completo,telefono,correo) VALUES
 ('112340567','Ana Vargas Soto','88881234','ana.vargas@example.com'),
 ('204560789','Luis Herrera Mora','87774521','luis.herrera@example.com'),
 ('109870654','Sofia Chaves Rojas','86549102','sofia.chaves@example.com'),
 ('303210456','Mario Solano Vega','84567788','mario.solano@example.com'),
 ('11110222','Carlos Rojas','87001122','carlos.rojas@example.com'),
 ('203330444','Marta Quesada','87003344','marta.quesada@example.com'),
 ('105550666','Rafael Campos','87005566','rafael.campos@example.com'),
 ('307770888','Gabriela Mena','87007788','gabriela.mena@example.com');

INSERT INTO filial(numero,id_torre,id_tipo_filial) VALUES
 ('A-101',1,1),('A-102',1,1),('A-103',1,2),('B-204',2,2),
 ('B-205',2,3),('C-301',3,4),('C-302',3,4);

INSERT INTO persona_filial(id_persona,id_filial,id_tipo_relacion,vive_en_filial) VALUES
 (1,1,1,TRUE),(2,1,1,FALSE),(1,4,1,FALSE),(3,3,1,TRUE),(2,5,1,FALSE);

INSERT INTO empleado(id_persona,activo) VALUES (5,TRUE),(6,FALSE),(7,TRUE),(8,TRUE);

INSERT INTO empleado_puesto(id_empleado,id_puesto,id_turno) VALUES
 (1,1,1),(2,1,2),(3,1,3),(4,1,1);
