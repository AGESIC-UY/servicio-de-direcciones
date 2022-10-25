package uy.agesic.direcciones.data;

import lombok.Data;

@Data
public class EntradaNormalizada {
	
	String entrada;
	String nomVia;
	int portal;
	String letra;
	String ruta;
	double km;
	String manzana;
	String solar;
	String esquinaCon;
	String localidad;
	String departamento;
	
	TipoDirec tipoDirec;
	
	public enum TipoDirec {
		CALLE,
		CALLEyPORTAL,
		POI,
		MANZANAySOLAR,
		ESQUINA,
		RUTAyKM,
		LOCALIDAD
	}
	
	public EntradaNormalizada(String entrada) {
		this.entrada = entrada;
	}

	public String getDebugString() {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append( getNomVia()).append('|');
        strBuf.append( getPortal()).append('|');
        strBuf.append( getLetra()).append('|');
        strBuf.append( getManzana()).append('|');
        strBuf.append( getSolar()).append('|');
        strBuf.append( getRuta()).append('|');
        strBuf.append( getKm()).append('|');
        strBuf.append( getEsquinaCon()).append('|');
        strBuf.append( getLocalidad()).append('|');
        strBuf.append( getDepartamento()).append('|');
        strBuf.append( getTipoDirec()).append('|');

		return strBuf.toString();
	}
}
