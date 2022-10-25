package es.scolab.geocoder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import uy.agesic.direcciones.controller.GeocodeControllerV0;
import uy.agesic.direcciones.controller.IGeocodeV1;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.repository.GeocodeRepository;
import uy.agesic.direcciones.utils.GeocodingUtils;


// INTEGRATION TESTS
// Vamos a comprobar que los resultados tienen sentido. La pega es que usamos la base de datos, y todo lo demás, pero creo que es más útil que hacer mocks
@RunWith(SpringRunner.class)
@SpringBootTest(classes = uy.agesic.direcciones.GeocoderApplication.class)
@Ignore
public class GeocoderApplicationTests {

	@Autowired
	private GeocodeControllerV0 cV0;

	@Autowired
	private IGeocodeV1 cV1;

    @Autowired
    private GeocodeRepository geocodeRepository;

	@Test
	public void contextLoads() {
	}
	
	@Test
	public void testUsarIndice() {
		long t1 = System.currentTimeMillis();
		String sql = "EXPLAIN SELECT \n" + 
				"  c.idcalle,\n" + 
				"  c.nombre,\n" + 
				"  c.idLocalidad, \n" + 
				"  c.localidad,\n" + 
				"  c.departamento,\n" + 
				"  c.idDepartamento, \n" + 
				"  '' as nombre_inmueble, \n" + 
				"  ts_rank(c.fulltext, plainto_tsquery('spanish', '[NOMBRE]')) as ranking \n" + 
				"FROM budu.mv_direcciones2 c \n" + 
				 "  left join budu.alias_departamento a on c.iddepartamento = a.departamento_id \n" + 
				 "  left join budu.alias_localidad_geo l on c.idlocalidad = l.localidad_id \n" + 
				" WHERE ( immutable_unaccent(c.nombre) ilike unaccent('%estadio centena%') \n" +
				// " WHERE ( c.idcalle = 17593 \n" +
				//"WHERE ( c.alias % 'estadio centena' \n" +
				 " OR immutable_unaccent(c.nombre) % unaccent('estadio centena') \n" + 
				 " OR immutable_unaccent(c.aliasbuscar) % unaccent('ESTADIO CENTENA') \n" + 
				")  \n" + 
				"ORDER BY ranking DESC LIMIT 100; ";
		
		geocodeRepository.testSql(sql);
		
		long t2 = System.currentTimeMillis();
		System.out.println("T=" + (t2-t1) + " msecs");
		assertTrue((t2-t1) < 200);
		
	}

	@Test
	public void testGeocodeControllerV0() {
		String q1 = "Ordoñez";
		EntradaNormalizada n = GeocodingUtils.parseAddress(q1);
		ResponseEntity<List<GeocoderResultV0>> result = cV0.getBusquedaDireccion(n.getNomVia(), n.getLocalidad(), n.getDepartamento());
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		System.out.println(q1 + " : " + result.getBody().get(0).toString());
		
		
		q1 = "COLMAN 4625, MONTEVIDEO, MONTEVIDEO";
		n = GeocodingUtils.parseAddress(q1);
		result = cV0.getBusquedaDireccion(n.getNomVia(), n.getLocalidad(), n.getDepartamento());
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		System.out.println(q1 + " : " + result.getBody().get(0).toString());
		
	}

	@Test
	public void testGeocodeControllerV1() {
		
		String q1 = "GRAL. GARIBALDI 519, MELO, CERRO LARGO";
		ResponseEntity<List<GeocoderResult>> result = cV1.getCandidates(q1, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);
		
		
		q1 = "Ordoñez";
		result = cV1.getCandidates(q1, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);
		
		q1 = "CESAR MAYO GUTIERREZ 39, LA PAZ, CANELONES";
		result = cV1.getCandidates(q1, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);		
		
	}
	
	@Test
	public void testGeocodeManzanaSolar() {
        String q = "MANZANA 243 SOLAR 20, PAYSANDU, PAYSANDU";
		ResponseEntity<List<GeocoderResult>> result = cV1.getCandidates(q, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);
		System.out.println("Buscando Manzana y Solar: " + q + "\n" + result.toString());

	}
	
	@Test
	public void testGeocodeRutaKm() {
		String q = "RUTA 5 KM. 23, TOLEDO, CANELONES";
		ResponseEntity<List<GeocoderResult>> result = cV1.getCandidates(q, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);		
	}
	
	@Test
	public void testGeocodeEsquina() {
		String q = "convención esquina mercedes, Montevideo, Montevideo";
		ResponseEntity<List<GeocoderResult>> result = cV1.getCandidates(q, false, 20);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);		
		System.out.println("Buscando Por Esquina: " + q + "\n" + result.toString());
	}
	
	@Test
	public void testGeocodeLocalidadV0() {
		String q = "Montevideo";
		ResponseEntity<List<LocalidadResultV0>> result = cV0.getLocalidades(q, false);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);		
		System.out.println("Buscando Localidades sin Alias: " + q + "\n" + result.toString());
		
		result = cV0.getLocalidades(q, true);
		assertEquals(result.getStatusCodeValue(), HttpStatus.OK.value());
		assertTrue(result.getBody().size() > 0);		
		System.out.println("Buscando Localidades con Alias: " + q + "\n" + result.toString());

	}


	
