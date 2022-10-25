
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
 */
@Component
public class GeocodeRowMapperPortalPK implements RowMapper<GeocoderResult> {
    @Override
    public GeocoderResult mapRow(ResultSet row, int index) throws SQLException {
        GeocoderResult result = new GeocoderResult();
        result.setId(row.getString("id"));

        String nombre = row.getString("nombre");
        String localidad = row.getString("localidad");
        String departamento = row.getString("departamento");
        String inmueble = row.getString("nombre_inmueble");
        int numero = row.getInt("numero");
        String letra = row.getString("letra");
//        String solar = row.getString("solar");
        String direc = GeocodingUtils.getAddress(nombre, numero, letra, localidad, departamento);
//        if (null != inmueble) {
//        	if (direc != "")
//        		direc = inmueble + " - " + direc;
//        	else
//        		direc = inmueble;
//        }
        if (numero >= 0) {
        	result.setType(EntradaNormalizada.TipoDirec.CALLEyPORTAL);
        }
        else {
        	result.setType(TipoDirec.CALLE);
        }
        if ((null != inmueble) && (!inmueble.equals(""))) {
        	result.setType(TipoDirec.POI);
        	result.setInmueble(inmueble);
        }
//        if (null != solar) {
//        	result.setType(TipoDirec.MANZANAySOLAR);
//        }
        	
        result.setAddress(direc);
        result.setNomVia(nombre);
        result.setIdCalle(row.getInt("idcalle"));
        result.setLetra(letra); // PONER LETRA ANTES QUE PORTAL NUMBER
        result.setPortalNumber(numero);
        
        result.setPostalCode(row.getString("cp"));
        result.setIdLocalidad(row.getInt("idLocalidad"));
        result.setIdDepartamento(row.getInt("idDepartamento"));
        result.setLocalidad(row.getString("localidad"));
        result.setDepartamento(departamento);
        
        
        
//        result.setManzana(row.getString("manzana"));
//        result.setSolar(solar);
//        result.setInmueble(inmueble);
//        result.setProvince(row.getString("country"));

        Double longitud = row.getDouble("longitud");
        Double latitud = row.getDouble("latitud");
        result.setLng(longitud);
        result.setLat(latitud);
        String locCode = GeocodingUtils.latlngToOLC(latitud, longitud);
        result.setOLC(locCode);

        // CALCULAMOS OpenLocationCode y RECUPERAMOS el guardado para devolver los dos
        // Es porque puede que hayan asignado un código, y el usuario se ha cambiado de sitio.
        
        
//        result.setRanking(row.getDouble("ranking"));

        return result;
    }
}
