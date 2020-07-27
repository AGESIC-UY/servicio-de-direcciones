-- ALTER TABLE BUDU.tipo_vialidad

-- ALTER TABLE BUDU.tipo_alias ADD PRIMARY KEY (id); 


alter table BUDU.alias_calle ADD CONSTRAINT alias_calle_tipo_alias_fk FOREIGN KEY (tipo_alias_id) REFERENCES budu.tipo_alias (id);
alter table BUDU.alias_calle ADD CONSTRAINT alias_calle_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);


alter table BUDU.alias_departamento ADD CONSTRAINT alias_departamento_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_entidad_colectiva ADD CONSTRAINT alias_entidad_colectiva_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_localidad_geo ADD CONSTRAINT alias_localidad_geo_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_numero_puerta ADD CONSTRAINT alias_numero_puerta_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_tramo_ruta ADD CONSTRAINT alias_tramo_ruta_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_punto_notable ADD CONSTRAINT alias_punto_notable_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

alter table BUDU.alias_solarp ADD CONSTRAINT alias_solarp_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

---


ALTER TABLE BUDU.calle ADD CONSTRAINT calle_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
ALTER TABLE BUDU.calle ADD CONSTRAINT calle_ruta_fk FOREIGN KEY (ruta_id) REFERENCES budu.ruta (id);
-- TODO: QUITAR IDLOCALIDAD DE CALLE (HAY REGISTROS CON IDLOCALIDAD QUE NO EXISTE EN LOCALIDADES_NO_OFICIALES (ejemplo: idlocalidad = 90)
-- ALTER TABLE BUDU.calle ADD CONSTRAINT calle_localidad_fk FOREIGN KEY (idlocalidad) REFERENCES budu.localidades_no_oficiales (apis_id);

ALTER TABLE BUDU.calle_departamento ADD CONSTRAINT calle_departamento_departamento_fk FOREIGN KEY (iddepartamento) REFERENCES budu.departamento (id);
-- Hay registros en calle_departamento con un idcalle que no existe en calle (ejemplo: 8415)
-- ALTER TABLE BUDU.calle_departamento ADD CONSTRAINT calle_departamento_calle_fk FOREIGN KEY (idcalle) REFERENCES budu.calle (id);

-- Hay registros en calle_localidad con un idlocalidad no váliddo. Ej: 5. TODO: Corregir esto para poder crear este indice.
-- ALTER TABLE BUDU.calle_localidad  ADD CONSTRAINT calle_localidad_localidad_fk FOREIGN KEY (idlocalidad) REFERENCES budu.localidades_no_oficiales (apis_id);

ALTER TABLE budu.codigos_postales_vigentes ADD CONSTRAINT codigos_postales_vigentes_pk PRIMARY KEY (cod_postal);
ALTER TABLE BUDU.codigos_postales_vigentes ADD CONSTRAINT cp_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
-- apis_id = 31 no está en localidades_no_oficiales TODO:
-- ALTER TABLE BUDU.codigos_postales_vigentes ADD CONSTRAINT cp_localidad_fk FOREIGN KEY (apis_id) REFERENCES budu.localidades_no_oficiales (apis_id);


-- ALTER TABLE BUDU.departamento (like public.departamento including all);

-- TODO: PREGUNTAR SI HAY QUE PODER BUSCAR ENTIDAD_COLECTIVA Y SI LO INCLUIMOS EN PUNTO NOTABLE O NO.
-- ALTER TABLE BUDU.entidad_colectiva (like public.entidad_colectiva including all);

-- ALTER TABLE BUDU.localidades_no_oficiales (like public.localidades_no_oficiales including all);

ALTER table BUDU.intersec_localidad_departamento ADD CONSTRAINT intersec_departamento_fk FOREIGN KEY (iddepto) REFERENCES budu.departamento (id);
ALTER table BUDU.intersec_localidad_departamento ADD CONSTRAINT intersec_localidad_fk FOREIGN KEY (idloc) REFERENCES budu.localidades_no_oficiales (apis_id);
		

-- ALTER TABLE BUDU.forma_fuente


-- ALTER TABLE BUDU.fuente
ALTER TABLE BUDU.punto_notable  ADD PRIMARY KEY (id); 
ALTER TABLE BUDU.punto_notable  ADD CONSTRAINT punto_notable_calle_fk FOREIGN KEY (idcalle) REFERENCES budu.calle (id);
-- TODO: COD_POSTAL ES DE TIPO TEXT. DEBERIAMOS CAMBIARLO A INTEGER SEGURAMENTE
-- ALTER TABLE BUDU.punto_notable  ADD CONSTRAINT punto_notable_cp_fk FOREIGN KEY (cp) REFERENCES budu.codigos_postales_vigentes (cod_postal));

ALTER TABLE BUDU.manzana ADD CONSTRAINT manzana_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

ALTER TABLE BUDU.solar ADD CONSTRAINT solar_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
-- ALTER TABLE BUDU.solar ADD CONSTRAINT solar_manzana_fk FOREIGN KEY (manzana) REFERENCES budu.manzana (manzana);


ALTER TABLE BUDU.ruta ADD CONSTRAINT ruta_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);

-- ALTER TABLE BUDU.sinonimo (like public.sinonimo including all);

-- ALTER TABLE BUDU.sinonimo_entidad (like public.sinonimo_entidad including all);

-- ALTER TABLE BUDU.sinonimo_localidad (like public.sinonimo_localidad including all);

ALTER TABLE BUDU.tramo_ruta ADD CONSTRAINT tramo_ruta_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
ALTER TABLE BUDU.tramo_ruta ADD CONSTRAINT tramo_ruta_ruta_fk FOREIGN KEY (id_ruta) REFERENCES budu.ruta (id);

ALTER TABLE BUDU.portal_pk ADD CONSTRAINT portalpk_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
-- TODO idcalle=41821 no está en la tabla calle. Hay que añadir esas calles o borrar los portales que no tienen bien el idcalle.
-- ALTER TABLE BUDU.portal_pk ADD CONSTRAINT portalpk_calle_fk FOREIGN KEY (idcalle) REFERENCES budu.calle (id);
-- TODO: Foreign Key con cp cuando cod_postal se pase a Integer.
		

ALTER TABLE BUDU.solarp ADD CONSTRAINT solarp_fuente_fk FOREIGN KEY (fuente_id) REFERENCES budu.fuente (id);
-- TODO: AHORA LOS IDCALLE ESTÁN A CERO. DEBERÍAN TENER SIEMPRE UNA CALLE ASIGNADA?
-- ALTER TABLE BUDU.solarp ADD CONSTRAINT solarp_calle_fk FOREIGN KEY (idcalle) REFERENCES budu.calle (id);
ALTER TABLE BUDU.solarp ADD CONSTRAINT solarp_solar_fk FOREIGN KEY (solar_id) REFERENCES budu.solar (apis_id);
-- TODO: No puede haber foreign key con padron porque no va a ser una clave única tal y como está.
--ALTER TABLE BUDU.solarp ADD CONSTRAINT solarp_padron_fk FOREIGN KEY (padron) REFERENCES budu.solar (padron);

-- TODO: Quitar los tramos que tienen un idcalle que no está en la tabla calle. Ej: 10851
--alter table budu.tramo add constraint tramo_calle_fk foreign key (idcalle) references budu.calle (id);
alter table budu.tramo add constraint tramo_fuente_fk foreign key (fuente_id) references budu.fuente (id);
	