//	ID|DEPARTAMENTO|LOCALIDAD|DIRECCION|YOUTRACK|OBSERVACIONES|TIPO PRUEBA
//	1|MONTEVIDEO|MONTEVIDEO|3(MILLAN Y LECOCQ) 853 A.210|ver observaciones||Entidad Colectiva
//	2|MONTEVIDEO|MONTEVIDEO|3 873 A.309 ESQ.COMPLEJO MILLAN Y LECOCQ|ver observaciones||Calle - Nro
//	3|MONTEVIDEO|MONTEVIDEO|6 824 ALTO.107 - COMPLEJO MILLAN Y LECOCQ|youtrack tsubasa 19||Calle - Nro
	@Test  
	@Ignore
	public void testFile() throws IOException {
	    String path = "./src/test/resources";
	    String fileName = "entradas.txt";

	    File file = new File(path, fileName);

	    assertTrue(file.exists());
	    	    
	    FileReader fileReader = new FileReader(file);
	    
	    TestRestTemplate testRemote = new TestRestTemplate();
	    String remoteUrl = "http://servicios.ide.gub.uy/servicios/BusquedaDireccion?";
	    
    	CSVReader csvReader = new CSVReader(fileReader, '|');
        String[] values = null;
        int i = 0;
        int numEncontrados = 0;
        int numEncontradosV0 = 0;
        int numEncontradosAgesic = 0;

	    String fileName2 = "test.csv";
	    File fileCsv = new File(path, fileName2);

        FileWriter fileWriter = new FileWriter(fileCsv);
        CSVWriter csvWriter = new CSVWriter(fileWriter);

        while ((values = csvReader.readNext()) != null) {
        	i++;
        	if (i == 1)
        		continue;
            boolean foundA = false, foundB = false, foundC = false;
        	String nomVia = values[3];
        	String localidad = values[2];
        	String departamento = values[1];
        	String q = nomVia + ", " + localidad + ", " + departamento;
        	String youtrack = values[4];
        	String observaciones = values[5];
        	String tipoPrueba = values[6];
        	EntradaNormalizada en = GeocodingUtils.parseAddress(q);
            System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec() +
            		" \ntipoAgesic:" + tipoPrueba + " observaciones:" + observaciones + " youtrack:" + youtrack);
            System.out.println(en.getDebugString());
        	
            System.out.println("Geocodificando " + q);
            long t1 = System.currentTimeMillis();
    		ResponseEntity<List<GeocoderResult>> result = cV1.getCandidates(q, false, 10);
    		long t2 = System.currentTimeMillis();
    		long tA = t2-t1;
    		String best1 = "NO ENCONTRADO";
    		String best2 = "NO ENCONTRADO";
    		String best3 = "NO ENCONTRADO";
            if (result.getBody().size() == 0)
            {
            	System.err.println("No se ha encontrado nada.");
            }	
            else
            {
            	System.out.println("Mejor candidato:" + result.getBody().get(0));
            	numEncontrados++;
            	foundA = true;
            	best1 = result.getBody().get(0).toString();
            }
            
            ResponseEntity<List<GeocoderResultV0>> result2 = cV0.getBusquedaDireccion(nomVia, localidad, departamento);
            long t3 = System.currentTimeMillis();
            long tB = t3 - t2;
            if (result2.getBody().size() == 0)
            {
            	System.err.println("No se ha encontrado nada con BusquedaDireccion.");
            }
            else
            {
            	System.out.println("Mejor candidato BusquedaDireccion:" + result2.getBody().get(0));
            	numEncontradosV0++;
            	foundB = true;
            	best2 = result2.getBody().get(0).toString();
            }
            
            String request = remoteUrl + "departamento=" + departamento +
    	    		"&localidad=" + localidad + "&calle=" + nomVia;
            System.out.println("Request: " + request);
            // System.err.println("POR AHORA DESHABILITO LA LLAMADA A AGESIC");
    	    String resRemote = testRemote.getForObject(request, String.class);
    	    long tC = System.currentTimeMillis() - t3;
    	    System.out.println(resRemote);
    	    
    	    if (resRemote == null) {
    	    	System.err.println("No se ha encontrado en Agesic");
    	    }
    	    else
    	    {
    	    	if (resRemote.equals("[]"))
    	    		System.err.println("No se ha encontrado en Agesic");
    	    	else
    	    	{
		    	    ObjectMapper objMapper = new ObjectMapper();
		    	    JsonNode node = objMapper.readTree(resRemote);
		    	    JsonNode nodeAux = node.findValue("error");
		    	    if (nodeAux.asText() != "") {
		    	    	System.err.println("No se ha encontrado en Agesic");
		    	    }
		    	    else
		    	    {
		    	    	System.out.println("Encontrado en Agesic: " + node.toString());
		    	    	numEncontradosAgesic++;
		    	    	foundC = true;
		    	    	best3 = node.get(0).toString();
		    	    }
    	    	}
    	    }            
    	    String str = q + ";" + tipoPrueba + ";" + youtrack + ";" + observaciones + ";" 
    	    		+ foundA + ";" + tA + ";" + foundB + ";" + tB + ";" + foundC + ";" + tC + ";" +
    	    		best1 + ";" + best2 + ";" + best3 +"\r\n" ;
    	    System.out.println(str);
    	    byte[] ptext = str.getBytes(ISO_8859_1); 
    	    String strUTF8 = new String(ptext, UTF_8); 
    	    fileWriter.write(strUTF8);

        }
        fileWriter.close();
        System.out.println("NumEncontrados: " + numEncontrados + " V0: " + numEncontradosV0 + " Agesic: " + numEncontradosAgesic);        
	    
	}

}
