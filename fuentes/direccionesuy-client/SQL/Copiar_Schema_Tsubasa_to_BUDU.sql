CREATE SCHEMA IF NOT EXISTS budu AUTHORIZATION geomatica; 

drop table if exists budu.tipo_vialidad cascade;
CREATE TABLE budu.tipo_vialidad (like public.tipo_vialidad including all);
insert into budu.tipo_vialidad select * from public.tipo_vialidad;

drop table if exists budu.tipo_alias cascade;
CREATE TABLE budu.tipo_alias (like public.tipo_alias including all);
insert into budu.tipo_alias select * from public.tipo_alias;


drop table if exists budu.alias_calle cascade;
CREATE TABLE budu.alias_calle (like public.alias including all);
insert into budu.alias_calle select * from public.alias;
alter table budu.alias_calle drop column origen_alias_id;
alter table budu.alias_calle drop column buscar;
alter table budu.alias_calle add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_departamento cascade;
CREATE TABLE budu.alias_departamento (like public.alias_departamento including all);
insert into budu.alias_departamento select * from public.alias_departamento;
alter table budu.alias_departamento add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_entidad_colectiva cascade;
CREATE TABLE budu.alias_entidad_colectiva (like public.alias_entidad_colectiva including all);
insert into budu.alias_entidad_colectiva select * from public.alias_entidad_colectiva;
alter table budu.alias_entidad_colectiva drop column buscar;
alter table budu.alias_entidad_colectiva drop column busqueda;
alter table budu.alias_entidad_colectiva add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_localidad_geo cascade;
CREATE TABLE budu.alias_localidad_geo (like public.alias_localidad_geo including all);
insert into budu.alias_localidad_geo select * from public.alias_localidad_geo;
alter table budu.alias_localidad_geo drop column buscar;
alter table budu.alias_localidad_geo drop column busqueda;
alter table budu.alias_localidad_geo add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_numero_puerta cascade;
CREATE TABLE budu.alias_numero_puerta (like public.alias_numero_puerta including all);
insert into budu.alias_numero_puerta select * from public.alias_numero_puerta;
alter table budu.alias_numero_puerta drop column origen_alias_id;
alter table budu.alias_numero_puerta add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_tramo_ruta cascade;
CREATE TABLE budu.alias_tramo_ruta (like public.alias_tramo_ruta including all);
insert into budu.alias_tramo_ruta select * from public.alias_tramo_ruta;
alter table budu.alias_tramo_ruta drop column buscar;
alter table budu.alias_tramo_ruta drop column busqueda;
alter table budu.alias_tramo_ruta add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.alias_punto_notable cascade;
CREATE TABLE budu.alias_punto_notable (
	id int4 NOT NULL,
	id_punto_notable int4 NOT NULL,
	nombre varchar NOT NULL,
	fuente_id int4 NOT NULL
);

CREATE TABLE budu.alias_solarp (
	id int4 NOT NULL,
	id_solarp int4 NOT NULL,
	nombre varchar NOT NULL,
	fuente_id int4 NOT NULL
);

CREATE TABLE budu.alias_manzana (
	id int4 NOT NULL,
	id_manzana int4 NOT NULL,
	nombre varchar NOT NULL,
	fuente_id int4 NOT NULL
);


-- Tabla Bloque está vacía.

drop table if exists budu.calle cascade;
CREATE TABLE budu.calle (like public.calle including all);
insert into budu.calle select * from public.calle;
alter table budu.calle drop column tipo_vialidad_id;
alter table budu.calle drop column cod_nombre;
alter table budu.calle drop column cantidad_puntos;
alter table budu.calle drop column geom;
alter table budu.calle drop column origen;
alter table budu.calle add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.calle_departamento cascade;
CREATE TABLE budu.calle_departamento (like public.calle_departamento including all);
insert into budu.calle_departamento select * from public.calle_departamento;

drop table if exists budu.calle_localidad cascade;
CREATE TABLE budu.calle_localidad (like public.calle_localidad including all);
insert into budu.calle_localidad select * from public.calle_localidad;

