
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.TramoResult;

/**
 * Maps a sql ResultSet to a CapaPoligonalResult. Se usan para obtener los polígonos con los que buscar dentro direcciones.
 */
@Component
public class GeocodeRowMapperTramo implements RowMapper<TramoResult> {
	
	@Override
    public TramoResult mapRow(ResultSet row, int index) throws SQLException {
		TramoResult result = new TramoResult();

		result.setGid(row.getLong("gid"));
        result.setIdcalle(row.getInt("idcalle"));
        result.setFuente_id(row.getInt("fuente_id"));
        result.setTipo_vialidad_id(row.getInt("tipo_vialidad_id"));
        result.setGeom(row.getString("geojson"));
        return result;
    }
    
    
}
