package com.kranon.bbva.reporteador;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.kranon.bbva.reporteador.util.Excel;
import com.kranon.bbva.reporteador.util.Log;
import com.kranon.bbva.reporteador.util.Utilidades;
import com.kranon.conexionHttp.ConexionHttp;
import com.kranon.conexionHttp.ConexionResponse;
import com.kranon.purecloud.PureCloud;

public class ReportLog {
	private final String vsURLPCDetails = "https://api.mypurecloud.com/api/v2/analytics/conversations/details/query";
	private final String vsURLPCCall = "https://api.mypurecloud.com/api/v2/conversations/calls/";
	private String vsRutaConfi = "C:/Appl/BBVA/Reporteador/Configuraciones/conf.properties";
	private String[] vaConfi = {"client_id","client_secret","Log"};
	private String vsToken = "fXCB_63n2l-MjqAzcV-SS21Dd50wkoYtDsGAdBTeMSZqjz1xnZ-gJX2bF6HPPoxSEG9-2I_-4T7sMdAevh15gg";
	private String vsUUI = "";
	private final Integer viTimeOut = 15000;
	private String vsFecha = "";
	private String vsFechaInt = "";
	private StringBuffer voSB = null;
	
	private ConexionResponse voConexionResponse = null;
	private ConexionResponse voConexionResponseCall = null;
	private ConexionHttp voConexionHttp = null;
	private Utilidades voUti = null;
	private PureCloud pc = null;
	
