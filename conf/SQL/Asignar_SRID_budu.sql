select * from geometry_columns
where f_table_schema like 'budu';

SELECT UpdateGeometrySRID('budu', 'codigos_postales_vigentes','multipolygon',32721);
SELECT UpdateGeometrySRID('budu', 'punto_notable','punto',32721);
-- SELECT UpdateGeometrySRID('budu', 'mv_puntos_direccion','p4326',4326);
SELECT UpdateGeometrySRID('budu', 'intersec_localidad_departamento','intersec',32721);
SELECT UpdateGeometrySRID('budu', 'portal_pk','punto',32721);
SELECT UpdateGeometrySRID('budu', 'punto_notable','punto',32721);