drop table if exists budu.codigos_postales_vigentes cascade;
CREATE TABLE budu.codigos_postales_vigentes (like public.codigos_postales_vigentes including all);
insert into budu.codigos_postales_vigentes select * from public.codigos_postales_vigentes;
alter table budu.codigos_postales_vigentes add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.departamento cascade;
CREATE TABLE budu.departamento (like public.departamento including all);
insert into budu.departamento select * from public.departamento;
alter table budu.departamento add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.entidad_colectiva cascade;
CREATE TABLE budu.entidad_colectiva (like public.entidad_colectiva including all);
insert into budu.entidad_colectiva select * from public.entidad_colectiva;
alter table budu.entidad_colectiva add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.codigos_clasificacion cascade;
CREATE TABLE budu.codigos_clasificacion (like public.codigos_clasificacion including all);
insert into budu.codigos_clasificacion select * from public.codigos_clasificacion;

drop table if exists budu.localidades_no_oficiales cascade;
CREATE TABLE budu.localidades_no_oficiales (like public.localidades_no_oficiales including all);
insert into budu.localidades_no_oficiales select * from public.localidades_no_oficiales;
alter table budu.localidades_no_oficiales add column fuente_id integer NOT NULL DEFAULT 0 ;

-- Mejora de datos: Creamos una nueva tabla con las intersecciones de las localidades y los departamentos (con snapping para evitar errores)
drop table if exists budu.intersec_localidad_departamento cascade;
create table budu.intersec_localidad_departamento as
	select oldLoc.apis_id as idloc, d.id as iddepto, ST_MakeValid(ST_SNAP(oldLoc.multipolygon, d.geom, 0.01)) as intersec , 
			st_area(st_intersection(d.geom, ST_MakeValid(ST_SNAP(oldLoc.multipolygon, d.geom, 0.01))))/(st_area(oldLoc.multipolygon))*100 as porcentaje
		from public.localidades_no_oficiales oldLoc left join public.departamento d 
		on
		st_intersects (d.geom, oldLoc.multipolygon)
		where  
			st_area(st_intersection(d.geom, ST_MakeValid(ST_SNAP(oldLoc.multipolygon, d.geom, 0.01))))/(st_area(oldLoc.multipolygon))*100 > 2;
			
		
create index on budu.intersec_localidad_departamento (idloc);
create index on budu.intersec_localidad_departamento (iddepto);

-- Fin tabla de intesercciones localidad - departamento

-- Tablas de fuente de información
-- tabla en el esquema nuevo
-- portal pk
-- poi
-- solarp
-- calle
-- ruta
-- tramo ruta
-- departamento
-- localidades no oficiales
-- manzana
-- solar
-- entidad colectiva
-- alias numero de puerta
-- alias poi*
-- alias solarp*
-- alias calle
-- alias tramo ruta
-- alias departamento
-- alias localidades geo
-- alias manzana*
-- alias entidad colectiva

-- * no están en el esquema actual

drop table if exists budu.forma_fuente cascade;
CREATE TABLE budu.forma_fuente
(
  id integer NOT NULL,
  forma varchar(40) NOT NULL,
  descripcion varchar(250),
  CONSTRAINT fuente_pkey PRIMARY KEY (id)
);

INSERT INTO budu.forma_fuente VALUES(1,'Carga individual','La entidad se inserta en la base de datos de forma individual');
INSERT INTO budu.forma_fuente VALUES(2,'Actualización individual','Se actualizan datos de la entidad de forma individual');
INSERT INTO budu.forma_fuente VALUES(3,'Carga masiva','La entidad es insertada en la base de datos en una carga masiva de registros');
INSERT INTO budu.forma_fuente VALUES(4,'Actualización masiva','Se actualizan datos de la entidad de forma masiva');

drop table if exists budu.fuente cascade;
CREATE TABLE budu.fuente
(
  id integer NOT NULL,
  fuente varchar(40) NOT NULL,
  descripcion varchar(250),
  CONSTRAINT forma_fuente_pkey PRIMARY KEY (id)
);

