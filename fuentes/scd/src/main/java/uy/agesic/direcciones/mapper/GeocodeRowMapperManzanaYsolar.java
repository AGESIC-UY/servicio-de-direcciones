
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
 * Maps a sql ResultSet to a GeocoderResult. Solo para Calles
 */
@Component
public class GeocodeRowMapperManzanaYsolar implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        result.setId(row.getString("id"));
        // String nombre = row.getString("nombre");
        String localidad = row.getString("localidad");
        String departamento = row.getString("departamento");
        Integer manzana = row.getInt("manzana");
        String auxSolar = row.getString("solar");
        int solar = 0;
        try {
        	solar = Integer.parseInt(auxSolar);
        }
        catch (NumberFormatException e) {
        	System.out.println("Error: campo solar nulo en localidad " + localidad + " manzana " + manzana );
        }

        String strDirec = "MANZANA " + manzana + " SOLAR " + solar;
        String direc = GeocodingUtils.getAddress(strDirec, localidad, departamento);
        // int idcalle = row.getInt("solar_id");
        int idcalle = row.getInt("idcalle");
        
       	result.setType(TipoDirec.MANZANAySOLAR);
        		
        result.setIdCalle(idcalle); // es solar_id en realidad
        result.setAddress(direc);
        result.setNomVia(strDirec);
        result.setLocalidad(localidad);
        result.setDepartamento(departamento);
        result.setIdDepartamento(row.getInt("idDepartamento"));
        result.setIdLocalidad(row.getInt("idLocalidad"));
        result.setManzana(manzana);
        result.setSolar(solar);
        result.setPostalCode(row.getString("cp"));

        Double longitud = row.getDouble("longitud");
        Double latitud = row.getDouble("latitud");
        result.setLng(longitud);
        result.setLat(latitud);
        String locCode = GeocodingUtils.latlngToOLC(latitud, longitud);
        result.setOLC(locCode);


        //        result.setLat(row.getDouble("lat"));
//        result.setLng(row.getDouble("lng"));

        return result;
    }
    
    
}
