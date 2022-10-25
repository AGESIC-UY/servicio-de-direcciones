CREATE SCHEMA IF NOT EXISTS historico;

CREATE TABLE historico.calle
(
  id bigint NOT NULL,
  nombre text NOT NULL,
  idlocalidad integer,
  ruta_id integer,
  version integer NOT NULL,
  fechaversion timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  operacion character varying(25),
  CONSTRAINT calle_historico_pkey PRIMARY KEY (id, version),
  CONSTRAINT historico_calle_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_calle_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);

CREATE TABLE historico.tramo
(
  gid bigint NOT NULL,
  idcalle bigint NOT NULL,
  geom geometry(MultiLineString,32721),
  version integer NOT NULL,
  fechaversion timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  operacion character varying(25),
  CONSTRAINT tramo_historico_pkey PRIMARY KEY (gid, version),
  CONSTRAINT historico_calle_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_calle_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);

CREATE TABLE historico.portal_pk
(
  id bigint,
  idcalle bigint,
  cp integer,
  numero integer,
  letra character varying(5),
  km double precision,
  punto geometry,
  version integer NOT NULL,
  fechaversion timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  operacion character varying(25),
  CONSTRAINT portal_pk_historico_pkey PRIMARY KEY (id, version),
  CONSTRAINT historico_portal_pk_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_portal_pk_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);

CREATE TABLE historico.solar
(
  apis_id bigint NOT NULL,
  padron character varying(1024),
  solar character varying(1024),
  manzana character varying(1024),
  loc_cat character varying(1024),
  depto character varying(1024),
  loc_cdp character varying(1024),
  loc_ine_11 character varying(1024),
  multipolygon geometry(MultiPolygon,32721),
  manzana_id integer,
  version integer NOT NULL,
  fechaversion timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  operacion character varying(25),
  CONSTRAINT solar_pkey PRIMARY KEY (apis_id, version),
  CONSTRAINT historico_solar_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_solar_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);


CREATE TABLE historico.solarp
(
  id bigint,
  idcalle integer,
  cp integer,
  numero integer,
  letra character varying(5),
  km double precision,
  punto geometry,
  padron integer,
  solar_id integer,
  version integer NOT NULL,
  fechaversion timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  operacion character varying(25),
  CONSTRAINT solarp_pkey PRIMARY KEY (id, version),
  CONSTRAINT historico_solarp_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_solarp_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);

CREATE TABLE historico.tipoalias
(
  id integer,
  tipo character varying(25),
  CONSTRAINT tipoalias_pkey PRIMARY KEY (id)
);

INSERT INTO historico.tipoalias VALUES (0, 'CALLE');
INSERT INTO historico.tipoalias VALUES (1, 'DEPARTAMENTO');
INSERT INTO historico.tipoalias VALUES (2, 'ENTIDAD COLECTIVA');
INSERT INTO historico.tipoalias VALUES (3, 'LOCALIDAD GEO');
INSERT INTO historico.tipoalias VALUES (4, 'MANZANA');
INSERT INTO historico.tipoalias VALUES (5, 'NUMERO PUERTA');
INSERT INTO historico.tipoalias VALUES (6, 'PUNTO NOTABLE');
INSERT INTO historico.tipoalias VALUES (7, 'SOLARP');
INSERT INTO historico.tipoalias VALUES (8, 'TRAMO RUTA');

CREATE TABLE historico.alias
(
	id serial NOT NULL ,
	tipo integer, 
	id_type integer,
	nombre text,
	tipo_alias_id integer,
	publicable boolean,
	operacion character varying(25),
	fecha timestamp with time zone,
	fuente integer,
	forma_fuente integer,
	usr character varying(50),
	CONSTRAINT typealias_pkey PRIMARY KEY (id),
	CONSTRAINT historico_alias_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id),
	CONSTRAINT historico_alias_tipoalias FOREIGN KEY (tipo)
      REFERENCES historico.tipoalias (id)
);

