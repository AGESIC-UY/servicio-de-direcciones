package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CalleNormalizada {
	
	@JsonProperty("nombre_normalizado")
	String nombreNormalizado;

	Integer idCalle;
}
