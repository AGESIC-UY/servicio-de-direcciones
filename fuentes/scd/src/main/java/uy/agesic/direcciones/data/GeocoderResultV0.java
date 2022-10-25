package uy.agesic.direcciones.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class GeocoderResultV0 {
	private Direccion direccion;
	private int codigoPostal;
	
	@JsonIgnore
	private int codigoPostalAmpliado;
	
	private double puntoX;
	private double puntoY;
	private int idPunto;
	private int srid = 4326;
	private int idTipoClasificacion;
	private String error = "";
	
	public void setCodigoPostal(int cp) {
		this.codigoPostalAmpliado = cp;
		this.codigoPostal = (cp / 100) * 100;
	}

}
