package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NombreNormalizadoLocalidad {
	
	@JsonProperty("nombre_normalizado")
	String nombreNormalizado;
	
	int idLocalidad;
}
