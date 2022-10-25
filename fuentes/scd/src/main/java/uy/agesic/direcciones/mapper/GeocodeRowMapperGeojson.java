
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * Maps a sql ResultSet to a GeocoderResult. Solo para Calles
 */
@Component
public class GeocodeRowMapperGeojson implements RowMapper<String> {
	
	@Override
    public String mapRow(ResultSet row, int index) throws SQLException {
        String geojson = row.getString("geojson");
        return geojson;
    }
    
    
}
