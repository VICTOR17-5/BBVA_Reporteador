package com.kranon.bbva.reporteador.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

public class Metrics {
	private StringBuffer voStringBuffer = null;
	
	public Metrics(StringBuffer voStringBuffer) {
		this.voStringBuffer = voStringBuffer;
	}
	
	public void ContainsBreadCrumbs(Map<String,Integer> voMapBreadCrumbs) {
		String[] vaBCSplits = voStringBuffer.toString().split("\n"); 
		for(String vsLineaBC : vaBCSplits) 
			for (Entry<String, Integer> voMapEntry : voMapBreadCrumbs.entrySet()) 
				if(vsLineaBC.contains(voMapEntry.getKey())) 
					voMapBreadCrumbs.replace(voMapEntry.getKey(), voMapEntry.getValue() + 1);
	}
	
	public Integer ContainsBreadCrumbs(String vsBreadCrumb) {
		Integer viContador = 0;
		String[] vaBCSplits = voStringBuffer.toString().split("\n"); 
		for(String vsLineaBC : vaBCSplits) 
			if(vsLineaBC.equals(vsBreadCrumb)) 
				viContador++;
		return viContador;
	}
	
	public boolean GenerateMetrics(String vsRutaMetricas, Map<String,Integer> voMapBreadCrumb,
			String vsFechaInicio, String vsFechaFin, String vsApplication, Integer vsTotales) {
		StringBuffer voBufferMetrics = new StringBuffer();
		
		
		
		voBufferMetrics.append(
				  "		------------------------------------------------------------\r\n"
				+ "		Reporte de Llamadas: Genesys Cloud\r\n"
				+ "		------------------------------------------------------------\r\n"
				+ "		Promotora Kranon S.A. de C.V.\r\n"
				+ "		Aplicación:		" + vsApplication + "\r\n"
				+ "		Fecha:			" + vsFechaInicio + " 00:00 al " + vsFechaFin + " 00:00\r\n"
				+ "		Número llamadas totales: " + vsTotales + "\r\n"
				+ "		------------------------------------------------------------\n\n");
		
		voBufferMetrics.append(String.format(
				  "		Total Llamadas:\r\n"
			  +   "			- Llamadas en horario abierto					%8d\n",voMapBreadCrumb.get("bbvamxap_002")));
		voBufferMetrics.append(String.format(
				  "			- Llamadas en horario cerrado					%8d\n",voMapBreadCrumb.get("bbvamxap_001")));
		voBufferMetrics.append(
				  "			---------------------------------------------------\n");
		voBufferMetrics.append(String.format(
				  "										Totales				%8d\n\n",(voMapBreadCrumb.get("bbvamxap_001") + voMapBreadCrumb.get("bbvamxap_002"))));
		
		voBufferMetrics.append(String.format(
				  "		Tipo de Atención:\r\n"
			    + "			- Inglés										%8d\n",voMapBreadCrumb.get("bbvamxap_007")));	
		voBufferMetrics.append(String.format(
				  "			- Español										%8d\n",voMapBreadCrumb.get("bbvamxap_008")));
		voBufferMetrics.append(String.format(
				  "			- HangUp										%8d\n",voMapBreadCrumb.get("bbvamxap_002,bbvamxap_004")));
		voBufferMetrics.append(
				  "			---------------------------------------------------\n");
		voBufferMetrics.append(String.format(
				  "										Totales				%8d\n\n",(voMapBreadCrumb.get("bbvamxap_007") + voMapBreadCrumb.get("bbvamxap_008")
				  																		+ voMapBreadCrumb.get("bbvamxap_002,bbvamxap_004")
				  																	)));
		
		voBufferMetrics.append(String.format(
				  "		Atención Inglés:\r\n"
				+ "			- Escuchan aviso de privacidad					%8d\n",voMapBreadCrumb.get("bbvamxap_009")));
		voBufferMetrics.append(String.format(
				  "			- NO escuchan aviso de privacidad				%8d\n",voMapBreadCrumb.get("bbvamxap_010")));
		voBufferMetrics.append(
				  "			---------------------------------------------------\n");
		voBufferMetrics.append(String.format(
				  "										Totales				%8d\n\n",(voMapBreadCrumb.get("bbvamxap_010") + voMapBreadCrumb.get("bbvamxap_009"))));
		
		voBufferMetrics.append(String.format(
				  "		Módulo Identificación:\r\n"
				+ "			- Número de solicitudes de tarjeta				%8d\n\n",voMapBreadCrumb.get("identification_001")));
		
		voBufferMetrics.append(String.format(
				  "			- Clientes exceden intentos						%8d\n",voMapBreadCrumb.get("identification_002")));
		voBufferMetrics.append(String.format(
				  "			- Clientes ingresan tarjeta correcta			%8d\n",voMapBreadCrumb.get("identification_003")));
		voBufferMetrics.append(String.format(
				  "			- Número errores técnicos WS					%8d\n",voMapBreadCrumb.get("identification_004")));
		voBufferMetrics.append(String.format(
				  "			- Número errores status 409						%8d\n",voMapBreadCrumb.get("identification_005")));
		voBufferMetrics.append(
				  "			---------------------------------------------------\n");
		voBufferMetrics.append(String.format(
				  "										Totales				%8d\n\n",(voMapBreadCrumb.get("identification_002") + voMapBreadCrumb.get("identification_003") 
																					  + voMapBreadCrumb.get("identification_004") + voMapBreadCrumb.get("identification_005")
																					  )));
		
		voBufferMetrics.append(String.format(
				  "		Módulo Client Profile:\r\n"
				+ "			- WS Success (200)								%8d\n",voMapBreadCrumb.get("bbvamxap_011")));
		voBufferMetrics.append(String.format(
				  "			- WS Error	 (Default)							%8d\n\n",voMapBreadCrumb.get("bbvamxap_012")));
		
		voBufferMetrics.append(String.format(
				  "			- Transferencia Personas Físicas				%8d\n",voMapBreadCrumb.get("bbvamxap_013")));
		voBufferMetrics.append(String.format(
				  "			- Transferencia	Personas Morales				%8d\n",voMapBreadCrumb.get("bbvamxap_014")));
		voBufferMetrics.append(String.format(
				  "			- Transferencia Personas Universales			%8d\n",voMapBreadCrumb.get("bbvamxap_015")));
		voBufferMetrics.append(
				  "			---------------------------------------------------\n");
		voBufferMetrics.append(String.format(
				  "										Totales				%8d",(voMapBreadCrumb.get("bbvamxap_013") + voMapBreadCrumb.get("bbvamxap_014") 
																					  + voMapBreadCrumb.get("bbvamxap_015")
																					  )));
		
		File voFile = new File(vsRutaMetricas);
		if(voFile.exists()) voFile.delete();
		PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(vsRutaMetricas, true));
            pw.println(voBufferMetrics.toString());
            pw.close();
        } catch (Exception e) {
        	return false;
        }
		return true;
	}
}
