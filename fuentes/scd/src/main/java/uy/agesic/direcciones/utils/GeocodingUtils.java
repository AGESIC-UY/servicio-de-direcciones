package uy.agesic.direcciones.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.openlocationcode.OpenLocationCode;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import uy.agesic.direcciones.controller.GeocodeController;
import uy.agesic.direcciones.data.EntradaNormalizada;
import uy.agesic.direcciones.data.EntradaNormalizada.TipoDirec;

public class GeocodingUtils {
	private static Logger logger = LoggerFactory.getLogger(GeocodingUtils.class);
	// If there is no portal number, it returns -1
	public static int getPortalNumber(String s) {
		String temp = s.replaceAll(" +", " ");
		String[] w = temp.split(","); // quitamos la localidad si existe
		String[] words = w[0].split(" ");
		if (words.length > 1) {
			String wEnd = words[words.length - 1].trim();
			if (isNumeric(wEnd))
				return Integer.parseInt(wEnd);
		}
		return -1;
	}

	public static boolean isNumeric(String q) {
		try {
			Double.parseDouble(q);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isOLC(String q) {
		if (OpenLocationCode.isValidCode(q))
			return true;
		return false;
	}

	public static EntradaNormalizada parseAddress(String entrada) {
		if (GeocodingUtils.isOLC(entrada)) {
			EntradaNormalizada en = new EntradaNormalizada(entrada);
			en.setOLC(entrada);
			en.setNomVia(entrada); // Por ahora, hack para que se vea en el cliente gvsigOL
			en.setTipoDirec(TipoDirec.OLC);
			return en;
		}
		String[] partes = entrada.split(",");
		String calle;
		String localidad = null;
		String departamento = null;
		String localidadODepto = null;
		String calleSinPortal = null;
		int numero = -1;
		if (partes.length == 1) {
			calle = partes[0].trim();
		} else if (partes.length == 2) {
			calle = partes[0].trim();
			localidadODepto = partes[1].trim();
			departamento = localidadODepto;
		} else {
			// partes.length > 2
			calle = "";
			for (int i = 0; i < partes.length - 2; i++) {
				calle += partes[i].trim();
			}
			// TODO: MEJORAR ESTO BUSCANDO EN UNA CACHE DE DEPARTAMENTOS
			localidad = partes[partes.length - 2].trim();
			departamento = partes[partes.length - 1].trim();
		}
		String calleIncompleta = null;
		String letra = null;
		if (!calle.endsWith(" ") /* && partes.length == 1 */) { // Si se terminó de escribir la palabra pero no se
																// escribió localidad o depto
			String[] palabras = calle.split(" ");
			calle = "";
			for (int i = 0; i < palabras.length; i++) {
				calle += GeocodingUtils.cleanWord(palabras[i]) + " ";
			}
			calleIncompleta = palabras[palabras.length - 1];
			try {
				// Si la ultima palabra es un numero no le doy bola
				numero = new Integer(calleIncompleta);
				calleSinPortal = calle.replace(" " + numero, "");
				calleIncompleta = null;
			} catch (Exception e) {
				// Comprobar si la penúltima palabra es un número de portal y la última es la letra.
				if (calleIncompleta.length() < 6) {
					if (palabras.length > 2) {
						try {
							String penultima = palabras[palabras.length-2];
							numero = new Integer(penultima);
							letra = calleIncompleta;
							calleSinPortal = calle.replace(" " + penultima + " " + letra, "");
							calleIncompleta = null;							
						}
						catch (Exception ex2){
							
						}
					}
				}
			}
			
		}
		EntradaNormalizada resul = new EntradaNormalizada(entrada);
		resul.setDepartamento(departamento);
		resul.setLocalidad(localidad);
		if (calleSinPortal != null)
		{
			resul.setNomVia(calleSinPortal.trim());
		}
		else
		{
			resul.setNomVia(calle.trim());
		}
		resul.setPortal(numero);
		resul.setTipoDirec(TipoDirec.CALLE); // Por defecto
		if (numero > 0) {
			resul.setTipoDirec(TipoDirec.CALLEyPORTAL);
			if (letra != null)
				resul.setLetra(letra);
		}
		if (entrada.contains(" S/N")) {
			resul.setTipoDirec(TipoDirec.CALLEyPORTAL);
			resul.setPortal(0);
			resul.setLetra("S/N");			
		}
		String strAux = calle.trim().toUpperCase();
//		Pattern patronRutaKM = Pattern.compile(
//				"(.*)\\s(KM|KM|KILOMETRO|KILÓMETRO)(\\s|\\.)(\\d{1,3}\\.\\d{1,5}|\\d{1,3}\\,\\d{1,5}|\\d{1,5})");
		Pattern patronRutaKM = Pattern.compile("(.*RUTA.*)(.*KM\\.|.*KM.*|.*KILÓMETRO.*|.*KILOMETRO.*)");
		Pattern patronNumbers = Pattern.compile("(\\d+?(\\.|,)\\d*)|(\\d+)");
		Matcher mRutaKM = patronRutaKM.matcher(strAux);
		if (mRutaKM.matches()) {
			System.out.println("matchea con ruta");
			try {
				Matcher mNumbers = patronNumbers.matcher(strAux);
				mNumbers.find();
				String rutaa = mNumbers.group(0);
				mNumbers.find();
				String kmstring = mNumbers.group(0).trim();
				boolean bDecimal = mNumbers.find();
				if (bDecimal) {
					kmstring += "." + mNumbers.group(0).trim();
				}
				Double km = null;
				if (kmstring.contains(",") || kmstring.contains(".")) {
					kmstring = kmstring.replace(",", ".");
					km = new Double(kmstring);
				} else {
					km = new Double(kmstring);
					if (km > 999) {
						km = km / 1000;
					}
				}
				if (km != null) {
					resul.setRuta(rutaa);
					resul.setKm(km);
				}
				resul.setTipoDirec(TipoDirec.RUTAyKM);
				resul.setPortal(-1);
			}
			catch (Exception e) {
				logger.debug(e.getMessage());
			}
		} else {
			Pattern patron = Pattern
					.compile("(.*)(MANZANA|MAN\\.|M\\.|M\\:)\\s*(.+)\\s*(SOLAR|SOL\\.|S\\.|S\\:)\\s*(.+)\\s*");
			Matcher m = patron.matcher(strAux);
			if (m.find()) {
				// Se busca por manzana y solar
				resul.setNomVia(m.group(1).trim());
				resul.setManzana(m.group(3).trim());
				resul.setSolar(m.group(5).trim());

				patron = Pattern.compile("([^\\s]*)\\s+.*");
				m = patron.matcher(resul.getManzana());
				if (m.find()) {
					resul.setManzana(m.group(1));
				}
				m = patron.matcher(resul.getSolar());
				if (m.find()) {
					resul.setSolar(m.group(1));
				}
				resul.setTipoDirec(TipoDirec.MANZANAySOLAR);
				resul.setPortal(-1);

			} else { // FJP: PORQUÉ SE VUELVE A BUSCAR??
				Pattern patronms = Pattern
						.compile("(.*)(MANZANA|MAN\\.|M\\.|M\\:|M)(\\d+)\\s*(SOLAR|SOL\\.|S\\.|S\\:|S)(\\d+)\\s*");
				Matcher ms = patronms.matcher(strAux);
				if (ms.find()) {
					// Se busca por manzana y solar
					resul.setNomVia(ms.group(1).trim());
					resul.setManzana(ms.group(3).trim());
					resul.setSolar(ms.group(5).trim());
					resul.setTipoDirec(TipoDirec.MANZANAySOLAR);
					
					patron = Pattern.compile("([^\\s]*)\\s+.*");
					// 2018-08-30 lo corregi y no lo probe, saque la linea siguiente y deje la otra
					// ms = patronms.matcher(de.getManzana());
					ms = patron.matcher(resul.getManzana());
					if (ms.find()) {
						resul.setManzana(ms.group(1));
					}
					// 2018-08-30 lo corregi y no lo probe, saque la linea siguiente y deje la otra
					// ms = patronms.matcher(de.getSolar());
					ms = patron.matcher(resul.getSolar());
					if (ms.find()) {
						resul.setSolar(ms.group(1));
					}
				}
			}
		}
		//	agregado para esquina*/
		Pattern patronEsquina = Pattern.compile("(.*)(ESQ |ESQUINA |ESQ\\. )(.*)");
		if (patronEsquina.matcher(strAux).find()) {
			String calle1;
			String calle2;
			Matcher m = patronEsquina.matcher(strAux);
			m.find();
			calle1 = m.group(1).trim();
			calle2 = m.group(3).trim();
			resul.setNomVia(calle1);
			resul.setEsquinaCon(calle2);
			resul.setTipoDirec(TipoDirec.ESQUINA);
		}
		
//        resul.setPadron(null);
//        Pattern patronPadron = Pattern.compile("(.*)P(\\d+)(R|U)(.*)");
//        Matcher m = patronPadron.matcher(de.getDireccion().toUpperCase());
//        if (m.find()) {
//            de.setPadron(Integer.parseInt(m.group(2)));
//            de.setPadronRural(m.group(3).equals("R"));
//        }

		return resul;
	}

//    public DataEntrada generarEntrada(String direccion,
//            String numero,
//            String resto,
//            String nombreinmueble) {
//        DataEntrada de = new DataEntrada();
//        de.setDireccion(direccion);
//        de.setCalle(direccion);
//        de.setNumero(numero);
//
//        Pattern patronRutaKM = Pattern.compile("(.*)\\s(KM|KM|KILOMETRO|KILÓMETRO)(\\s|\\.)(\\d{1,3}\\.\\d{1,5}|\\d{1,3}\\,\\d{1,5}|\\d{1,5})");
//        Matcher mRutaKM = patronRutaKM.matcher(de.getCalle().toUpperCase());
//        if (mRutaKM.find()) {
////            System.out.println("matchea con ruta");
//            String rutaa = mRutaKM.group(1).trim();
//            if (!"RUTA".equals(rutaa)) {
//                String kmstring = mRutaKM.group(4).trim();
//                Double km = null;
//                if (kmstring.contains(",") || kmstring.contains(".")) {
//                    kmstring = kmstring.replace(",", ".");
//                    km = new Double(kmstring);
//                } else {
//                    km = new Double(kmstring);
//                    if (km > 999) {
//                        km = km / 1000;
//                    }
//                }
//                if ((km != null) && (!"RUTA".equals(rutaa))) {
//                    de.setRuta(rutaa);
//                    de.setKm(km);
//                }
//            }
//        } else {
//            //Pattern patron = Pattern.compile("(.*)(MANZANA|MAN\\.|M\\.|M\\:|M)(\\d+)\\s*(SOLAR|SOL\\.|S\\.|S\\:|S)(\\d+)\\s*");
//            Pattern patron = Pattern.compile("(.*)(MANZANA|MAN\\.|M\\.|M\\:)\\s*(.+)\\s*(SOLAR|SOL\\.|S\\.|S\\:)\\s*(.+)\\s*");
//            Matcher m = patron.matcher(de.getCalle().toUpperCase());
//            if (m.find()) {
//                // Se busca por manzana y solar
//                de.setCalle(m.group(1).trim());
//                de.setManzana(m.group(3).trim());
//                de.setSolar(m.group(5).trim());
//
//                patron = Pattern.compile("([^\\s]*)\\s+.*");
//                m = patron.matcher(de.getManzana());
//                if (m.find()) {
//                    de.setManzana(m.group(1));
//                }
//                m = patron.matcher(de.getSolar());
//                if (m.find()) {
//                    de.setSolar(m.group(1));
//                }
//
//            } else {
//                Pattern patronms = Pattern.compile("(.*)(MANZANA|MAN\\.|M\\.|M\\:|M)(\\d+)\\s*(SOLAR|SOL\\.|S\\.|S\\:|S)(\\d+)\\s*");
//                Matcher ms = patronms.matcher(de.getCalle().toUpperCase());
//                if (ms.find()) {
//                    // Se busca por manzana y solar
//                    de.setCalle(ms.group(1).trim());
//                    de.setManzana(ms.group(3).trim());
//                    de.setSolar(ms.group(5).trim());
//
//                    patron = Pattern.compile("([^\\s]*)\\s+.*");
//                    //2018-08-30 lo corregi y no lo probe, saque la linea siguiente y deje la otra
//                    //ms = patronms.matcher(de.getManzana());
//                    ms = patron.matcher(de.getManzana());
//                    if (ms.find()) {
//                        de.setManzana(ms.group(1));
//                    }
//                    //2018-08-30 lo corregi y no lo probe, saque la linea siguiente y deje la otra
//                    //ms = patronms.matcher(de.getSolar());
//                    ms = patron.matcher(de.getSolar());
//                    if (ms.find()) {
//                        de.setSolar(ms.group(1));
//                    }
//
//                } else {
//                    // El número no está en una columna, se saca de la calle
//                    patron = Pattern.compile("(\\d*\\D+)(\\d+)(.*)");
//                    m = patron.matcher(de.getCalle().toUpperCase());
//                    if (m.find()) {
//                        direccion = m.group(1).trim();
//                        numero = m.group(2);
//                        resto = m.group(3).trim();
//                        if (numero.trim().length() < 3) {
////                                Pattern patron2 = Pattern.compile("(calle \\d+\\D*|\\D*)(\\d{3,4})(.*)", Pattern.CASE_INSENSITIVE);
//                            Pattern patron2 = Pattern.compile("(\\D*)(\\d{3,4})(.*)");
////                                Pattern.c
//                            Matcher m2 = patron2.matcher(resto);
//                            if (m2.find()) {
//                                direccion += " " + numero + " " + m2.group(1);
//                                numero = m2.group(2);
//                                resto = m2.group(3);
//                            }
//                        }
////                        System.out.println("direccion: " + direccion);
////                        System.out.println("numero: " + numero);
////                        System.out.println("resto: " + resto);
//                        de.setCalle(direccion);
//                        de.setNumero(numero);
//                        de.setResto(resto);
//                    }
//                }
//            }
//        }
//
//        if (nombreinmueble == null) {
//            /*
//             * El nombre inmueble no está en una columna, se saca de la calle
//             * */
//
//            /**
//             * Primero busco si tiene algpun separador del tipo Esquina, S/N,
//             * etc que ya se que no va a ser parte del nombre Estas palabras
//             * filtro se setean en el archivo de configuración
//             */
//            Pattern patron = Pattern.compile("(\\d*\\D+)\\s+((" + Configuracion.obtenerParametro("palabrasCorteDireccion") + ")(\\s+|$).*)", Pattern.CASE_INSENSITIVE);
//            Matcher m = patron.matcher(de.getCalle());
//            //      Matcher m = patron.matcher(de.getCalleNormalizada());  
//
//            if (m.find()) {
//                de.setNombreInmueble(m.group(1).trim());
//            } else {
//                /**
//                 * Si no encuentra busco de sacar los números al final
//                 */
////                patron = Pattern.compile("(\\d*\\D+)(\\d*.*)");
////                m = patron.matcher(de.getCalle());
////                if (m.find()) {
////                    de.setNombreInmueble(m.group(1).trim());
////                } else {
//                //  de.setNombreInmueble(de.getCalle());
//                de.setNombreInmueble(de.getDireccion());
////                }
//            }
//        } else {
//            //de.setNombreInmueble(de.getCalle());
//            de.setNombreInmueble(de.getDireccion());
//        }
//
//        /*agregado para esquina*/
//        Pattern patronEsquina = Pattern.compile("(.*)(ESQ |ESQUINA |ESQ\\. )(.*)");
//        if (patronEsquina.matcher(de.getCalle().toUpperCase()).find()) {
//            String calle1;
//            String calle2;
//            Matcher m = patronEsquina.matcher(de.getCalle().toUpperCase());
//            m.find();
//            calle1 = m.group(1).trim();
//            calle2 = m.group(3).trim();
//            de.setCalle(calle1);
//            de.setEsquina(calle2);
//        }
//
//        de.setPadron(null);
//        Pattern patronPadron = Pattern.compile("(.*)P(\\d+)(R|U)(.*)");
//        Matcher m = patronPadron.matcher(de.getDireccion().toUpperCase());
//        if (m.find()) {
//            de.setPadron(Integer.parseInt(m.group(2)));
//            de.setPadronRural(m.group(3).equals("R"));
//        }
//
//        return de;
//    }

	private static String cleanWord(String entrada) {
		if (isNumeric(entrada))
			return entrada.trim();
        entrada = entrada.replace('.', ' ');
        entrada = entrada.replace('\'', ' ');
        entrada = entrada.replaceAll("\\(.*\\)", " ");
        return entrada;
	}

	public static String getAddress(String nombre, String localidad, String departamento) {
		String direc = nombre;
		if (null == nombre)
			if (null != localidad)
				direc = localidad;
			else
				direc = "";
		else {
			if (null != localidad)
				direc = nombre + ", " + localidad;
			else
				direc = nombre;
		}

		if (null != departamento)
			direc = direc + ", " + departamento;

		return direc;

	}

	public static String getAddress(String nombre, int portal, String letra, String localidad, String departamento) {
		String direc = "";
		if (null == nombre)
			if (null != localidad)
				direc = localidad;
			else
				direc = "";
		else {
			if (portal >= 0)
				direc = nombre + " " + portal;
			else
				direc = nombre;

			if ((letra != null ) && (letra != "")) {
				if (letra.equals("S/N")) {
					direc = nombre; // Quitamos el cero	
				}
				direc = direc + " " + letra;
			}

			
			if (null != localidad)
				direc = direc + ", " + localidad;
		}

		if (null != departamento)
			direc = direc + ", " + departamento;

		return direc;

	}

	
	public static String getAddressRuta(String nombre, double km, String localidad, String departamento) {
		String direc = "";
		if (null == nombre)
			if (null != localidad)
				direc = localidad;
			else
				direc = "";
		else {
			if (km > 0)
				direc = nombre + " KM " + km;
			else
				direc = nombre;

			if (null != localidad)
				direc = direc + ", " + localidad;
		}

		if (null != departamento)
			direc = direc + ", " + departamento;

		return direc;

	}

	public static String getAddressLocalidad(String localidad, String departamento) {
		String direc = localidad;

		if (null != departamento)
			direc = direc + ", " + departamento;

		return direc;

	}
	
	// return distance in meters.
	public static double calculateDistance(
	            double lon1, double lat1, double lon2, double lat2) {
	        GeodesicData g = Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
	        return g.s12;  // distance in meters
	}
	
	public static String latlngToOLC(double lat, double lng) {
		// Tamaño -> precisión. Con 12 dígitos debería ser suficiente
//		10	1/8000	13.9 meters
//		11	1/40000 x 1/32000	2.8 x 3.5 meters
//		12	1/200000 x 1/128000	56 x 87 cm
//		13	1/1e6 x 1/512000	11 x 22 cm
//		14	1/5e6 x 1/2.048e6	2 x 5 cm
		String res = OpenLocationCode.encode(lat, lng, 12);
		return res;
	}

}
