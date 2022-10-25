package uy.agesic.direcciones.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import uy.agesic.direcciones.data.CapaPoligonalResult;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.TramoResult;

public interface IGeocodeV1 {

	ResponseEntity<List<GeocoderResult>> getCandidates(String query, boolean onlyLocalidad, int limit);

	ResponseEntity<List<GeocoderResult>> getAddress(String type, String idcalle, String nomvia, String localidad,
			String portal, String departamento, String manzana, String solar, String inmueble, String ruta, String km,
			String letra,
			String idcalle2);

	ResponseEntity<List<GeocoderResult>> getCornersOfStreet(int idcalle);

	ResponseEntity<List<GeocoderResult>> getCruces(String departamento, String localidad, String calle, String filtro);

	ResponseEntity<List<GeocoderResult>> getCrucesPorIdCalle(int idcalle, String filtro);

	ResponseEntity<List<GeocoderResult>> getCrucesConRadio(String departamento, String localidad, String calle,
			double latitud, double longitud, Double radio, String filtro);

	ResponseEntity<List<GeocoderResult>> getRutaKm(String ruta, Double km);

	List<GeocoderResult> getReverseGeocoding(double lat, double lng, int limit);

	ResponseEntity<List<GeocoderResult>> fuzzyGeocode(String query, boolean onlyLocalidad, int limit);

	ResponseEntity<List<GeocoderResult>> direcEnPoligono(String poligono, String tipoDirec, int limit);

	ResponseEntity<List<GeocoderResult>> poligono(String capa, int id, String tipoDirec, int limit);

	ResponseEntity<List<CapaPoligonalResult>> getCapasPoligonales();

	ResponseEntity<List<GeocoderResult>> getDirecManzaSolar(String departamento, String localidad, int manzana,
			int solar, int limit);

	ResponseEntity<List<GeocoderResult>> getDirecPadron(String departamento, String localidad, int padron, int limit);

	ResponseEntity<List<GeocoderResult>> getDirecPuntoNotable(String departamento, String poi, int limit);

	ResponseEntity<List<GeocoderResult>> direcUnica(String query, int limit);
	
	ResponseEntity<String> getTramos(int idCalle);

}