INSERT INTO budu.fuente VALUES(0,'Desconocido',NULL);
INSERT INTO budu.fuente VALUES(1,'ANC','Administración Nacional de Correos');
INSERT INTO budu.fuente VALUES(2,'ANTEL','Administración Nacional de Telecomunicaciones');
INSERT INTO budu.fuente VALUES(3,'BPS','Banco de Previsión Social');
INSERT INTO budu.fuente VALUES(4,'BSE','Banco de Seguros del Estado');
INSERT INTO budu.fuente VALUES(5,'DGI','Dirección General Impositiva');
INSERT INTO budu.fuente VALUES(6,'DNC','Dirección Nacional de Catastro');
INSERT INTO budu.fuente VALUES(7,'IDE','Infraestructura de Datos Espaciales');
INSERT INTO budu.fuente VALUES(8,'IMPUESTO DE PRIMARIA','Impuesto de Primaria');
INSERT INTO budu.fuente VALUES(9,'INE','Instituto Nacional de Estadística');
INSERT INTO budu.fuente VALUES(10,'INTENDENCIA DE ARTIGAS','Intendencia Municipal de Artigas');
INSERT INTO budu.fuente VALUES(11,'INTENDENCIA DE CANELONES','Intendencia Municipal de Canelones');
INSERT INTO budu.fuente VALUES(12,'INTENDENCIA DE CERRO LARGO','Intendencia Municipal de Cerro Largo');
INSERT INTO budu.fuente VALUES(13,'INTENDENCIA DE COLONIA','Intendencia Municipal de Colonia');
INSERT INTO budu.fuente VALUES(14,'INTENDENCIA DE DURAZNO','Intendencia Municipal de Durazno');
INSERT INTO budu.fuente VALUES(15,'INTENDENCIA DE FLORES','Intendencia Municipal de Flores');
INSERT INTO budu.fuente VALUES(16,'INTENDENCIA DE FLORIDA','Intendencia Municipal de Florida');
INSERT INTO budu.fuente VALUES(17,'INTENDENCIA DE LAVALLEJA','Intendencia Municipal de Lavalleja');
INSERT INTO budu.fuente VALUES(18,'INTENDENCIA DE MALDONADO','Intendencia Municipal de Maldonado');
INSERT INTO budu.fuente VALUES(19,'INTENDENCIA DE MONTEVIDEO','Intendencia Municipal de Montevideo');
INSERT INTO budu.fuente VALUES(20,'INTENDENCIA DE PAYSANDÚ','Intendencia Municipal de Paysandú');
INSERT INTO budu.fuente VALUES(21,'INTENDENCIA DE RIO NEGRO','Intendencia Municipal de Rio Negro');
INSERT INTO budu.fuente VALUES(22,'INTENDENCIA DE RIVERA','Intendencia Municipal de Rivera');
INSERT INTO budu.fuente VALUES(23,'INTENDENCIA DE ROCHA','Intendencia Municipal de Rocha');
INSERT INTO budu.fuente VALUES(24,'INTENDENCIA DE SALTO','Intendencia Municipal de Salto');
INSERT INTO budu.fuente VALUES(25,'INTENDENCIA DE SAN JOSE','Intendencia Municipal de San Jose');
INSERT INTO budu.fuente VALUES(26,'INTENDENCIA DE SORIANO','Intendencia Municipal de Soriano');
INSERT INTO budu.fuente VALUES(27,'INTENDENCIA DE TACUAREMBÓ','Intendencia Municipal de Tacuarembó');
INSERT INTO budu.fuente VALUES(28,'INTENDENCIA DE TREINTA Y TRES','Intendencia Municipal de Treinta Y Tres');
INSERT INTO budu.fuente VALUES(29,'MIDES','Ministerio de Desarrollo Social');
INSERT INTO budu.fuente VALUES(30,'MINISTERIO DEL INTERIOR','Ministerio del Interior');
INSERT INTO budu.fuente VALUES(31,'MTOP','Ministerio de Transporte y Obras Publicas');
INSERT INTO budu.fuente VALUES(32,'MTSS','Ministerio de Trabajo y Seguridad Social');
INSERT INTO budu.fuente VALUES(33,'MVOTMA','Ministerio de Vivienda, Ordenamiento Territorial y Medio Ambiente');
INSERT INTO budu.fuente VALUES(34,'OPP','Oficina de Planeamiento y Presupuesto');
INSERT INTO budu.fuente VALUES(35,'OSE','Administración Nacional de las Obras Sanitarias del Estado');
INSERT INTO budu.fuente VALUES(36,'OSM','OpenStreetMap');
INSERT INTO budu.fuente VALUES(37,'UTE','Administración Nacional de Usinas y Trasmisiones Eléctricas');
-- Fin fuentes de información


drop table if exists budu.punto_notable cascade;
CREATE TABLE budu.punto_notable as 
	SELECT id, idcalle, cp, numero, punto, nombre_inmueble, letra
	FROM public.punto
	where nombre_inmueble is not null
	and idcalle is not null;
