
package uy.agesic.direcciones.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import uy.agesic.direcciones.data.Sinonimo;

@Component
public class SinonimosRowMapper implements RowMapper<Sinonimo> {
    @Override
    public Sinonimo mapRow(ResultSet row, int index) throws SQLException {
        Sinonimo s = new Sinonimo();
        String palabra = row.getString("palabra");
        String sinonimo = row.getString("sinonimo");
        		
        s.setPalabra(palabra);
        s.setSinonimo(sinonimo);
        s.setId(row.getString("id"));
        s.setIdSinonimo(row.getString("idSinonimo"));

        return s;
    }
    
    
}
