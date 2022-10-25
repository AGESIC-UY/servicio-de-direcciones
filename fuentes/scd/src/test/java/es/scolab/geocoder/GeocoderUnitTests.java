package es.scolab.geocoder;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.junit.Test;

import com.opencsv.CSVReader;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.utils.GeocodingUtils;


// UNIT TESTS
// Vamos a hacer pruebas con el matching de direcciones, para saber si acertamos con el tipo de dirección
public class GeocoderUnitTests {

	@Test
	public void contextLoads() {
	}
	
	
	@Test
	public void testRutaKm() {
		String q = "RUTA 6 KM. 23.5, TOLEDO, CANELONES";
		String tipoAgesic = "Ruta - Km";
    	EntradaNormalizada en = GeocodingUtils.parseAddress(q);
        System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec() +
        		" \ntipoAgesic:" + tipoAgesic );
        System.out.println(en.getDebugString());
        assertTrue( en.getRuta().equals("6"));
        assertTrue( en.getKm() == 23.5);
	}
	
	@Test
	public void testPortalConLetra() {
		String q = "DOMINGUEZ RIERA 5932 BIS";
    	EntradaNormalizada en = GeocodingUtils.parseAddress(q);

    	System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec());
        System.out.println(en.getDebugString());
        
        assertTrue( en.getTipoDirec() == TipoDirec.CALLEyPORTAL);
        assertTrue( en.getPortal() == 5932);
        assertTrue( (en.getLetra() != null) && (en.getLetra().equals("BIS")));

		q = "14 de julio 5932 BIS, MONTEVIDEO";
    	en = GeocodingUtils.parseAddress(q);

    	System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec());
        System.out.println(en.getDebugString());
        
        assertTrue( en.getTipoDirec() == TipoDirec.CALLEyPORTAL);
        assertTrue( en.getPortal() == 5932);

		q = "14 de julio S/N, MONTEVIDEO";
    	en = GeocodingUtils.parseAddress(q);

    	System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec());
        System.out.println(en.getDebugString());
        
        assertTrue( en.getTipoDirec() == TipoDirec.CALLEyPORTAL);
        assertTrue( en.getPortal() == 0);
        assertTrue( en.getLetra().equals("S/N"));

	}
	
	@Test
	public void testFuzzyComparison() {
		String q = "Avda. Petrini 535,Sarandí del Yí";
		String r1 = "PETRINI, SARANDI DEL YI";
		String r2 = "DON BOSCO, SARANDI DEL YI";
		FuzzyScore fuzzy = new FuzzyScore(Locale.forLanguageTag("es-ES"));
    	double score1 = fuzzy.fuzzyScore(q, r1);
    	double score2 = fuzzy.fuzzyScore(q,  r2);
        System.out.println("Score1:" + score1 + ", score2:" + score2);
        assert(score1 > score2);
        
        q = "Ignacio Uribe";
        r1 = "Coronel Ignacio Rivas";
        r2 = "General Ignacio Oribe";
    	score1 = fuzzy.fuzzyScore(q, r1);
    	score2 = fuzzy.fuzzyScore(q,  r2);
    	score1 = FuzzySearch.weightedRatio(q, r1) / 10.0;
    	score2 = FuzzySearch.weightedRatio(q, r2) / 10.0;
		double scoreTruco1 = truco(q, r1, fuzzy);
		System.out.println("ScoreTruco1:" + scoreTruco1);
		double scoreTruco2 = truco(q, r2, fuzzy);
		System.out.println("ScoreTruco2:" + scoreTruco2);
		
		score1 = score1 + scoreTruco1 / 2.0;
		score2 = score2 + scoreTruco2 / 2.0;
    	
        System.out.println("Score1:" + score1 + ", score2:" + score2);
        assert(score2 > score1);
		
	}


	public double truco(String q, String r, FuzzyScore fuzzy) {
		double scoreTruco = 0;
		String[] words = StringUtils.splitByWholeSeparator(r, " ");
		if (words.length > 2) {
			// El algoritmo es demasiado sensible a la primera palabra. Intentamos evitar aquí
			// que si la calle en la base de datos empieza por GENERAL por ejemplo, y el usuario no
			// ha escrito la primera palabra, probamos a quitarla y ver qué tal es el score.
			String dirSin = words[1] + " ";
			for (int i=2;  i < words.length; i++) {
				dirSin += words[i] + " ";
			}
			dirSin = dirSin.trim();
			scoreTruco = fuzzy.fuzzyScore(q, dirSin) / 10.0;
			scoreTruco = FuzzySearch.weightedRatio(q, dirSin);
		}
		return scoreTruco;
	}

	
	@Test
	public void TestParseWithCodPostalInLocalidad() {
		String q = "Juan Alvarez 8032, Treinta y Tres ,Treinta y Tres";
		EntradaNormalizada en = GeocodingUtils.parseAddress(q);	  
		System.out.println(en.getDebugString());
	}


	@Test
	public void testManzanaSolar() {
		String q = "33 ORIENTALES MANZANA 243 SOLAR 15, PAYSANDU, PAYSANDU";
		String tipoAgesic = "Manzana - Solar";
    	EntradaNormalizada en = GeocodingUtils.parseAddress(q);
        System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec() +
        		" \ntipoAgesic:" + tipoAgesic );
        System.out.println(en.getDebugString());        
        assertTrue(en.getTipoDirec() == TipoDirec.MANZANAySOLAR);
	}

	@Test
	public void testEsquina() {
		String q = "RIO BRANCO ESQ MERCEDES, MONTEVIDEO, MONTEVIDEO";
		String tipoAgesic = "Esquina"; // Revisar, en el fichero de pruebas pone Calle - Nro
    	EntradaNormalizada en = GeocodingUtils.parseAddress(q);
        System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec() +
        		" \ntipoAgesic:" + tipoAgesic );
        System.out.println(en.getDebugString());
        assertTrue(en.getTipoDirec() == TipoDirec.ESQUINA);
		
	}
	
	@Test
	public void testGeocoderResutlV0() {
		int codPostal = 10092;
		GeocoderResultV0 r = new GeocoderResultV0();
		
		r.setCodigoPostal(codPostal);
		
		assert(r.getCodigoPostalAmpliado() == codPostal);
		assert(r.getCodigoPostal() == 10000);
		
	}

	@Test
	public void testGeocoderResutl() {
		GeocoderResult result = new GeocoderResult();
		
		String direc = GeocodingUtils.getAddress("micalle", 10, null, "miLocalidad", "miDepartamento");
		result.setDepartamento("miDepartamento");
		result.setLocalidad("miLocalidad");
        result.setNomVia("micalle");
        result.setPortalNumber(10);

		
		assert(result.getPortalNumber() == 10);
		assert(result.getAddress().equals(direc));
		
	}

	

	
