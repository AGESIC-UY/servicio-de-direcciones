package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class TramoResult {
	long gid;
	int idcalle;
	int fuente_id;
	int tipo_vialidad_id;
	
//	@JsonIgnore
	String geom;
}
