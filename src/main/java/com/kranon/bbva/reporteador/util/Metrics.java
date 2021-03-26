package com.kranon.bbva.reporteador.util;

import java.util.Date;

public class Metrics {
	private StringBuffer voStringBuffer = null;
	private String vsUUI = "";
	
	public Metrics(String vsUUI, StringBuffer voStringBuffer) {
		this.voStringBuffer = voStringBuffer;
		this.vsUUI = vsUUI;
	}
	
	public void ContainsBreadCrumbs() {
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ContainsBreadCrumbs][INFO] ---> BEGINNING THE BREAD CRUMB COUNT");
		String[] vaBCSplits = voStringBuffer.toString().split("\n"); 
		for(String vsLineaBC : vaBCSplits) {
			if(vsLineaBC.contains("")) {
				
			}
		}
	}
}
