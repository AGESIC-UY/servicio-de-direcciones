
package uy.agesic.direcciones.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.openlocationcode.OpenLocationCode;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.utils.GeocodingUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a sql ResultSet to a GeocoderResult. Solo para RutaYKm
 * DEVUELVE fraccion, idcalle, nombreruta, p4326, localidad, departamento
 */
@Component
public class GeocodeRowMapperRutaYkm implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        String nombre = row.getString("nombreruta");
        String idcalle = row.getString("idcalle"); // OJO, ESTO NO ES IDCALLE, ES EL id DE tramo_ruta (es por la continuidad de lo que hay ahora mismo, que nos es bueno.
        Integer idLocalidad = row.getInt("idLocalidad");
        Integer idDepartamento = row.getInt("idDepartamento");
        
       	result.setType(TipoDirec.RUTAyKM);

        		
        result.setId(idcalle);
        result.setIdCalle(Integer.parseInt(idcalle));
        result.setAddress(nombre);
        result.setNomVia(nombre);

        Double longitud = row.getDouble("longitud");
        Double latitud = row.getDouble("latitud");
        result.setLng(longitud);
        result.setLat(latitud);
        String locCode = GeocodingUtils.latlngToOLC(latitud, longitud);
        result.setOLC(locCode);
        
        result.setIdLocalidad(idLocalidad);
        result.setLocalidad(row.getString("localidad"));
        result.setIdDepartamento(idDepartamento);
        result.setDepartamento(row.getString("departamento"));
        // result.setInmueble(null);
        result.setPostalCode(null);
//        result.setRanking(0);

        return result;
    }
    
    
}
