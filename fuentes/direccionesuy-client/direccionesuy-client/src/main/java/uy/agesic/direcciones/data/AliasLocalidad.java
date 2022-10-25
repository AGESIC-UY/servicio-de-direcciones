package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class AliasLocalidad {

	int id;
	String nombre;
	
	@JsonIgnore
	boolean tipo_alias;
}
