
-- View: budu.puntos_direccion

DROP  materialized view IF EXISTS budu.mv_puntos_direccion CASCADE;

CREATE materialized VIEW budu.mv_puntos_direccion AS
 SELECT ppk.id,
    st_y(st_transform(ppk.punto, 4326)) AS latitud,
    st_x(st_transform(ppk.punto, 4326)) AS longitud,
    st_transform(ppk.punto, 4326) AS p4326,
    ppk.idcalle,
    calle.nombre,  -- calle.id_localidad tampoco es correcto. Necesitamos un update, o quitar este campo.
    ppk.numero,
	ppk.letra,
    l.apis_id as idLocalidad,
    l.nombre AS localidad,
    -- l.depto as loca_Departamento, -- No coincide el departamento de localidad con el que en realidad es.
    -- l.apis_id, l.buscar, l.cod_postal, l.nombre,
    d.id as idDepartamento,
    d.nombre as depa,
--    portal_pk.padron AS ppadron,
--    solar.solar,
--    solar.padron AS spadron,
--    solar.manzana,
    ppk.km,
    -- portal_pk.nombre_inmueble,
    ppk.cp,
    to_tsvector('spanish', unaccent(COALESCE(calle.nombre, ' ')) || ' ' || 
    	unaccent(COALESCE(l.nombre, ' ')) || ' ' || unaccent(COALESCE(d.nombre, ''::text))) AS fulltext
   FROM budu.portal_pk ppk
     INNER JOIN budu.calle ON ppk.idcalle = calle.id
     inner JOIN budu.calle_localidad cl ON ppk.idcalle = cl.idcalle
     INNER join budu.localidades_no_oficiales l on cl.idlocalidad = l.apis_id
--     LEFT JOIN solar ON ppk.solar_id = solar.apis_id
     INNER JOIN budu.calle_departamento cd ON ppk.idcalle = cd.idcalle
     INNER join budu.departamento d on cd.iddepartamento = d.id;
--    limit 100;

CREATE INDEX idx_fulltext_mv_puntos_direccion
    ON budu.mv_puntos_direccion USING gin
    (fulltext);

CREATE INDEX idx_puntos_direccion_p4326 ON budu.mv_puntos_direccion USING GIST(p4326);
CREATE INDEX IF NOT EXISTS punto_notable_punto_idx ON budu.punto_notable USING GIST(st_transform(punto, 4326));

CREATE INDEX idx_puntos_direccion_on_idcalle ON budu.mv_puntos_direccion (idcalle);
CREATE INDEX idx_depa_localidad_nombre ON budu.mv_puntos_direccion (depa, localidad, nombre);
CREATE INDEX idx_puntos_direccion_on_nombre ON budu.mv_puntos_direccion USING btree (nombre);

-- View: budu.puntos_direccion

DROP MATERIALIZED VIEW IF EXISTS budu.mv_direcciones2 CASCADE;

