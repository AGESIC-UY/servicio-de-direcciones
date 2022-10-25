
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.LocalidadResultV0;

/**
 * Maps a sql ResultSet to a GeocoderResult. Solo para LocalidadesV0
 */
@Component
public class GeocodeRowMapperLocalidadV0 implements RowMapper<LocalidadResultV0> {
    @Override
    public LocalidadResultV0 mapRow(ResultSet row, int index) throws SQLException {
        LocalidadResultV0 result = new LocalidadResultV0();
        result.setId(row.getInt("id"));
        result.setNombre(row.getString("nombre"));
        result.setCodigoPostal(row.getInt("codigoPostal"));
        		
        return result;
    }
    
    
}
