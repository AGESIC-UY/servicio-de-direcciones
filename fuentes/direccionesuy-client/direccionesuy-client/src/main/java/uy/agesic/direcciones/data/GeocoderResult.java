package uy.agesic.direcciones.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uy.agesic.direcciones.utils.GeocodingUtils;

@Data
@Builder(toBuilder=true)
@AllArgsConstructor
@NoArgsConstructor
public class GeocoderResult implements Comparable {

	private EntradaNormalizada.TipoDirec type;
	private String id;
	private String address;
	private int idCalle;
	private String nomVia;
	private String postalCode;
	private int idLocalidad;
	private String localidad;
	private int idDepartamento;
	private String departamento;
	private Integer manzana;
	private Integer solar;
	private String inmueble;
	private int idCalleEsq;
	private double km;
	private int priority;

	private String geom; // geojson format. Maybe a point, line or polygon
	private String tip_via;
	private double lat;
	private double lng;
	private int portalNumber;
	private String letra = "";
	private String stateMsg = "";
	private String source="ide_uy"; // Used in gvsig online
	private double ranking;
	
	// Estados:
	// 1 - OK
	// 2 - OK con adaptaci�n de los datos
	// 4 - Con errores
	private int state = 1;
	
	public void setPortalNumber(int portal) {
		portalNumber = portal;
		
//		updateAddress();
	}

//	private void updateAddress() {
//		if (portalNumber >= 0)
//		{
//			if (inmueble != null) {
//				address = inmueble + " - " + GeocodingUtils.getAddress(nomVia, portalNumber, letra, localidad, departamento);
//			}
//			else
//				address = GeocodingUtils.getAddress(nomVia, portalNumber, letra, localidad, departamento);
//		}
//	}

	public void setKm(double km) {
		this.km = km;
//		address = GeocodingUtils.getAddressRuta(nomVia, km, localidad, departamento);
	}
	
	public void setInmueble(String inmueble) {
		this.inmueble = inmueble;
//		updateAddress();
	}

	@Override
	public int compareTo(Object arg0) {
		GeocoderResult r = (GeocoderResult) arg0;
		if (r == null) return 1;
		if (r.ranking < ranking)
			return -1;
		if (r.ranking > ranking)
			return +1;
		return 0;			
	}

    
}
