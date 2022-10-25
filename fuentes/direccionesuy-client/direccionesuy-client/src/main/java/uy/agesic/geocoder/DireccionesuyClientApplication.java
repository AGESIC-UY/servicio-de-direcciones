package uy.agesic.geocoder;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;

import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;
import uy.agesic.direcciones.data.GeocoderResult;
import uy.agesic.direcciones.data.GeocoderResultV0;
import uy.agesic.direcciones.data.LocalidadResultV0;
import uy.agesic.direcciones.data.SugerenciaResultV0;
import uy.agesic.direcciones.utils.GeocodingUtils;

public class DireccionesuyClientApplication {


	private static final Log log = LogFactory.getLog(DireccionesuyClientApplication.class);
	public static void main(String[] args) {
		
		System.out.println("Ejecutando Cliente de Direcciones UY");
		
		Options options = new Options();

        Option input = new Option("i", "input", true, "Fichero de Entrada (.csv)");
        input.setRequired(false);
        options.addOption(input);
        
        Option dirUnica = new Option("du", "dirUnica", true, "Fichero de Entrada Direcci�n �nica(.txt)");
        dirUnica.setRequired(false);
        options.addOption(dirUnica);


        Option output = new Option("o", "output", true, "Fichero de Salida (.out)");
        output.setRequired(false);
        options.addOption(output);
        
        Option optRemoteUrl = new Option("remoteUrl", "remoteUrl", true, "Ejemplo: https://callejerouy-direcciones.agesic.gub.uy/api");
        optRemoteUrl.setRequired(false);
        options.addOption(optRemoteUrl);

        Option optVersion = new Option("v", "version", true, "version (v0 v1)");
        optVersion.setRequired(false);
        options.addOption(optVersion);

        Option optMethod = new Option("m", "method", true, "funci�n a probar (ej: SugerenciaCompleta)");
        optMethod.setRequired(false);
        options.addOption(optMethod);
        
        Option optQuery = new Option("q", "query", true, "par�metro de b�squeda (ej: Avda. 18 de Julio");
        optQuery.setRequired(false);
        options.addOption(optQuery);

        // String remoteUrl = "https://callejerouy-direcciones.agesic.gub.uy/api"; //  (Direcciones sin pipeline (primera prueba de todas))
        // http://callejerouy2-direccionesuy-dev.paas.red.uy/ // DEV (direcciones-dev) (Ojo, http)
        String remoteUrl = "https://direcciones.ide.uy/api";    // Por defecto a PROD

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            
            String inputFilePath = cmd.getOptionValue("input");
            String outputFilePath = cmd.getOptionValue("output");

            
    	    if (cmd.getOptionValue("remoteUrl") != null) {
    	    	remoteUrl = cmd.getOptionValue("remoteUrl");    	    	
    	    }
    	    
    	    System.out.println("Usando el servicio en: " + remoteUrl );
    	    
    	    
    	    String query = cmd.getOptionValue("query");
    	    String localidad = cmd.getOptionValue("localidad");
    	    String departamento = cmd.getOptionValue("departamento");
    	    String version = cmd.getOptionValue("version");
    	    String method = cmd.getOptionValue("method");
    	    String pathTestFile = cmd.getOptionValue("input");
    	    String pathOutputFile = cmd.getOptionValue("output");
    	    String pathFileDirUnica = cmd.getOptionValue("dirUnica");
    	    
    	    GeocodeV0 v0 = new GeocodeV0(remoteUrl);
    	    
    	    List<GeocoderResult> resultV1 = null;

    	    if ((pathTestFile == null) && (pathFileDirUnica == null)) {
    	    	if (version != null) {    	    
	    	    	if (version.equalsIgnoreCase("v0")) {
	    	    		List<SugerenciaResultV0> resultSug;
	    	    		List<GeocoderResultV0> resultGeo;
	    	    		List<LocalidadResultV0> resultLoc;
	    	    		switch (method) {
	    	    		case "SugerenciaCompleta":
	    	    			resultSug = v0.getSugerenciaCalleCompleta(query, true);
	    	    			System.out.println(resultSug);
	    	    			break;
	    	    		case "BusquedaDireccion":
	    	    			resultGeo = v0.getBusquedaDireccion(query, localidad, departamento);
	    	    			printV0(resultGeo);
	    	    			break;
	    	    		case "Localidades":
	    	    			resultLoc = v0.getLocalidades(departamento, true);
	    	    			System.out.println(resultLoc);
	    	    			break;
	    	    		case "ReverseGeocoding":
	    	    			// Ojo al orden: latitud y longitud
	    	    			resultV1 = v0.getReverseGeocoding(-34.9032784, -56.1881599, 100);
	    	    			System.out.println(resultV1);
	    	    			break;
	    	    		default:
	    	    			System.out.println("M�todo o nombre de funci�n desconocida.");
	    	    			break;
	    	    		
	    	    		}
	    	    	}
	    	    	else if (version.equalsIgnoreCase("v1")) {
	    	    		GeocodeV1 v1 = new GeocodeV1(remoteUrl);
	    	    		switch (method) {
		    	    		case "candidates":
		    	    			resultV1 = v1.getCandidates(query, false, 12);
		    	    			break;
		    	    		case "find":
		    	    			resultV1 = v1.getAddress("calle", "8770", "", "", "710", "", "", "", "", "", "", "", "");
		    	    			break;
		    	    		case "puntoNotable":
		    	    			resultV1 = v1.getDirecPuntoNotable("Montevideo", "palacio", 10);
		    	    			break;
		    	    			
		    	    		// TODO: A�ADIR EJEMPLOS CON EL RESTO

	    	    		}
	    	    		
	    	    		printV1(resultV1);
	    	    	}
    	    	}
    	    	else {
    	    		throw new Exception("Error: par�metros desconocidos.");
    	    	}
    	    		
    	    }
    	    else {
    	    	if (pathFileDirUnica != null) {
    	    		File fileOut = new File(pathFileDirUnica.replace("txt","out"));
    	    		processFileDirUnica(pathFileDirUnica, fileOut.getAbsolutePath(), remoteUrl);
    	    	}
    	    	else
    	    	{
    	    		processFile(pathTestFile, pathOutputFile, remoteUrl);
    	    	}
    	    }
            
        } catch (Exception e) {
            System.out.println(e.toString());
            formatter.printHelp("geocoderUY", options);
            System.out.println("Ejemplo de uso: java -jar direccionesuy-client.jar -i test.csv -o test.out");

            System.exit(1);
        }		
	}
	private static void processFile(String inputFilePath, String outputFilePath, String remoteUrl) throws IOException {

	    File file = new File(inputFilePath);

	    if (!file.exists() ) {
	    	System.err.println("El fichero " + inputFilePath + " no existe.");
	    	return;
	    }
	    	    	    
	    FileReader fileReader = new FileReader(file);
	    	    
    	CSVReader csvReader = new CSVReader(fileReader, '|');
        String[] values = null;
        int i = 0;
        int numEncontrados = 0;
        int numEncontradosV0 = 0;

	    File fileCsv = new File(outputFilePath);

        FileWriter fileWriter = new FileWriter(fileCsv);
//        CSVWriter csvWriter = new CSVWriter(fileWriter);
        GeocodeV1 cV1 = new GeocodeV1(remoteUrl);
        GeocodeV0 cV0 = new GeocodeV0(remoteUrl);

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
    		List<GeocoderResult> result = cV1.direcUnica(q, 1);
    		long t2 = System.currentTimeMillis();
    		long tA = t2-t1;
    		String best1 = "NO ENCONTRADO";
    		String best2 = "NO ENCONTRADO";
            if (result.size() == 0)
            {
            	System.err.println("No se ha encontrado nada.");
            }	
            else
            {
            	System.out.println("Mejor candidato:" + result.get(0));
            	numEncontrados++;
            	foundA = true;
            	best1 = result.get(0).toString();
            }
            
            List<GeocoderResultV0> result2 = cV0.getBusquedaDireccion(nomVia, localidad, departamento);
            long t3 = System.currentTimeMillis();
            long tB = t3 - t2;
            if (result2.size() == 0)
            {
            	System.err.println("No se ha encontrado nada con BusquedaDireccion.");
            }
            else
            {
            	System.out.println("Mejor candidato BusquedaDireccion:" + result2.get(0));
            	numEncontradosV0++;
            	foundB = true;
            	best2 = result2.get(0).toString();
            }
            
            String request = remoteUrl + "departamento=" + departamento +
    	    		"&localidad=" + localidad + "&calle=" + nomVia;
    	    String str = q + ";" + tipoPrueba + ";" + youtrack + ";" + observaciones + ";" 
    	    		+ foundA + ";" + tA + ";" + foundB + ";" + tB + ";" + foundC + ";" +
    	    		best1 + ";" + best2 + "\r\n" ;
    	    System.out.println(str);
    	    byte[] ptext = str.getBytes(ISO_8859_1); 
    	    String strUTF8 = new String(ptext, UTF_8); 
    	    fileWriter.write(strUTF8);

        }
        fileWriter.close();
        System.out.println("NumEncontrados: " + numEncontrados + " V0: " + numEncontradosV0 );        
	    
		
	}

	private static void processFileDirUnica(String inputFilePath, String outputFilePath, String remoteUrl) throws IOException {
		// ENTRADA
//		ID;ENTRADA_USUARIO
//		1;cavia 2858, mvd
//		2;herrera 826, durazno, dzno
//		3;palacio legisislativ, montevideo, montevideo
		
		// SALIDA
//		ID;ENTRADA_USUARIO;DIRECCION;X;Y;TipoResultado;STATUS_MSG
//		1;cavia 2858, mvd;LUIS B CAVIA 2858, MONTEVIDEO, MONTEVIDEO;xxxxxxxxxxx;yyyyyyyyyyyyy
//		2;herrera 826, durazno, dzno;DE HERRERA, DR. LUIS ALBERTO 826, DURAZNO, DURAZNO;xxxxxxxxxxx;yyyyyyyyyyyyy
//		3;palacio legisislativ, montevideo, montevideo;PALACIO LEGISLATIVO - AVENIDA DE LAS LEYES, MONTEVIDEO, MONTEVIDEO;xxxxxxxxxxx;yyyyyyyyyyyyy
		

	    File file = new File(inputFilePath);

	    if (!file.exists() ) {
	    	System.err.println("El fichero " + inputFilePath + " no existe.");
	    	return;
	    }
	    	    	    
	    FileReader fileReader = new FileReader(file);
	    	    
    	CSVReader csvReader = new CSVReader(fileReader, ';');
        String[] values = null;
        int i = 0;
        int numEncontrados = 0;
        int numEncontradosV0 = 0;
        
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMinimumFractionDigits(8);

	    File fileCsv = new File(outputFilePath);

        FileWriter fileWriter = new FileWriter(fileCsv);
//        CSVWriter csvWriter = new CSVWriter(fileWriter);
        GeocodeV1 cV1 = new GeocodeV1(remoteUrl);
        
        String strHeader = "ID;ENTRADA_USUARIO;DIRECCION;Longitud;Latitud;TipoResultado;StatusMsg\r\n";
	    byte[] ptext = strHeader.getBytes(ISO_8859_1); 
	    String strUTF8 = new String(ptext, UTF_8); 
	    fileWriter.write(strUTF8);


        while ((values = csvReader.readNext()) != null) {
//        	try {
	        	i++;
	        	if (i == 1)
	        		continue;
	            boolean foundA = false, foundB = false, foundC = false;
	            String id = values[0];
	        	String q = values[1];
	        	EntradaNormalizada en = GeocodingUtils.parseAddress(q);	        	
	            System.out.println("Parseando " + q + " tipo:" + en.getTipoDirec());
	            System.out.println(en.getDebugString());
	            // Revisamos la localidad para quitar el código postal
//	            String[] tokens = en.getLocalidad().split(" ");
//	            String localidadLimpia = "";
//	            int auxj = 0;
//	            for (String s : tokens) {
//	            	if (!GeocodingUtils.isNumeric(s)) {
//	            		if (auxj == 0) {
//	            			localidadLimpia = s; 
//	            		}
//	            		else
//	            		{
//	            			localidadLimpia += " " + s;
//	            		}
//	            		auxj++;
//	            	}
//	            }
//	            q = en.getNomVia() + "," + localidadLimpia + "," + en.getDepartamento();
	        	
	            System.out.println("Geocodificando " + q);
	            long t1 = System.currentTimeMillis();
	    		// List<GeocoderResult> result = cV1.direcUnica(en.getNomVia() + "," + en.getDepartamento(), 1);
	            
	            List<GeocoderResult> result = cV1.direcUnica(q, 1);
	    		long t2 = System.currentTimeMillis();
	    		long tA = t2-t1;
	    		String best1 = "NO ENCONTRADO";
	    		GeocoderResult bestResult = null;
	    		int tipoDirec = -1; // Por defecto, inválido
	    		String stateMsg = "NO ENCONTRADO";
	            if (result.size() == 0)
	            {
	            	System.err.println("No se ha encontrado nada.");
	            	// Pruebo con localidad:
	            	result = cV1.fuzzyGeocode(en.getLocalidad() + "," + en.getDepartamento(), true, 1);
	            	if (result.size() > 0) {
		            	System.out.println("Mejor candidato:" + result.get(0));
		            	numEncontrados++;
		            	foundA = true;
		            	best1 = result.get(0).toString();
		            	bestResult = result.get(0);
		            	tipoDirec = bestResult.getType().ordinal();
		            	stateMsg = "Aproximado LOCALIDAD";	            		
	            	}
	            }	
	            else
	            {
	            	System.out.println("Mejor candidato:" + result.get(0));
	            	numEncontrados++;
	            	foundA = true;
	            	best1 = result.get(0).toString();
	            	bestResult = result.get(0);
	            	tipoDirec = bestResult.getType().ordinal();
	            	stateMsg = bestResult.getStateMsg();

	            	if (bestResult.getStateMsg().equalsIgnoreCase("Aproximado"))
	            	{
		            	if (bestResult.getType() == TipoDirec.CALLE)
		            		stateMsg = "Aproximado CALLE";
		            	if (bestResult.getType() == TipoDirec.CALLEyPORTAL)
		            		stateMsg = "Aproximado PORTAL";
	            	}
	            	
	            	if (bestResult.getType() == TipoDirec.RUTAyKM) {
	            		if (bestResult.getLat() == 0.0)
	            			stateMsg = "RUTA SIN CONTINUIDAD - IMPOSIBLE POSICIONAR";
	            	}

	            }
	            
	            String x = "0.0";
	            String y = "0.0";
	            if (bestResult != null) {
	            	best1 = bestResult.getAddress();
	            	x = nf.format(bestResult.getLng());
	            	y = nf.format(bestResult.getLat());
	            }
	    	    String str = id + ";" + q + ";" + best1 + ";" + x + ";" + y + ";" + tipoDirec + ";" + stateMsg + "\r\n" ;
	    	    System.out.println(str);
//	    	    ptext = str.getBytes(ISO_8859_1); 
//	    	    strUTF8 = new String(ptext, UTF_8); 
	    	    fileWriter.write(str);
//        	}
//        	catch (Exception e) {
//        		System.out.println(e.getMessage());
//        	}
        }
        fileWriter.close();
        System.out.println("NumEncontrados: " + numEncontrados );        
	    
		
	}

	
	
	private static void printV1(List<GeocoderResult> res ) {
		System.out.println("Resultados V1: ");
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT); //pretty print
		for (GeocoderResult r : res) {
			String s = null;
			try {
				s = om.writeValueAsString(r);
				System.out.println(s);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void printV0(List<GeocoderResultV0> res ) {
		System.out.println("Resultados V0: ");
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT); //pretty print
		for (GeocoderResultV0 r : res) {
			String s = null;
			try {
				s = om.writeValueAsString(r);
				System.out.println(s);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
