package uy.agesic.geocoder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.SugerenciaResultV0;

public class GeocodeV0 implements IGeocodeV0 {
	
	private String apiUrl;
	private RestTemplate restTemplate = new RestTemplate();
	
	
	public GeocodeV0(String apiUrl) {
		this.apiUrl = apiUrl + "/v0/";
	}

	public List<GeocoderResultV0> getBusquedaDireccion(String nomvia, String localidad,
			String departamento) {
		String url = apiUrl + "/geocode/BusquedaDireccion?calle=" + nomvia +  "&localidad=" + localidad +
				"&departamento=" + departamento;
		ResponseEntity<GeocoderResultV0[]> res = restTemplate.getForEntity(url, GeocoderResultV0[].class);
		List<GeocoderResultV0> result = Arrays.asList(res.getBody());
		return result;

	}

	public List<SugerenciaResultV0> getSugerenciaCalleCompleta(String entrada, boolean todos) {
		String url = apiUrl + "/geocode/SugerenciaCalleCompleta?entrada=" + entrada +  "&todos=" + todos;
		ResponseEntity<SugerenciaResultV0[]> res = restTemplate.getForEntity(url, SugerenciaResultV0[].class);
		List<SugerenciaResultV0> result = Arrays.asList(res.getBody());
		return result;
	}

	public List<LocalidadResultV0> getLocalidades(String departamento, boolean alias) {
		String url = apiUrl + "/geocode/localidades?entrada=" + departamento +  "&alias=" + alias;
		ResponseEntity<LocalidadResultV0[]> res = restTemplate.getForEntity(url, LocalidadResultV0[].class);
		List<LocalidadResultV0> result = Arrays.asList(res.getBody());
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

}
