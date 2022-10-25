package uy.agesic.direcciones.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LocalidadResultV0 {
	int id;
	String nombre;
	
	@JsonProperty("codigoPostal")
	int codigoPostal;
	
	@JsonProperty("alias")
	List<AliasLocalidad> alias;

}
