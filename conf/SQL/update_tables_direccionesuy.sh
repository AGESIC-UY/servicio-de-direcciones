#!/bin/bash
psql postgresql://geomatica:controlce@localhost/tsubasa << EOF
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

      REFRESH MATERIALIZED VIEW budu.mv_puntos_direccion;
      REFRESH MATERIALIZED VIEW budu.mv_direcciones2;
	  refresh materialized view budu.calle_con_geom;
EOF
