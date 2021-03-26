package com.kranon.purecloud;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import com.kranon.bbva.reporteador.util.Log;
import com.kranon.conexionHttp.*;


public class PureCloud {
	private ConexionResponse conexionResponse = null;
	private String vsUUI = "";
	
	public void setUUI(String vsUUI) {
		this.vsUUI = vsUUI;
	}
	
	public String getToken(String vsClID, String vsClSec) {
		String vsAccessToken = "ERROR";
        String encodeData;
        String URLServicio = "https://login.mypurecloud.com/oauth/token?grant_type=client_credentials";
        String inputJson = "";
        int timeOut = 15000;
        HashMap<String, String> header = new HashMap<>();
		try {
			encodeData = new String(Base64.encodeBase64((vsClID + ":" + vsClSec).getBytes("ISO-8859-1")));
	        header.put("Authorization", " Basic " + encodeData);
		} catch (UnsupportedEncodingException e1) {
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> " + e1.getMessage());
		}
        try {
            ConexionHttp conexionHttp = new ConexionHttp();
            conexionResponse = conexionHttp.executePost(URLServicio, timeOut, inputJson, header);
            if (conexionResponse.getCodigoRespuesta() == 200) {
                JSONObject json = new JSONObject(conexionResponse.getMensajeRespuesta());
                if (json.has("access_token")) {
                    vsAccessToken = json.getString("access_token");
                    Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][INFO] ---> TOKEN[SUCCESS].");
                 } else  Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> TOKEN[ERROR].");
            } else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] ---> TOKEN[" + vsAccessToken + "], "
            	 		+ " CODIGO RESPUESTA[" + conexionResponse.getCodigoRespuesta() + "], MENSAJE RESPUESTA[" + conexionResponse.getMensajeRespuesta() + "]");
        } catch (IOException e) {
        	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] --->" + e.getMessage());
        } catch (Exception e) {
        	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][getToken][ERROR] --->" + e.getMessage());
        }
        return vsAccessToken;
    }
	
	public String getBody(String vsFecha, String vsFechaInt) {
		return "{\r\n"
				+ "	\"order\": \"desc\",\r\n"
				+ "	\"orderBy\": \"conversationStart\",\r\n"
				+ "	\"paging\": {\r\n"
				+ "	\"pageSize\": 100,\r\n"
				+ "	\"pageNumber\": 1\r\n"
				+ "	}, \"segmentFilters\": [{\r\n"
				+ "	\"type\": \"and\",\r\n"
				+ "	\"predicates\": [{\r\n"
				+ "	\"dimension\": \"mediaType\",\r\n"
				+ "	\"value\": \"voice\"\r\n"
				+ "	}]},\r\n"
				+ "	{ \"type\": \"or\",\r\n"
				+ "	\"predicates\": [{\r\n"
				+ "	\"dimension\": \"direction\",\r\n"
				+ "	\"value\": \"inbound\"\r\n"
				+ "	}]}],\r\n"
				+ "	\"conversationFilters\":[],\r\n"
				+ "	\"evaluationFilters\":[],\r\n"
				+ "	\"surveyFilters\":[],\r\n"
				+ "	\"metrics\": [\"tAcd\",\"tAcw\",\"tTalkComplete\",\"tIvr\",\"tHeldComplete\",\"tAlert\",\"tAbandon\",\r\n"
				+ "	\"nTransferred\",\"tTalk\",\"tHeld\",\"nOutboundAttempted\",\"tContacting\",\"tDialing\",\"tHandle\",\r\n"
				+ "	\"nBlindTransferred\",\"nConsult\",\"nConsultTransferred\",\"oMediaCount\",\"oExternalMediaCount\",\r\n"
				+ "	\"tVoicemail\",\"tMonitoring\",\"tFlowOut\" ],\r\n"
				+ "	\"interval\": \"" + vsFecha + "T06:00:00.000Z/" + vsFechaInt + "T06:00:00.000Z\"\r\n"
				+ "}";
	}

}
