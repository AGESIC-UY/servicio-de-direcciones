
package uy.agesic.direcciones.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.openlocationcode.OpenLocationCode;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.utils.GeocodingUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a sql ResultSet to a GeocoderResult.
 * Esta clase es un poco especial. Buscamos rellenar los datos de la ESQUINA, no de la calle que originó
 * la consulta de esquina. Así que en idcalle metemos el campo idcalleEsq, y lo mismo con el nombre.
 */
@Component
public class GeocodeRowMapperEsquina implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        result.setId(row.getString("idcalleEsq"));
        String nombre = row.getString("nombre");
        String nombre2 = row.getString("nombreEsq");
        String localidad = row.getString("localidad");
        String departamento = row.getString("departamento");
        String direc = GeocodingUtils.getAddress(nombre2, localidad, departamento);
       	result.setType(TipoDirec.ESQUINA);

        result.setIdCalle(row.getInt("idcalleEsq"));
//        result.setIdCalleEsq(row.getInt("idcalleEsq"));
        
        result.setNomVia(nombre);
        result.setAddress(nombre + " ESQ " + direc);
//        result.setPostalCode(row.getString("cp"));
        result.setLocalidad(row.getString("localidad"));
        result.setDepartamento(departamento);

        Double longitud = row.getDouble("longitud");
        Double latitud = row.getDouble("latitud");
        String locCode = GeocodingUtils.latlngToOLC(latitud, longitud);
        result.setOLC(locCode);

        result.setLng(longitud);
        result.setLat(latitud);

        return result;
    }
}