alter table budu.punto_notable add column km numeric DEFAULT 0 ;
alter table budu.punto_notable add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.manzana cascade;
CREATE TABLE budu.manzana (like public.manzana including all);
insert into budu.manzana select * from public.manzana;
alter table budu.manzana add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.solar cascade;
CREATE TABLE budu.solar (like public.solar including all);
insert into budu.solar select * from public.solar;
alter table budu.solar add column fuente_id integer NOT NULL DEFAULT 0 ;


drop table if exists budu.ruta cascade;
CREATE TABLE budu.ruta (like public.ruta including all);
insert into budu.ruta select * from public.ruta;
alter table budu.ruta add column fuente_id integer NOT NULL DEFAULT 0 ;

drop table if exists budu.sinonimo cascade;
CREATE TABLE budu.sinonimo (like public.sinonimo including all);
insert into budu.sinonimo select * from public.sinonimo;

drop table if exists budu.sinonimo_entidad cascade;
CREATE TABLE budu.sinonimo_entidad (like public.sinonimo_entidad including all);
insert into budu.sinonimo_entidad select * from public.sinonimo_entidad;

drop table if exists budu.sinonimo_localidad cascade;
CREATE TABLE budu.sinonimo_localidad (like public.sinonimo_localidad including all);
insert into budu.sinonimo_localidad select * from public.sinonimo_localidad;

drop table if exists budu.tramo_ruta cascade;
CREATE TABLE budu.tramo_ruta (like public.tramo_ruta including all);
insert into budu.tramo_ruta select * from public.tramo_ruta;
alter table budu.tramo_ruta drop column id_tipo_tramo;
alter table budu.tramo_ruta add column fuente_id integer NOT NULL DEFAULT 0 ;


drop table if exists budu.portal_pk cascade;
CREATE TABLE budu.portal_pk as 
	 select id, idcalle, cp, numero, letra, km, punto, forma_de_origen
		FROM public.punto
		where idcalle is not null;
		-- and nombre_inmueble is null; CAMBIO PARA NO PERDER LOS PORTALES DONDE HAY UN PUNTO NOTABLE
		
alter table budu.portal_pk add column fuente_id integer NOT NULL DEFAULT 0 ;
		
--drop table if exists budu.poi cascade;
--CREATE TABLE budu.poi as 
--	 select id, idcalle, cp, numero, letra, km, punto, nombre_inmueble, forma_de_origen
--		FROM public.punto
--		where nombre_inmueble is not null;

drop table if exists budu.solarp cascade;
CREATE TABLE budu.solarp as 
	 select id, idcalle, cp, numero, letra, km, punto, padron, solar_id, forma_de_origen
		FROM public.punto
		where solar_id is not null;
alter table budu.solarp add column fuente_id integer NOT NULL DEFAULT 0 ;
	
CREATE INDEX portal_pk_geom_idx
  ON budu.portal_pk
  USING GIST (punto);
 


-- Asumimos todas estas capas están en 32721. Si estuvieran en otro SRID, habría que añadir el SRID aquí.
drop table if exists budu.capas_poligonales cascade;
CREATE TABLE IF NOT EXISTS budu.capas_poligonales (
	tabla       char(80) CONSTRAINT firstkey PRIMARY KEY,
    descripcion varchar NOT NULL,
    campo_id    char(50) NOT NULL,
	campo_geom    char(50) NOT NULL
);

INSERT INTO budu.capas_poligonales VALUES
    ('codigos_postales_vigentes', 'Códigos postales vigentes', 'apis_id', 'multipolygon');
INSERT INTO budu.capas_poligonales VALUES
    ('departamento', 'Departamentos', 'id', 'geom');
INSERT INTO budu.capas_poligonales VALUES
    ('entidad_colectiva', 'Entidades colectivas', 'id', 'geom');
INSERT INTO budu.capas_poligonales VALUES
    ('localidades_no_oficiales', 'Localidades', 'apis_id', 'multipolygon');
INSERT INTO budu.capas_poligonales VALUES
    ('manzana', 'Manzanas', 'gid', 'multipolygon');
INSERT INTO budu.capas_poligonales VALUES
    ('solar', 'Solares', 'apis_id', 'multipolygon');



--CREATE EXTENSION postgis_topology;
--SELECT topology.CreateTopology('calle_topo', 32721, 1.0, true);
--SELECT topology.AddTopoGeometryColumn('calle', 'budu', 'calle', 'topo_geom', 'LINESTRING');