CREATE MATERIALIZED VIEW budu.mv_direcciones2
AS
 SELECT DISTINCT count(*) AS numregs,
    mv_puntos_direccion.idcalle,
    mv_puntos_direccion.nombre,
    alias_calle.nombre as aliasBuscar,
    mv_puntos_direccion.idLocalidad,
    mv_puntos_direccion.localidad,
    mv_puntos_direccion.idDepartamento,
    mv_puntos_direccion.depa AS departamento,
    mv_puntos_direccion.cp,
    avg(mv_puntos_direccion.latitud) AS lat,
    avg(mv_puntos_direccion.longitud) AS lng,
    --mv_puntos_direccion.nombre_inmueble,
    to_tsvector('spanish', unaccent(COALESCE(alias_calle.nombre, ' ')) || ' ' || 
    	unaccent(COALESCE(mv_puntos_direccion.localidad, ' ')) || ' ' || unaccent(COALESCE(mv_puntos_direccion.depa, ''))) AS fulltext
   FROM budu.mv_puntos_direccion 
     JOIN budu.alias_calle ON alias_calle.idcalle = mv_puntos_direccion.idcalle
  GROUP BY mv_puntos_direccion.nombre, alias_calle.nombre, mv_puntos_direccion.idcalle, mv_puntos_direccion.idLocalidad, mv_puntos_direccion.localidad, mv_puntos_direccion.idDepartamento, mv_puntos_direccion.depa,
  mv_puntos_direccion.cp, mv_puntos_direccion.fulltext
  UNION
	SELECT 
		0 as numregs,
		c.id as idcalle,
		c.nombre,
		alias_calle.nombre as aliasbuscar,
		cl.idlocalidad as idLocalidad,
		l.nombre as localidad,
		d.id as idDepartamento,
		d.nombre as departamento,
		NULLIF(l.cod_postal, '')::int as cp,		
		0 as lat,
		0 as lng,
		to_tsvector('spanish'::regconfig, (((unaccent(COALESCE(c.nombre, ' '::text)) || ' '::text) || 
			unaccent(COALESCE(l.nombre, ' '::character varying)::text)) || ' '::text) ||
			unaccent(COALESCE(d.nombre, ''::text))) AS fulltext
	FROM budu.calle c
		LEFT JOIN budu.alias_calle ON alias_calle.idcalle = c.id
		inner join budu.calle_departamento cd on cd.idcalle = c.id
		inner join budu.calle_localidad cl on cl.idcalle = c.id 
		inner join budu.localidades_no_oficiales l on l.apis_id = cl.idlocalidad 
		inner join budu.departamento d on d.id = cd.iddepartamento 
		left join budu.mv_puntos_direccion mv on c.id = mv.idcalle 
	where mv.idcalle is null
  	--, mv_puntos_direccion.manzana, mv_puntos_direccion.nombre_inmueble
 --HAVING mv_puntos_direccion.manzana IS NULL
WITH DATA;


CREATE INDEX idx_fulltext_mv_direcciones2
    ON budu.mv_direcciones2 USING gin
    (fulltext)
    TABLESPACE pg_default;
	
CREATE INDEX mv_direcciones2_idcalle_idx ON budu.mv_direcciones2 USING btree (idcalle);
CREATE INDEX idx_mv_direcciones2_nombre ON budu.mv_direcciones2 USING btree (nombre);
-- CREATE INDEX idx_soundex_buscar_direcciones2 ON budu.mv_direcciones2 USING btree (soundexesp(nombre));
-- CREATE INDEX idx_soundex_departamento_direcciones2 ON budu.mv_direcciones2 USING btree (soundexesp(departamento));


	
CREATE INDEX idx_mv_direcciones2_departamento ON budu.mv_direcciones2 USING btree (departamento);
CREATE INDEX IF NOT EXISTS localidades_no_oficiales_buscar_idx ON budu.localidades_no_oficiales USING btree (nombre);


-- Vista para poder hacer búsquedas de esquinas
DROP materialized VIEW if exists budu.calle_con_geom CASCADE;

CREATE MATERIALIZED VIEW budu.calle_con_geom as
SELECT 
  --DISTINCT 
  c.idcalle,
  c.nombre,
  c.localidad,
  c.departamento,
  st_union(t.geom) as geom
FROM budu.mv_direcciones2 c 
 inner join budu.tramo t on c.idcalle = t.idcalle
 group by c.idcalle, c.nombre, c.localidad, c.departamento, t.geom
 having t.geom is not null; 
 
 CREATE INDEX calle_con_geom_idx
  ON budu.calle_con_geom 
  USING GIST (geom);
  
CREATE INDEX calle_con_geom_idcalle_idx
  ON budu.calle_con_geom (idcalle);  


CREATE INDEX idx_mv_direcciones2_iddepartamento ON budu.mv_direcciones2 USING btree (iddepartamento);
CREATE INDEX idx_mv_direcciones2_idlocalidad ON budu.mv_direcciones2 USING btree (idlocalidad);

CREATE INDEX IF NOT EXISTS alias_departamento_id_idcalle_idx ON budu.alias_departamento USING btree (departamento_id);



-- drop index index_mv_direcciones2_on_nombre_trigram;

-- Ejecutar esta línea DESPUÉS de haber creado la función inmmutable_unaccent.
CREATE INDEX CONCURRENTLY index_mv_direcciones2_on_nombre_trigram ON budu.mv_direcciones2 USING gin (immutable_unaccent(nombre) gin_trgm_ops);
CREATE INDEX CONCURRENTLY index_mv_direcciones2_on_aliasbuscar_trigram ON budu.mv_direcciones2 USING gin (immutable_unaccent(aliasbuscar) gin_trgm_ops);

--ANALYZE budu.mv_direcciones2;



