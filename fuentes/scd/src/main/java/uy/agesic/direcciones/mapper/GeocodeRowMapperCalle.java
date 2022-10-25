
package uy.agesic.direcciones.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.utils.GeocodingUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a sql ResultSet to a GeocoderResult. Solo para Calles
 */
@Component
public class GeocodeRowMapperCalle implements RowMapper<GeocoderResult> {
	
	@Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
//        result.setId(row.getString("id"));
        String nombre = row.getString("nombre");
        String localidad = row.getString("localidad");
        String departamento = row.getString("departamento");
//        String inmueble = row.getString("nombre_inmueble");
        String direc = GeocodingUtils.getAddress(nombre, localidad, departamento);
//        String solar = row.getString("solar");
//        String ruta = row.getString("ruta");

        String idcalle = row.getString("idcalle");
//        if (null != inmueble) {
//        	if (direc != "")
//        		direc = inmueble + " - " + direc;
//        	else
//        		direc = inmueble;
//        }
        
       	result.setType(TipoDirec.CALLE);
//        if (null != inmueble) {
//        	result.setType(TipoDirec.POI);
//        }

        		
        result.setId(idcalle);
        result.setIdCalle(Integer.parseInt(idcalle));
        result.setAddress(direc);
        result.setNomVia(nombre);
        result.setLocalidad(localidad);
        result.setDepartamento(departamento);
        result.setIdLocalidad(row.getInt("idLocalidad"));
        result.setIdDepartamento(row.getInt("idDepartamento"));
        result.setRanking(row.getDouble("ranking"));

//        result.setInmueble(inmueble);
//        result.setPostalCode(row.getString("cp"));
        
        return result;
    }
    
    
}
