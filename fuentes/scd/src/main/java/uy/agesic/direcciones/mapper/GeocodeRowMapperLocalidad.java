
package uy.agesic.direcciones.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.openlocationcode.OpenLocationCode;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.utils.GeocodingUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a sql ResultSet to a GeocoderResult. Solo para Localidades
 */
@Component
public class GeocodeRowMapperLocalidad implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        result.setId(row.getString("id"));
        String localidad = row.getString("localidad");
        String departamento = row.getString("departamento");
        String direc = GeocodingUtils.getAddressLocalidad(localidad, departamento);
        
       	result.setType(EntradaNormalizada.TipoDirec.LOCALIDAD);
        		
        result.setAddress(direc);
        result.setLocalidad(localidad);
        result.setDepartamento(departamento);
        result.setPostalCode(row.getString("cp"));
        result.setIdLocalidad(row.getInt("idLocalidad"));
        result.setIdDepartamento(row.getInt("idDepartamento"));

        Double longitud = row.getDouble("longitud");
        Double latitud = row.getDouble("latitud");
        result.setLng(longitud);
        result.setLat(latitud);
        String locCode = GeocodingUtils.latlngToOLC(latitud, longitud);
        result.setOLC(locCode);


        return result;
    }
    
    
}
