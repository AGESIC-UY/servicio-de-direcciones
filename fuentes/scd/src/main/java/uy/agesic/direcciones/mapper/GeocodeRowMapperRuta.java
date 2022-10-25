
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
 * Maps a sql ResultSet to a GeocoderResult. Solo para Rutas
 * Obtenemos el idcalle que necesitamos para hacer la siguiente consulta e
 * interpolar el KM.
 */
@Component
public class GeocodeRowMapperRuta implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        String nombre = row.getString("nombre");
        String idcalle = row.getString("id");
        
       	result.setType(TipoDirec.RUTAyKM);

        		
        result.setId(row.getString("ruta_id"));
        result.setIdCalle(Integer.parseInt(idcalle));
        result.setAddress(nombre);
        result.setNomVia(nombre);
        result.setLocalidad(null);
        result.setDepartamento(null);
//        result.setInmueble(null);
        result.setPostalCode(null);
//        result.setRanking(0);

        return result;
    }
    
    
}
