package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class CapaPoligonalResult {
	String tabla;
	String descripcion;
	String campo_id;
	
	@JsonIgnore
	String campoGeom;
}
