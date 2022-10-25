package uy.agesic.direcciones.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiOperation;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.SugerenciaResultV0;

public interface IGeocodeV0 {

	ResponseEntity<List<GeocoderResultV0>> getBusquedaDireccion(String nomvia, String localidad, String departamento);

	ResponseEntity<List<SugerenciaResultV0>> getSugerenciaCalleCompleta(String entrada, boolean todos);

	ResponseEntity<List<LocalidadResultV0>> getLocalidades(String departamento, boolean alias);

	List<GeocoderResult> getReverseGeocoding(double lat, double lng, int limit);

}