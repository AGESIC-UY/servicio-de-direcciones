package uy.agesic.geocoder;

import org.junit.Test;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.utils.GeocodingUtils;

public class UnitTest {
	
	@Test
	public void TestParseWithCodPostalInLocalidad() {
		String q = "Juan Alvarez 8032, 33000 Treinta y Tres, Departamento de Treinta y Tres, Uruguay,Treinta y Tres ,Treinta y Tres";
		EntradaNormalizada en = GeocodingUtils.parseAddress(q);	  
		System.out.println(en.getDebugString());
	}

}
