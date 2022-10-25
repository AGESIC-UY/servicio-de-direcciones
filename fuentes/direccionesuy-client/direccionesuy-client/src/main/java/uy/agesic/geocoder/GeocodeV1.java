package uy.agesic.geocoder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import uy.agesic.direcciones.data.CapaPoligonalResult;
import uy.agesic.direcciones.data.GeocoderResult;

public class GeocodeV1 implements IGeocodeV1 {
	
	private String apiUrl;
	
    RestTemplate restTemplate = new RestTemplate();
	

	
	/**
	 * Example: https://callejerouy-direcciones.agesic.gub.uy/api
	 * En la clase se le a�ade el "v1"
	 * @param apiUrl
	 */
	public GeocodeV1(String apiUrl) {
		super();
		this.apiUrl = apiUrl + "/v1/";
	}

	public List<GeocoderResult> getCandidates(String query, boolean onlyLocalidad, int limit) {
		String url = apiUrl + "/geocode/candidates?q=" + query + "&onlyLocalidad=" + onlyLocalidad + "&limit=" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getAddress(String type, String idcalle, String nomvia, String localidad,
			String portal, String letra, String departamento, String manzana, String solar, String inmueble, String ruta, String km,
			String idcalle2) {
		
		String url = apiUrl + "/geocode/find?type=" + type + "&idcalle=" + idcalle + "&nomvia=" + nomvia
				+ "&localidad=" + localidad + "&portal=" + portal + "&letra=" + letra + "&departamento=" + departamento + "&manzana=" + manzana
				+ "&solar=" + solar + "&inmueble=" + inmueble + "&ruta=" + ruta + "&km=" + km + "&idcalle2=" + idcalle2;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;

	}

	public List<GeocoderResult> getCornersOfStreet(int idcalle) {
		String url = apiUrl + "/geocode/findEsq?idcalle=" + idcalle;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getCruces(String departamento, String localidad, String calle,
			String filtro) {
		String url = apiUrl + "/geocode/cruces?departamento=" + departamento + "&localidad=" + localidad 
				+ "&calle=" + calle + "&q=" + filtro;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getCrucesPorIdCalle(int idcalle, String filtro) {
		String url = apiUrl + "/geocode/cruces?idcalle=" + idcalle + "&q=" + filtro;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getCrucesConRadio(String departamento, String localidad, String calle,
			double latitud, double longitud, Double radio, String filtro) {
		String url = apiUrl + "/geocode/crucesConRadio?departamento=" + departamento + "&localidad=" + departamento
				+"&calle" + calle + "&latitud=" + latitud + "&longitud" + longitud + "&radio=" + radio 
				+"&q" + filtro;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getRutaKm(String ruta, Double km) {
		String sKm = String.format(Locale.US, "%.4f", km);
		String url = apiUrl + "/geocode/rutakm?ruta=" + ruta + "&km=" + sKm;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;

	}

	public List<GeocoderResult> getReverseGeocoding(double lat, double lng, int limit) {
		String sLat = String.format(Locale.US, "%.6f", lat);
		String sLon = String.format(Locale.US, "%.6f", lng);
		String url = apiUrl + "/geocode/reverse?latitud=" + sLat + "&longitud=" + sLon + "&limit=" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> fuzzyGeocode(String query, boolean onlyLocalidad, int limit) {
		String url = apiUrl + "/geocode/fuzzyGeocode?q=" + query + "&soloLocalidad=" + onlyLocalidad +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> direcEnPoligono(String poligono, String tipoDirec, int limit) {
		String url = apiUrl + "/geocode/direcEnPoligono?poligono=" + poligono + "&tipoDirec=" + tipoDirec +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> poligono(String capa, int id, String tipoDirec, int limit) {
		String url = apiUrl + "/geocode/poligono?capa=" + capa + "&id=" + id + "&tipoDirec=" + tipoDirec +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<CapaPoligonalResult> getCapasPoligonales() {
		String url = apiUrl + "/geocode/capasPoligonales";
		ResponseEntity<CapaPoligonalResult[]> res = restTemplate.getForEntity(url, CapaPoligonalResult[].class);
		List<CapaPoligonalResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getDirecManzaSolar(String departamento, String localidad, int manzana,
			int solar, int limit) {
		String url = apiUrl + "/geocode/direcManzaSolar?departamento=" + departamento + "&localidad=" + localidad +
				"&manzana=" + manzana + "&solar=" + solar +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getDirecPadron(String departamento, String localidad, int padron,
			int limit) {
		String url = apiUrl + "/geocode/direcPadron?departamento=" + departamento + "&localidad=" + localidad +
				"&padron=" + padron +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> getDirecPuntoNotable(String departamento, String poi, int limit) {
		String url = apiUrl + "/geocode/direcPuntoNotable?departamento=" + departamento + "&nombre=" + poi +"&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<GeocoderResult> direcUnica(String q, int limit) {
		String url = apiUrl + "/geocode/direcUnica?q=" + q + "&limit" + limit;
		ResponseEntity<GeocoderResult[]> res = restTemplate.getForEntity(url, GeocoderResult[].class);
		List<GeocoderResult> result = Arrays.asList(res.getBody());
		return result;
	}

}
