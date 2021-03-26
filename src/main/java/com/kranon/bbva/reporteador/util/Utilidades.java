package com.kranon.bbva.reporteador.util;

import java.io.FileReader;
import java.util.Date;
import java.util.Properties;

public class Utilidades {
    
    public boolean getProperty(String[] confi, String vsRutaArchivo) {
    	try {
            Properties p = new Properties();
            p.load(new FileReader(vsRutaArchivo));
            for (int i = 0; i < confi.length; i++) {
                String cadena = confi[i];
                confi[i] = p.getProperty(cadena);
                if (confi[i] == null) {
                    confi[i] = "";
                } else {
                    confi[i] = confi[i].trim();
                }
            }
            return true;
        } catch (Exception e) {
        	Log.GuardaLog("[" + new Date() + "][ERROR] NO SE PUDO LEER " + vsRutaArchivo);
            return false;
        }
    }
}
