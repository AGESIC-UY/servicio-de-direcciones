package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NombreNormalizadoDepartamento {
	
	@JsonProperty("nombre_normalizado")
	String nombreNormalizado;
	
	int idDepartamento;
}
