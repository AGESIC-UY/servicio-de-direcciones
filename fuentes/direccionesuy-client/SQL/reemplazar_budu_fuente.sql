
alter table budu.fuente alter column fuente type character varying(50);

INSERT INTO budu.fuente (id,fuente,descripcion) values (0, 'INDETERMINADO', 'INDETERMINADO') ON CONFLICT (id) do update set fuente = 'INDETERMINADO', descripcion = 'INDETERMINADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (1,'DESDE_VISTA','DESDE VISTA') ON CONFLICT (id) do update set fuente = 'DESDE VISTA', descripcion = 'DESDE VISTA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (2,'CALLE_NUMERO_DESDE_TSUBASA','CALLE NUMERO DESDE TSUBASA') ON CONFLICT (id) do update set fuente = 'CALLE_NUMERO_DESDE_TSUBASA', descripcion = 'CALLE NUMERO DESDE TSUBASA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (3,'DESDE_TSUBASA','DESDE TSUBASA') ON CONFLICT (id) do update set fuente = 'DESDE_TSUBASA', descripcion = 'DESDE TSUBASA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (4,'UTE','ADMINISTRACIÓN NACIONAL DE USINAS Y TRASMISIONES ELÉCTRICAS') ON CONFLICT (id) do update set fuente = 'UTE', descripcion = 'ADMINISTRACIÓN NACIONAL DE USINAS Y TRASMISIONES ELÉCTRICAS' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (5,'INE','INSTITUTO NACIONAL DE ESTADÍSTICA') ON CONFLICT (id) do update set fuente = 'INE', descripcion = 'INSTITUTO NACIONAL DE ESTADÍSTICA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (6,'ANTEL_GUIA','ANTEL GUIA') ON CONFLICT (id) do update set fuente = 'ANTEL_GUIA', descripcion = 'ANTEL GUIA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (7,'EQUISY','EQUISY') ON CONFLICT (id) do update set fuente = 'EQUISY', descripcion = 'EQUISY' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (8,'INTENDENCIA_DE_MONTEVIDEO','INTENDENCIA DE MONTEVIDEO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_MONTEVIDEO', descripcion = 'INTENDENCIA DE MONTEVIDEO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (9,'ANC','ADMINISTRACIÓN NACIONAL DE CORREOS') ON CONFLICT (id) do update set fuente = 'ANC', descripcion = 'ADMINISTRACIÓN NACIONAL DE CORREOS' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (10,'ANC_PARA_REVISIÓN','ANC PARA REVISIÓN') ON CONFLICT (id) do update set fuente = 'ANC_PARA_REVISIÓN', descripcion = 'ANC PARA REVISIÓN' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (11,'OSE','OBRAS SANITARIAS DEL ESTADO') ON CONFLICT (id) do update set fuente = 'OSE', descripcion = 'OBRAS SANITARIAS DEL ESTADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (12,'UTE_MODIFICADO','UTE MODIFICADO') ON CONFLICT (id) do update set fuente = 'UTE_MODIFICADO', descripcion = 'UTE MODIFICADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (13,'IM_MODIFICADO','IM MODIFICADO') ON CONFLICT (id) do update set fuente = 'IM_MODIFICADO', descripcion = 'IM MODIFICADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (14,'EQUISY_MODIFICADO','EQUISY MODIFICADO') ON CONFLICT (id) do update set fuente = 'EQUISY_MODIFICADO', descripcion = 'EQUISY MODIFICAD' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (15,'OSM','OPEN STREET MAP') ON CONFLICT (id) do update set fuente = 'OSM', descripcion = 'OPEN STREET MAP' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (16,'GSV','GSV') ON CONFLICT (id) do update set fuente = 'GSV', descripcion = 'GSV' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (17,'ANC_GSV','ANC_GSV') ON CONFLICT (id) do update set fuente = 'ANC_GSV', descripcion = 'ANC_GSV' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (18,'CARGA_MASIVA','CARGA MASIVA') ON CONFLICT (id) do update set fuente = 'CARGA_MASIVA', descripcion = 'CARGA MASIVA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (19,'MAPILLARY','MAPILLARY') ON CONFLICT (id) do update set fuente = 'MAPILLARY', descripcion = 'MAPILLARY' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (20,'MIDES','MINISTERIO DE DESARROLLO SOCIAL') ON CONFLICT (id) do update set fuente = 'MIDES', descripcion = 'MINISTERIO DE DESARROLLO SOCIAL' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (21,'MVOTMA','MINISTERIO DE VIVIENDA Y ORDENAMIENTO TERRITORIAL') ON CONFLICT (id) do update set fuente = 'MVOTMA',descripcion = 'MINISTERIO DE VIVIENDA Y ORDENAMIENTO TERRITORIAL' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (22,'CARGA_A_PARTIR_DE_POSTGRES.PADRONES_RURALES','CARGA A PARTIR DE POSTGRES.PADRONES_RURALES') ON CONFLICT (id) do update set fuente = 'CARGA_A_PARTIR_DE_POSTGRES.PADRONES_RURALES', descripcion = 'CARGA A PARTIR DE POSTGRES.PADRONES_RURALES' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (23,'MIDES_MODIFICADO','MIDES MODIFICADO') ON CONFLICT (id) do update set fuente = 'MIDES_MODIFICADO', descripcion = 'MIDES MODIFICADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (24,'INTENDENCIA_DE_CERRO_LARGO','INTENDENCIA DE CERRO LARGO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_CERRO_LARGO', descripcion = 'INTENDENCIA DE CERRO LARGO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (25,'DNC','DIRECCIÓN NACIONAL DE CATASTRO') ON CONFLICT (id) do update set fuente = 'DNC', descripcion = 'DIRECCIÓN NACIONAL DE CATASTRO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (26,'ANTEL_BASE','ANTEL BASE') ON CONFLICT (id) do update set fuente = 'ANTEL_BASE', descripcion = 'ANTEL BASE' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (27,'UTE_XY_ANTEL_ATRIBUTO','UTE XY ANTEL ATRIBUTO') ON CONFLICT (id) do update set fuente = 'UTE_XY_ANTEL_ATRIBUTO', descripcion = 'UTE XY ANTEL ATRIBUTO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (28,'MTSS','MINISTERIO DE TRABAJO Y SEGURIDAD SOCIAL DE LA REPÚBLICA') ON CONFLICT (id) do update set fuente = 'MTSS', descripcion = 'MINISTERIO DE TRABAJO Y SEGURIDAD SOCIAL DE LA REPÚBLICA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (29,'INTENDENCIA_DE_SAN_JOSE','INTENDENCIA DE SAN JOSE') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_SAN_JOSE', descripcion = 'INTENDENCIA DE SAN JOSE' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (30,'INTENDENCIA_DE_RIVERA','INTENDENCIA DE RIVERA') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_RIVERA', descripcion = 'INTENDENCIA DE RIVERA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (31,'IDE','INFRAESTRUCTURA DE DATOS ESPACIALES') ON CONFLICT (id) do update set fuente = 'IDE', descripcion = 'INFRAESTRUCTURA DE DATOS ESPACIALES' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (32,'ACTUALIZACION_MASIVA','ACTUALIZACION MASIVA') ON CONFLICT (id) do update set fuente = 'ACTUALIZACION_MASIVA', descripcion = 'ACTUALIZACION MASIVA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (33,'ANTEL_FIJO','ANTEL FIJO') ON CONFLICT (id) do update set fuente = 'ANTEL_FIJO', descripcion = 'ANTEL FIJO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (34,'BING','BING') ON CONFLICT (id) do update set fuente = 'BING', descripcion = 'BING' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (35,'IMPUESTO_DE_PRIMARIA','IMPUESTO DE PRIMARIA') ON CONFLICT (id) do update set fuente = 'IMPUESTO_DE_PRIMARIA', descripcion = 'IMPUESTO DE PRIMARIA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (36,'INTENDENCIA_DE_CANELONES','INTENDENCIA DE CANELONES') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_CANELONES', descripcion = 'INTENDENCIA DE CANELONES' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (37,'INTENDENCIA_DE_COLONIA','INTENDENCIA DE COLONIA') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_COLONIA', descripcion = 'INTENDENCIA DE COLONIA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (38,'INTENDENCIA_DE_MALDONADO','INTENDENCIA DE MALDONADO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_MALDONADO', descripcion = 'INTENDENCIA DE MALDONADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (39,'INTENDENCIA_DE_ROCHA','INTENDENCIA DE ROCHA') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_ROCHA', descripcion = 'INTENDENCIA DE ROCHA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (40,'OSE_MODIFICADO','OSE MODIFICADO') ON CONFLICT (id) do update set fuente = 'OSE_MODIFICADO', descripcion = 'OSE MODIFICADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (41,'INE_MODIFICADO','INE MODIFICADO') ON CONFLICT (id) do update set fuente = 'INE_MODIFICADO', descripcion = 'INE MODIFICADO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (42,'MINISTERIO_DEL_INTERIOR','MINISTERIO DEL INTERIOR') ON CONFLICT (id) do update set fuente = 'MINISTERIO_DEL_INTERIOR', descripcion = 'MINISTERIO DEL INTERIOR' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (43,'PODER_LEGISLATIVO','PODER LEGISLATIVO') ON CONFLICT (id) do update set fuente = 'PODER_LEGISLATIVO', descripcion = 'PODER LEGISLATIVO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (44,'CDP','CONJUNTO DE DATOS PROVISORIO') ON CONFLICT (id) do update set fuente = 'CDP', descripcion = 'CONJUNTO DE DATOS PROVISORIO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (45,'MTOP','MINISTERIO DE TRANSPORTE Y OBRAS PÚBLICAS') ON CONFLICT (id) do update set fuente = 'MTOP', descripcion = 'MINISTERIO DE TRANSPORTE Y OBRAS PÚBLICAS' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (46,'INTENDENCIA_DE_ARTIGAS','INTENDENCIA DE ARTIGAS') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_ARTIGAS', descripcion = 'INTENDENCIA DE ARTIGAS' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (47,'INTENDENCIA_DE_DURAZNO','INTENDENCIA DE DURAZNO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_DURAZNO', descripcion = 'INTENDENCIA DE DURAZNO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (48,'INTENDENCIA_DE_FLORES','INTENDENCIA DE FLORES') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_FLORES', descripcion = 'INTENDENCIA DE FLORES' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (49,'INTENDENCIA_DE_FLORIDA','INTENDENCIA DE FLORIDA') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_FLORIDA', descripcion = 'INTENDENCIA DE FLORIDA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (50,'INTENDENCIA_DE_LAVALLEJA','INTENDENCIA DE LAVALLEJA') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_LAVALLEJA', descripcion = 'INTENDENCIA DE LAVALLEJA' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (51,'INTENDENCIA_DE_PAYSANDÚ','INTENDENCIA DE PAYSANDÚ') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_PAYSANDÚ', descripcion = 'INTENDENCIA DE PAYSANDÚ' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (52,'INTENDENCIA_DE_RÍO_NEGRO','INTENDENCIA DE RÍO NEGRO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_RÍO_NEGRO', descripcion = 'INTENDENCIA DE RÍO NEGRO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (53,'INTENDENCIA_DE_SALTO','INTENDENCIA DE SALTO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_SALTO', descripcion = 'INTENDENCIA DE SALTO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (54,'INTENDENCIA_DE_SORIANO','INTENDENCIA DE SORIANO') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_SORIANO', descripcion = 'INTENDENCIA DE SORIANO' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (55,'INTENDENCIA_DE_TACUAREMBÓ','INTENDENCIA DE TACUAREMBÓ') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_TACUAREMBÓ', descripcion = 'INTENDENCIA DE TACUAREMBÓ' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (56,'INTENDENCIA_DE_TREINTA_Y_TRES','INTENDENCIA DE TREINTA Y TRES') ON CONFLICT (id) do update set fuente = 'INTENDENCIA_DE_TREINTA_Y_TRES', descripcion = 'INTENDENCIA DE TREINTA Y TRES' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (57,'MINISTERIO_DE_AMBIENTE','MINISTERIO DE AMBIENTE') ON CONFLICT (id) do update set fuente = 'MINISTERIO_DE_AMBIENTE', descripcion = 'MINISTERIO DE AMBIENTE' ;
INSERT INTO budu.fuente (id,fuente,descripcion) values (58,'AGESIC','AGENCIA DE GOBIERNO ELECTRÓNICO Y SOCIEDAD DE LA INFORMACIÓN Y DEL CONOCIMIENTO') ON CONFLICT (id) do update set fuente = 'AGESIC', descripcion = 'AGENCIA DE GOBIERNO ELECTRÓNICO Y SOCIEDAD DE LA INFORMACIÓN Y DEL CONOCIMIENTO' ;