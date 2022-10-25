package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Direccion {
	
	NombreNormalizadoDepartamento departamento;
	
	NombreNormalizadoLocalidad localidad;

	CalleNormalizada calle;
	
	Numero numero;
	
	Integer manzana;
	
	Integer solar;
	
	Inmueble inmueble;

}