CREATE TABLE historico.ruta
(
  id integer NOT NULL,
  numero character varying,
  nombre character varying,
  longitud character varying(50),
  origen character varying(50),
  destino character varying(50),
  tipo character varying,
  tipo_anexo character varying,
  tipo_obs character varying,
  geom geometry(MultiLineString,32721),
  operacion character varying(25),
  version integer NOT NULL,
  fecha timestamp with time zone NOT NULL,
  fuente integer,
  forma_fuente integer,
  usr character varying(50),
  CONSTRAINT rutaid_pkey PRIMARY KEY (id, version),
  CONSTRAINT historico_ruta_forma_fuente FOREIGN KEY (forma_fuente)
      REFERENCES budu.forma_fuente (id),
  CONSTRAINT historico_ruta_fuente FOREIGN KEY (fuente)
      REFERENCES budu.fuente (id)
);

CREATE TABLE historico.punto_notable (
	id int4 NOT NULL,
	idcalle int4 NULL,
	cp int4 NULL,
	numero int4 NULL,
	punto geometry NULL,
	nombre_inmueble varchar NULL,
	letra varchar(5) NULL,
	km numeric NULL,
	operacion varchar(25) NULL,
	"version" int4 NOT NULL,
	fecha timestamptz NOT NULL,
	fuente int4 NULL,
	forma_fuente int4 NULL,
	usr varchar(50) NULL,
	CONSTRAINT poi_pkey PRIMARY KEY (id, version),
	CONSTRAINT historico_poi_forma_fuente FOREIGN KEY (forma_fuente) REFERENCES budu.forma_fuente(id),
	CONSTRAINT historico_poi_fuente FOREIGN KEY (fuente) REFERENCES budu.fuente(id)
);

-- PROCEDIMIENTOS ESQUEMA HISTÓRICO

