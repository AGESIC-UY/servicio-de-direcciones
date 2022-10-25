
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.CapaPoligonalResult;

/**
 * Maps a sql ResultSet to a CapaPoligonalResult. Se usan para obtener los polígonos con los que buscar dentro direcciones.
 */
@Component
public class GeocodeRowMapperCapaPoligonal implements RowMapper<CapaPoligonalResult> {
	
	@Override
    public CapaPoligonalResult mapRow(ResultSet row, int index) throws SQLException {
		CapaPoligonalResult result = new CapaPoligonalResult();

		result.setTabla(row.getString("tabla").trim());
        result.setDescripcion(row.getString("descripcion").trim());
        result.setCampo_id(row.getString("campo_id").trim());
        result.setCampoGeom(row.getString("campo_geom").trim());
                
        return result;
    }
    
    
}
