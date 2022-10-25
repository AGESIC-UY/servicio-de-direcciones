
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.TipoVialidad;

@Component
public class TipoVialidadRowMapper implements RowMapper<TipoVialidad> {
    @Override
    public TipoVialidad mapRow(ResultSet row, int index) throws SQLException {
        TipoVialidad tv = new TipoVialidad();
        int id = row.getInt("id");
        String nombre = row.getString("nombre");
        tv.setId(id);
        tv.setNombre(nombre);

        return tv;
    }
    
    
}