CREATE OR REPLACE FUNCTION historico.log_portal_pk(idportal integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	pkversion integer;
	p budu.portal_pk%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.portal_pk WHERE id = idportal;
		IF nrows = 0 THEN
			SELECT * INTO p FROM budu.portal_pk WHERE id = idportal;
			INSERT INTO historico.portal_pk VALUES(idportal, p.idcalle, p.cp, p.numero, p.letra, p.km, p.punto, 1, now(), src, src_type, usr, op);
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO pkversion FROM historico.portal_pk WHERE id = idportal;
		IF pkversion IS NULL THEN
			pkversion := 0;
		END IF;
		pkversion := pkversion + 1;
		SELECT * INTO p FROM budu.portal_pk WHERE id = idportal;
		INSERT INTO historico.portal_pk VALUES(idportal, p.idcalle, p.cp, p.numero, p.letra, p.km, p.punto, pkversion, now(), src, src_type, usr, op);
		return pkversion;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
 
CREATE OR REPLACE FUNCTION historico.log_calle(idcalle integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	versioncalle integer;
	p budu.calle%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.calle WHERE id = idcalle;
		IF nrows = 0 THEN
			SELECT * INTO p FROM budu.calle WHERE id = idcalle;
			INSERT INTO historico.calle VALUES(idcalle, p.nombre, p.idlocalidad, p.ruta_id, 1, now(), src, src_type, usr, op);
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO versioncalle FROM historico.calle WHERE id = idcalle;
		IF versioncalle IS NULL THEN
			versioncalle := 0;
		END IF;
		versioncalle := versioncalle + 1;
		SELECT * INTO p FROM budu.calle WHERE id = idcalle;
		INSERT INTO historico.calle VALUES(idcalle, p.nombre, p.idlocalidad, p.ruta_id, versioncalle, now(), src, src_type, usr, op);
		return versioncalle;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  

CREATE OR REPLACE FUNCTION historico.log_tramo(idc integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	versiontramo integer;
	p budu.tramo%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.tramo WHERE idcalle = idc;
		IF nrows = 0 THEN
			FOR p IN SELECT * FROM budu.tramo WHERE idcalle = idc LOOP
				INSERT INTO historico.tramo VALUES(p.gid, idc, p.geom, 1, now(), src, src_type, usr, op);
			END LOOP;
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO versiontramo FROM historico.tramo WHERE idcalle = idc;
		IF versiontramo IS NULL THEN
			versiontramo := 0;
		END IF;
		versiontramo := versiontramo + 1;
		FOR p IN SELECT * FROM budu.tramo WHERE idcalle = idc LOOP
			INSERT INTO historico.tramo VALUES(p.gid, idc, p.geom, versiontramo, now(), src, src_type, usr, op);
		END LOOP;
		return versiontramo;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
  
CREATE OR REPLACE FUNCTION historico.log_solar(idsolar integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	versionsolar integer;
	p budu.solar%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.solar WHERE apis_id = idsolar;
		IF nrows = 0 THEN
			SELECT * INTO p FROM budu.solar WHERE apis_id = idsolar;
			INSERT INTO historico.solar VALUES(idsolar, p.padron, p.solar, p.manzana, p.loc_cat, p.depto, p.loc_cdp, p.loc_ine_11, p.multipolygon, p.manzana_id, 1, now(), NULL, NULL, NULL, 'FIRST');
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO versionsolar FROM historico.solar WHERE apis_id = idsolar;
		IF versionsolar IS NULL THEN
			versionsolar := 0;
		END IF;
		versionsolar := versionsolar + 1;
		SELECT * INTO p FROM budu.solar WHERE apis_id = idsolar;
		INSERT INTO historico.solar VALUES(idsolar, p.padron, p.solar, p.manzana, p.loc_cat, p.depto, p.loc_cdp, p.loc_ine_11, p.multipolygon, p.manzana_id, versionsolar, now(), src, src_type, usr, op);
		return versionsolar;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
  
  
CREATE OR REPLACE FUNCTION historico.log_solarp(idsolar integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	versionsolar integer;
	p budu.solarp%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.solarp WHERE solar_id = idsolar;
		IF nrows = 0 THEN
			FOR p IN SELECT * FROM budu.solarp WHERE solar_id = idsolar LOOP
				RAISE NOTICE 'ok %s', p.id;
				INSERT INTO historico.solarp VALUES(p.id, p.numero, p.cp, p.numero, p.letra, p.km, p.punto, p.padron, idsolar, 1, now(), NULL, NULL, NULL, 'FIRST');
			END LOOP;
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO versionsolar FROM historico.solarp WHERE solar_id = idsolar;
		IF versionsolar IS NULL THEN
			versionsolar := 0;
		END IF;
		versionsolar := versionsolar + 1;
		FOR p IN SELECT * FROM budu.solarp WHERE solar_id = idsolar LOOP
			INSERT INTO historico.solarp VALUES(p.id, p.idcalle, p.cp, p.numero, p.letra, p.km, p.punto, p.padron, idsolar, versionsolar, now(), src, src_type, usr, op);
		END LOOP;
		return versionsolar;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
  
  
  
CREATE OR REPLACE FUNCTION historico.log_ruta(idruta integer, src integer, src_type integer, usr varchar, op varchar)
  RETURNS integer AS
$BODY$
DECLARE
	versionruta integer;
	p budu.ruta%ROWTYPE;
	nrows integer;
BEGIN
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.ruta WHERE id = idruta;
		IF nrows = 0 THEN
			SELECT * INTO p FROM budu.ruta WHERE id = idruta;
			INSERT INTO historico.ruta VALUES(idruta, p.numero, p.nombre, p.longitud, p.origen, p.destino, p.tipo, p.tipo_anexo, p.tipo_obs, p.geom, 'FIRST', 1, now(), NULL, NULL, NULL);
			return 1;
		END IF;
	ELSE 
		SELECT MAX(version) INTO versionruta FROM historico.ruta WHERE id = idruta;
		IF versionruta IS NULL THEN
			versionruta := 0;
		END IF;
		versionruta := versionruta + 1;
		SELECT * INTO p FROM budu.ruta WHERE id = idruta;
		INSERT INTO historico.ruta VALUES(idruta, p.numero, p.nombre, p.longitud, p.origen, p.destino, p.tipo, p.tipo_anexo, p.tipo_obs, p.geom, op, versionruta, now(), src, src_type, usr);
		return versionruta;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

CREATE OR REPLACE FUNCTION historico.log_punto_notable(idpoi integer, src integer, src_type integer, usr character varying, op character varying)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
	pkversion integer;
	p budu.punto_notable%ROWTYPE;
	nrows integer;
BEGIN
	--Antes de hacer un update llamamos esta operación pq si no hay ninguna versión del registro lo machacamos y ya no se
	--se puede recuperar
	IF op = 'FIRST' THEN
		SELECT count(*) INTO nrows FROM historico.punto_notable WHERE id = idpoi;
		IF nrows = 0 THEN
			SELECT * INTO p FROM budu.punto_notable WHERE id = idpoi;
			INSERT INTO historico.punto_notable VALUES(idpoi, p.idcalle, p.cp, p.numero, p.punto, p.nombre_inmueble, p.letra, p.km, op, 1, now(), src, src_type, usr);
			return 1;
		END IF;
	ELSE
		SELECT MAX(version) INTO pkversion FROM historico.punto_notable WHERE id = idpoi;
		IF pkversion IS NULL THEN
			pkversion := 0;
		END IF;
		pkversion := pkversion + 1;
		SELECT * INTO p FROM budu.punto_notable WHERE id = idpoi;
		RAISE NOTICE '-->%', p.idcalle;
		INSERT INTO historico.punto_notable VALUES(idpoi, p.idcalle, p.cp, p.numero, p.punto, p.nombre_inmueble, p.letra, p.km, op, pkversion, now(), src, src_type, usr);
		return pkversion;
	END IF;
	return 0;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



CREATE OR REPLACE FUNCTION historico.log_alias()
  RETURNS trigger AS
$BODY$
DECLARE
	forma_fuente INTEGER;
BEGIN
  IF (TG_OP = 'INSERT') THEN
    forma_fuente = 1;
  END IF;
  IF (TG_OP = 'UPDATE') THEN
	  forma_fuente = 2;
  END IF;
	
	
  IF (TG_OP = 'DELETE' OR TG_OP = 'UPDATE') THEN
	IF TG_ARGV[0]::INTEGER = 0 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.idcalle, OLD.nombre, OLD.tipo_alias_id, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 1 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.departamento_id, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 2 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.entidad_colectiva_id, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 3 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.localidad_id, OLD.nombre, OLD.tipo_alias_id, OLD.publicable, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 4 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.id_manzana, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 5 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.idpunto, OLD.numero::VARCHAR, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 6 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.id_punto_notable, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente1, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 7 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.id_solarp, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
	ELSIF TG_ARGV[0]::INTEGER = 8 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, OLD.tramo_ruta_id, OLD.nombre, NULL, NULL, TG_OP, now(), OLD.fuente_id, forma_fuente, USER);
	END IF;
  END IF;
  
  IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
  	IF TG_ARGV[0]::INTEGER = 0 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.idcalle, NEW.nombre, NEW.tipo_alias_id, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 1 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.departamento_id, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 2 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.entidad_colectiva_id, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 3 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.localidad_id, NEW.nombre, NEW.tipo_alias_id, NEW.publicable, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 4 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.id_manzana, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 5 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.idpunto, NEW.numero::VARCHAR, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 6 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.id_punto_notable, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente1, USER);
  	ELSIF TG_ARGV[0]::INTEGER = 7 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.id_solarp, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
	ELSIF TG_ARGV[0]::INTEGER = 8 THEN
	  	INSERT INTO historico.alias VALUES(DEFAULT, TG_ARGV[0]::INTEGER, NEW.tramo_ruta_id, NEW.nombre, NULL, NULL, TG_OP, now(), NEW.fuente_id, forma_fuente, USER);
	END IF;
	--RAISE NOTICE 'INSERT % % %', TG_ARGV[0], TG_ARGV[1], TG_TABLE_NAME;
  END IF;
  RETURN NULL;
END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  

 CREATE OR REPLACE FUNCTION budu.calc_cp() RETURNS trigger AS
	$BODY$
		BEGIN

		IF (((TG_OP='UPDATE') AND NOT EQUALS(NEW.punto, old.punto)) OR TG_OP='INSERT') THEN
			NEW.cp = COALESCE(CAST((select c.codigo from budu.codigos_clasificacion c where st_isvalid(c.multipolygon) and st_intersects(NEW.punto, c.multipolygon) limit 1) as integer), 0);
		END IF;		
		RETURN NEW;
	END;
	$BODY$
	  LANGUAGE plpgsql VOLATILE
	  COST 100;
 
 
 -- Cortar tramos
 CREATE OR REPLACE FUNCTION budu.cortar_calles(idc integer, fuenteid integer, tipovia integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    sections geometry;
    inputgeom geometry;
    elem geometry;
    tramos budu.tramo%ROWTYPE;
    idtramo integer;
    tolerance constant numeric := 0.5;
    interseccion geometry;
BEGIN
    --****************************************************************
    --Corta los tramos de la bd que intersectan con una calle dada en el parámetro idc
    --****************************************************************
    RAISE NOTICE 'SPLIT...........idcalle:%', idc;
    --Une la calle dada en una sola geometría
    SELECT st_union(geom) INTO inputgeom FROM budu.tramo WHERE idcalle = idc;
    --Une las intersecciones en una sola geometría. Hace un snap con una tolerancia pequeña para garantizar el corte
    SELECT st_union(t.geom) FROM budu.tramo t WHERE ST_Intersects(st_snap(t.geom, inputgeom, tolerance),  st_snap(inputgeom, t.geom, tolerance)) AND idcalle != idc into interseccion;
    
    RAISE NOTICE 'Intersecciones: %', st_astext(interseccion);
    --Realiza el corte
    SELECT budu.cortar_geom(inputgeom, interseccion, tolerance) INTO sections;

    IF sections IS NOT NULL AND ST_Numgeometries(sections) > 1 then
        --Borra los tramos existentes de la calle a cortar 
        DELETE FROM budu.tramo WHERE idcalle = idc;
        --Inserta los tramos cortados
        for elem IN 1 .. ST_Numgeometries(sections) LOOP
            RAISE NOTICE 'Insertando tramo de idcalle:% % %', idc, elem, ST_AsText(ST_GeometryN(sections, elem));
            INSERT INTO budu.tramo (idcalle, geom, fuente_id, tipo_vialidad_id ) VALUES(idc, ST_Multi(ST_GeometryN(sections, elem)), fuenteid, tipovia);
        END LOOP;
    END IF;
    
    --Cortamos tramos existentes
    FOR idtramo in SELECT gid FROM budu.tramo t WHERE ST_Intersects(st_snap(t.geom, inputgeom, tolerance),  st_snap(inputgeom, t.geom, tolerance)) AND idcalle != idc LOOP
        RAISE NOTICE 'Tramo existente a cortar id:%', idtramo;
        PERFORM budu.cortar_tramo_existente(idtramo, inputgeom, tolerance);
    END LOOP;
    
END;
$function$
;


CREATE OR REPLACE FUNCTION budu.cortar_tramo_existente(idt integer, blade geometry, tolerance numeric)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
    DECLARE
        sections geometry;
        tramo budu.tramo%ROWTYPE;
        elem geometry;
    begin
        --****************************************************************
        --Corta los tramos de la bd identificados por su id (idt) con la geometría (blade)
        --El valor de tolerancia para el snap es en grados (actualmente se llama con 0.5)
        --****************************************************************
        SELECT * from budu.tramo t where t.gid = idt INTO tramo;
        SELECT budu.cortar_tramo_por_id(idt, blade, tolerance) INTO sections;
        IF sections IS NOT NULL AND ST_Numgeometries(sections) > 1 then
            --Borra el tramo existente a cortar 
            DELETE FROM budu.tramo WHERE gid = idt;
            --Inserta los tramos cortados
            for elem IN 1 .. ST_Numgeometries(sections) LOOP
                RAISE NOTICE 'Insertando tramo existente de idcalle:% % %', tramo.idcalle, elem, ST_AsText(ST_GeometryN(sections, elem));
                INSERT INTO budu.tramo (idcalle, geom, fuente_id, tipo_vialidad_id) VALUES(tramo.idcalle, ST_Multi(ST_GeometryN(sections, elem)), tramo.fuente_id, tramo.tipo_vialidad_id);
            END LOOP;
        END if;
    END;
$function$
;

CREATE OR REPLACE FUNCTION budu.cortar_tramo_por_id(idtramo integer, blade geometry, tolerance numeric)
 RETURNS geometry
 LANGUAGE plpgsql
AS $function$
    DECLARE
        sections geometry;
    BEGIN
        --****************************************************************
        --Corta un tramo a partir de una geometría usando un valor de tolerancia
        --Para realizar un snap previo al corte
        --****************************************************************
        SELECT ST_Split(st_snap(t.geom, blade, tolerance),  st_snap(blade, t.geom, tolerance)) from budu.tramo t where t.gid = idtramo INTO sections;
        return sections;
        EXCEPTION
        WHEN OTHERS then
            SELECT budu.cortar_tramo_por_id_exception(idtramo, blade, tolerance) INTO sections;
            return sections;
    END;
$function$
;

CREATE OR REPLACE FUNCTION budu.cortar_tramo_por_id_exception(idtramo integer, blade geometry, tolerance numeric)
 RETURNS geometry
 LANGUAGE plpgsql
AS $function$
    DECLARE
        sections geometry;
    BEGIN
        --****************************************************************
        --Si se produce una excepción al hacer el split por el snap entre 
        --geometrías lo intentamos sin snap  
        --****************************************************************
        SELECT ST_Split(t.geom,  st_snap(blade, t.geom, tolerance)) from budu.tramo t where t.gid = idtramo INTO sections;
        return sections;
        EXCEPTION
        WHEN OTHERS then
            SELECT ST_Split(t.geom,  blade) from budu.tramo t where t.gid = idtramo INTO sections;
            return sections;
    END;
$function$
;


CREATE OR REPLACE FUNCTION budu.cortar_geom(inputgeom geometry, blade geometry, tolerance numeric)
 RETURNS geometry
 LANGUAGE plpgsql
AS $function$
    DECLARE
        sections geometry;
    BEGIN
        --****************************************************************
        --Corta una geometría a partir de otra usando un valor de tolerancia
        --Para realizar un snap previo al corte
        --****************************************************************
        SELECT ST_Split(st_snap(inputgeom, blade, tolerance),  st_snap(blade, inputgeom, tolerance)) INTO sections;
        return sections;
        EXCEPTION
        WHEN OTHERS then
            SELECT budu.cortar_geom_exception(inputgeom, blade, tolerance) INTO sections;
            return sections;
    END;
$function$
;

CREATE OR REPLACE FUNCTION budu.cortar_geom_exception(inputgeom geometry, blade geometry, tolerance numeric)
 RETURNS geometry
 LANGUAGE plpgsql
AS $function$
    DECLARE
        sections geometry;
    BEGIN
        --****************************************************************
        --Si se produce una excepción al hacer el split por el snap entre 
        --geometrías lo intentamos sin snap  
        --****************************************************************
        SELECT ST_Split(inputgeom,  st_snap(blade, inputgeom, tolerance)) INTO sections;
        return sections;
        EXCEPTION
        WHEN OTHERS then
            SELECT ST_Split(inputgeom,  blade) INTO sections;
            return sections;
    END;
$function$
;


 --**************************************
 -- TRIGGERS PARA INSERCION EN ALIAS

  
  CREATE TRIGGER historico_alias_calle
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_calle
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(0);
  
  CREATE TRIGGER historico_alias_departamento
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_departamento
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(1);
  
  CREATE TRIGGER historico_alias_entidad_colectiva
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_entidad_colectiva
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(2);
  
  CREATE TRIGGER historico_alias_localidad_geo
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_localidad_geo
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(3);
  
  CREATE TRIGGER historico_alias_manzana
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_manzana
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(4);
  
  CREATE TRIGGER historico_alias_numero_puerta
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_numero_puerta
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(5);
  
  CREATE TRIGGER historico_alias_punto_notable
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_punto_notable
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(6);
  
  CREATE TRIGGER historico_alias_solarp
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_solarp
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(7);
  
  CREATE TRIGGER historico_alias_tramo_ruta
  AFTER INSERT OR UPDATE OR DELETE
  ON budu.alias_tramo_ruta
  FOR EACH ROW
  EXECUTE PROCEDURE historico.log_alias(8);
  


  
-- *****************************************************
--Trigger para cálculo de cp


CREATE TRIGGER trigger_cp_portal_pk
BEFORE INSERT OR UPDATE
ON budu.portal_pk
FOR EACH ROW
EXECUTE PROCEDURE budu.calc_cp();


CREATE TRIGGER trigger_cp_punto_notable
BEFORE INSERT OR UPDATE
ON budu.punto_notable
FOR EACH ROW
EXECUTE PROCEDURE budu.calc_cp();


CREATE TRIGGER trigger_cp_solarp
BEFORE INSERT OR UPDATE
ON budu.solarp
FOR EACH ROW
EXECUTE PROCEDURE budu.calc_cp();
  

