package uy.agesic.geocoder;

import java.util.List;

import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.SugerenciaResultV0;

public interface IGeocodeV0 {

	List<GeocoderResultV0> getBusquedaDireccion(String nomvia, String localidad, String departamento);

	List<SugerenciaResultV0> getSugerenciaCalleCompleta(String entrada, boolean todos);

	List<LocalidadResultV0> getLocalidades(String departamento, boolean alias);

	List<GeocoderResult> getReverseGeocoding(double lat, double lng, int limit);

}