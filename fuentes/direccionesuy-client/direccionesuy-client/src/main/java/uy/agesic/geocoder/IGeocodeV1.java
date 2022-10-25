package uy.agesic.geocoder;

import java.util.List;

import uy.agesic.direcciones.data.CapaPoligonalResult;
import uy.agesic.direcciones.data.GeocoderResult;

public interface IGeocodeV1 {

	List<GeocoderResult> getCandidates(String query, boolean onlyLocalidad, int limit);

	List<GeocoderResult> getAddress(String type, String idcalle, String nomvia, String localidad,
			String portal, String departamento, String manzana, String solar, String inmueble, String ruta, String km,
			String letra,
			String idcalle2);

	List<GeocoderResult> getCornersOfStreet(int idcalle);

	List<GeocoderResult> getCruces(String departamento, String localidad, String calle, String filtro);

	List<GeocoderResult> getCrucesPorIdCalle(int idcalle, String filtro);

	List<GeocoderResult> getCrucesConRadio(String departamento, String localidad, String calle,
			double latitud, double longitud, Double radio, String filtro);

	List<GeocoderResult> getRutaKm(String ruta, Double km);

	List<GeocoderResult> getReverseGeocoding(double lat, double lng, int limit);

	List<GeocoderResult> fuzzyGeocode(String query, boolean onlyLocalidad, int limit);

	List<GeocoderResult> direcEnPoligono(String poligono, String tipoDirec, int limit);

	List<GeocoderResult> poligono(String capa, int id, String tipoDirec, int limit);

	List<CapaPoligonalResult> getCapasPoligonales();

	List<GeocoderResult> getDirecManzaSolar(String departamento, String localidad, int manzana,
			int solar, int limit);

	List<GeocoderResult> getDirecPadron(String departamento, String localidad, int padron, int limit);

	List<GeocoderResult> getDirecPuntoNotable(String departamento, String poi, int limit);
	
	List<GeocoderResult> direcUnica(String query, int limit);

}