	public ReportLog() {
		vsUUI = java.util.UUID.randomUUID().toString();
		Calendar c = Calendar.getInstance();
		vsFecha = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		c.add(Calendar.DATE, 1);
		vsFechaInt = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		voUti = new Utilidades();
		pc = new PureCloud();
		voConexionHttp = new ConexionHttp();
		voSB = new StringBuffer();
		if(voUti.getProperty(vaConfi, vsRutaConfi)) {
			Log.vsRutaLog = vaConfi[2];
			vsToken = pc.getToken(vaConfi[0], vaConfi[1]);
			if(vsToken.equals("ERROR")) {
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> TOKEN NOT GENERATED");
				return;
			}
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ******************** INICIANDO *******************");
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> TOKEN SUCCESSFULLY");
			HashMap<String, String> voHeader = new  HashMap<String, String>();
			voHeader.put("Authorization", "bearer " + vsToken);
			String vsBody = pc.getBody(vsFecha,vsFechaInt);
			Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ENDPOINT[" + vsURLPCDetails + "], REQUEST[" + vsBody.replace("\r\n", "") + "]");
			try {
				voConexionResponse = voConexionHttp.executePost(vsURLPCDetails, viTimeOut, vsBody, voHeader);
			} catch (Exception e) {
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> " + e.getMessage());
				return;
			}
			if(voConexionResponse.getCodigoRespuesta() == 200) {
				Map<String,String> voDetailsConversations = null;
				String vsStringJson = voConexionResponse.getMensajeRespuesta();
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> STATUS[" + voConexionResponse.getCodigoRespuesta() + "], RESPONSE[" + vsStringJson.replace("\r\n", "") + "]");
				JSONObject voJsonConversations = new JSONObject(vsStringJson);
				Map<String,Map<String,String>> voConversations = new HashMap<String,Map<String,String>>();
				if(voJsonConversations.has("conversations")) {
					JSONArray voJsonArrayConversations = voJsonConversations.getJSONArray("conversations");
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> CONVERSATIONS FOUND[" + voJsonArrayConversations.length() + "]");
					for(int i=0;i<voJsonArrayConversations.length();i++) {
						voDetailsConversations = new HashMap<String,String>();
						String vsIdConversation = String.valueOf(voJsonArrayConversations.getJSONObject(i).get("conversationId"));
						String vsURLConversation = vsURLPCCall + vsIdConversation;
						String vsConversationStart = voJsonArrayConversations.getJSONObject(i).getString("conversationStart");
						String vsConversationEnd = voJsonArrayConversations.getJSONObject(i).getString("conversationEnd");
						voDetailsConversations.put("conversationStart", vsConversationStart.substring(0, 10) + " " + vsConversationStart.substring(12, 20));
						voDetailsConversations.put("conversationEnd", vsConversationEnd.substring(0, 10) + " " + vsConversationEnd.substring(12, 20));
						JSONArray voJSONParticipants = voJsonArrayConversations.getJSONObject(i).getJSONArray("participants");
						for(int j=0;j<voJSONParticipants.length();j++) {
							if(voJSONParticipants.getJSONObject(j).getString("purpose").equals("ivr")) {
								JSONArray voJSONSessions = voJSONParticipants.getJSONObject(j).getJSONArray("sessions");
								for(int k=0;k<voJSONSessions.length();k++) {
									if(voJSONSessions.getJSONObject(k).has("flow")) {
										JSONObject voJSONFlows = voJSONSessions.getJSONObject(k).getJSONObject("flow");
										voDetailsConversations.put("flowName", voJSONFlows.getString("flowName"));
										voDetailsConversations.put("flowType", voJSONFlows.getString("flowType"));
									}
								}
							}
						}
						try {
							voConexionResponseCall = voConexionHttp.executeGet(vsURLConversation, viTimeOut, voHeader, null);
						} catch (Exception e) {
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> " + e.getMessage());
							return;
						}
						String vsJsonCall = voConexionResponseCall.getMensajeRespuesta();
						JSONObject voJsonResponseCall = new JSONObject(vsJsonCall);
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ENDPOINT[" + vsURLConversation + "], STATUS[" + voConexionResponseCall.getCodigoRespuesta() + "], RESPONSE[" + vsJsonCall.replace("\r\n", "") + "]");
						if(voJsonResponseCall.has("participants")) {
							JSONArray voJsonArrayResponseCall = voJsonResponseCall.getJSONArray("participants");
							for(int j=0;j<voJsonArrayResponseCall.length();j++) {
								if(voJsonArrayResponseCall.getJSONObject(j).getJSONObject("attributes").length() > 0) {
									voDetailsConversations.put("ani", String.valueOf(voJsonArrayResponseCall.getJSONObject(j).get("ani")).replace("tel:", ""));
									voDetailsConversations.put("dnis", String.valueOf(voJsonArrayResponseCall.getJSONObject(j).get("dnis")).replace("tel:", ""));
									JSONObject voJSONAttributes = voJsonArrayResponseCall.getJSONObject(j).getJSONObject("attributes");
									if(voJSONAttributes.has("BreadCrumbs")) {
										voSB.append(voJSONAttributes.getString("BreadCrumbs") + "\n");
										voDetailsConversations.put("BreadCrumbs", voJSONAttributes.getString("BreadCrumbs").replace(",", " > "));
										voConversations.put(vsIdConversation, voDetailsConversations);
									} 
									break;
								}
							}
						}
						voDetailsConversations = null;
					}
				}
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> CONVERSATION WITH BREAD CRUMBS [" + voConversations.size() + "]");
				String vsPathExcel = "C:/Appl/BBVA/Reporteador/Excel/Report_" + vsFecha + ".csv";
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> GENERANDO CSV[" + vsPathExcel + "]");
				Excel voExcel = new Excel(vsUUI);
				Integer i = 0;
				Map<String,Object> voHeaders = new HashMap<String,Object>();
				voHeaders.put("conversationId", i++);
				voHeaders.put("conversationStart", i++);
				voHeaders.put("conversationEnd", i++);
				voHeaders.put("ani", i++);
				voHeaders.put("dnis", i++);
				voHeaders.put("flowName", i++);
				voHeaders.put("flowType", i++);
				voHeaders.put("BreadCrumbs", i++);
				voExcel.addInfo(voHeaders, voConversations);
				Boolean vbCreateExcel = voExcel.createCSV(vsPathExcel);
				if(vbCreateExcel) Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> SUCCESSFULLY CREATED FILE");
				else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> ERROR CREATING FILE");
			} else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> CODE[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE ERROR[" + voConexionResponse.getMensajeError() + "]");
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ******************** TERMINANDO *******************\n");
	}

	public static void main(String[] args) {
		new ReportLog();
	}
	
}