//	ID|DEPARTAMENTO|LOCALIDAD|DIRECCION|YOUTRACK|OBSERVACIONES|TIPO PRUEBA
//	1|MONTEVIDEO|MONTEVIDEO|3(MILLAN Y LECOCQ) 853 A.210|ver observaciones||Entidad Colectiva
//	2|MONTEVIDEO|MONTEVIDEO|3 873 A.309 ESQ.COMPLEJO MILLAN Y LECOCQ|ver observaciones||Calle - Nro
//	3|MONTEVIDEO|MONTEVIDEO|6 824 ALTO.107 - COMPLEJO MILLAN Y LECOCQ|youtrack tsubasa 19||Calle - Nro
	@Test   
	public void testFile() throws IOException {
	    String path = "./src/test/resources";
	    String fileName = "entradas.txt";

	    File file = new File(path, fileName);

	    assertTrue(file.exists());
	    	    
	    FileReader fileReader = new FileReader(file);
	    	    
    	CSVReader csvReader = new CSVReader(fileReader, '|');
        String[] values = null;
        int i = 0;
        int numEncontrados = 0;
        int numEncontradosV0 = 0;
        int numEncontradosAgesic = 0;
        while ((values = csvReader.readNext()) != null) {
        	i++;
        	if (i == 1)
        		continue;
        	String nomVia = values[3];
        	String localidad = values[2];
        	String departamento = values[1];
        	String youtrack = values[4];
        	String observaciones = values[5];
        	String tipoPrueba = values[6];
        	String q = nomVia + ", " + localidad + ", " + departamento;
        	EntradaNormalizada en = GeocodingUtils.parseAddress(q);
            System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec() +
            		" \ntipoAgesic:" + tipoPrueba + " observaciones:" + observaciones + " youtrack:" + youtrack);
            System.out.println(en.getDebugString());
        }
	    
	}

}
