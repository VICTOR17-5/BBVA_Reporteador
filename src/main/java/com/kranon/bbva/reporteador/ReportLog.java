package com.kranon.bbva.reporteador;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.kranon.bbva.reporteador.util.Excel;
import com.kranon.bbva.reporteador.util.Log;
import com.kranon.bbva.reporteador.util.Metrics;
import com.kranon.bbva.reporteador.util.Utilidades;
import com.kranon.conexionHttp.ConexionHttp;
import com.kranon.conexionHttp.ConexionResponse;
import com.kranon.purecloud.PureCloud;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

public class ReportLog {
	private String[] vaConfi = {"client_id","client_secret","Log","portSpark","HorarioVerano"};
	
	private final String vsURLPCDetails = "https://api.mypurecloud.com/api/v2/analytics/conversations/details/query";
	private final String vsURLPCCall = "https://api.mypurecloud.com/api/v2/conversations/calls/";
	private final String vsRutaConfi = "C:/Appl/BBVA/Reporteador/Configuraciones/conf.properties";
	
	private final Integer viTimeOut = 15000;
	
	private String vsToken = "fXCB_63n2l-MjqAzcV-SS21Dd50wkoYtDsGAdBTeMSZqjz1xnZ-gJX2bF6HPPoxSEG9-2I_-4T7sMdAevh15gg";
	private String vsUUI = "";
	private String vsFechaInicio = "";
	private String vsFechaFin = "";
	private String vsFlowName = "";
	
	private ConexionResponse voConexionResponse = null;
	private ConexionResponse voConexionResponseCall = null;
	private Map<String,Map<String,String>> voConversations;
	private ConexionHttp voConexionHttp = null;
	private Utilidades voUti = null;
	private PureCloud pc = null;
	private List<String> vlContactId = null;
	
	private StringBuffer voStringBufferBC;
	
	public ReportLog() {
		
		pc = new PureCloud();
		voUti = new Utilidades();
		if(voUti.getProperty(vaConfi, vsRutaConfi)) {
			Spark.port(Integer.valueOf(vaConfi[3]));
			Log.vsRutaLog = vaConfi[2];
			
		    HashMap<String, String> corsHeaders = new HashMap<>();
		    corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
		    corsHeaders.put("Access-Control-Allow-Origin", "*");
		    corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
		    corsHeaders.put("Access-Control-Allow-Credentials", "true");
		    Filter filter = new Filter() {
		        @Override
		        public void handle(Request request, Response response) throws Exception {
		            corsHeaders.forEach((key, value) -> { response.header(key, value); });
		        }};
		    
		    Spark.after(filter);
		    Spark.get("/collectBreadCrumbs", (request, response) -> { response.status(405); return pc.getJSONError(); });
		    Spark.put("/collectBreadCrumbs", (request, response) -> { response.status(405); return pc.getJSONError(); });
		    Spark.delete("/collectBreadCrumbs", (request, response) -> { response.status(405); return pc.getJSONError(); });
		    Spark.options("/collectBreadCrumbs", (request, response) -> { response.status(405); return pc.getJSONError(); });
		    
		    Spark.post("/collectBreadCrumbs", (request, response) -> {
		    	
		    	voStringBufferBC = new StringBuffer();
		    	vsUUI = java.util.UUID.randomUUID().toString();
		    	pc.setUUI(vsUUI);

		    	if(request.body().equals("")) {
		    		response.status(400);
		    		return pc.getJSONResponse("request.invalid", "The request is invalid.", 400);
		    	}
		    	
		    	JSONObject voJSONRequest = new JSONObject(request.body());
		    	if(!voJSONRequest.has("dateBegin") || !voJSONRequest.has("dateEnd") || !voJSONRequest.has("flowName")){
		    		response.status(400);
		    		return pc.getJSONResponse("request.incomplete", "The request is invalid.", 400);
		    	}
		    	
		    	vsFlowName = voJSONRequest.getString("flowName");
		    	vsFechaInicio = voJSONRequest.getString("dateBegin");
		    	vsFechaFin = voJSONRequest.getString("dateEnd");
		    	Log.vsFecha = vsFechaInicio;
		    	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ******************** REQUEST RECEIVED *******************");
		    	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> REQUEST[" + request.body().replace("\n", "") + "]");
		    	
		    	Date voDateInicio = new SimpleDateFormat("yyyy-MM-dd").parse(vsFechaInicio);
		    	Date voDateFin = new SimpleDateFormat("yyyy-MM-dd").parse(vsFechaFin);
		    	
		    	if(voDateInicio.equals(voDateFin) || voDateInicio.after(voDateFin)) {
		    		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> THE DATES ARE INCORRECT.");
		    		response.status(400);
		    		return pc.getJSONResponse("range.date.incorrect", "The range the date is incorrect.", 400);
		    	}
		    	
		    	
		    	vsToken = pc.getToken(vaConfi[0], vaConfi[1]);
				if(vsToken.equals("ERROR")) { response.status(405); return pc.getJSONError(); } 

				voConexionHttp = new ConexionHttp();
				
				voConversations = new HashMap<String,Map<String,String>>();
		    	HashMap<String, String> voHeader = new  HashMap<String, String>();
				voHeader.put("Authorization", "bearer " + vsToken);
		    	Integer viPag = 0;
		    	do {
		    		viPag++;
		    		vlContactId = new ArrayList<String>();
		    		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> LOADING PAGE NUMBER [" + (viPag) + "]");
		    		
		    		pc.vsHorarioInterval = (vaConfi[4].contentEquals("true")) ? "T05:00:00.000Z" : "T06:00:00.000Z";
		    		String vsBody = pc.getBody(viPag,vsFechaInicio,vsFechaFin);
		    		
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ENDPOINT[" + vsURLPCDetails + "], REQUEST[" + vsBody.replace("\r\n", "") + "]");
					try {
						voConexionResponse = voConexionHttp.executePost(vsURLPCDetails, viTimeOut, vsBody, voHeader);
					} catch (Exception e) {
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> " + e.getMessage());
						break;
					}
					if(voConexionResponse.getCodigoRespuesta() == 200) {
						Map<String,String> voDetailsConversations = null;
						String vsStringJson = voConexionResponse.getMensajeRespuesta();
						Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> STATUS[" + voConexionResponse.getCodigoRespuesta() + 
								"], RESPONSE[" + vsStringJson.replace("\r\n", "") + "]");
						
						if(vsStringJson.equals("{}")) {
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ALL CONVERSATIONS FOUND [0]");
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> FLOWS FOUND WITH THE NAME [bbva_bbvamxap_ivr][0]");
							break;
						}
						
						JSONObject voJsonConversations = new JSONObject(vsStringJson);
						
						if(voJsonConversations.has("conversations")) {
							JSONArray voJsonArrayConversations = voJsonConversations.getJSONArray("conversations");
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ALL CONVERSATIONS FOUND[" + voJsonArrayConversations.length() + "]");
							for(int i=0;i<voJsonArrayConversations.length();i++) {
								voDetailsConversations = new HashMap<String,String>();
								String vsIdConversation = voJsonArrayConversations.getJSONObject(i).getString("conversationId");
								String vsConversationStart = voJsonArrayConversations.getJSONObject(i).getString("conversationStart");
								String vsConversationEnd = voJsonArrayConversations.getJSONObject(i).getString("conversationEnd");
								
								voDetailsConversations.put("conversationStart", vsConversationStart.substring(0, 10) + " " + vsConversationStart.substring(12, 20));
								
								voDetailsConversations.put("conversationEnd", 
										(vsConversationEnd == null || vsConversationEnd == "") ? 
												"" : (vsConversationEnd.substring(0, 10) + " " + vsConversationEnd.substring(12, 20)));
								
								JSONArray voJSONParticipants = voJsonArrayConversations.getJSONObject(i).getJSONArray("participants");
								for(int j=0;j<voJSONParticipants.length();j++) {
									if(voJSONParticipants.getJSONObject(j).getString("purpose").equals("ivr")) {
										JSONArray voJSONSessions = voJSONParticipants.getJSONObject(j).getJSONArray("sessions");
										for(int k=0;k<voJSONSessions.length();k++) {
											if(voJSONSessions.getJSONObject(k).has("flow")) {
												JSONObject voJSONFlows = voJSONSessions.getJSONObject(k).getJSONObject("flow");
												if( voJSONFlows.getString("flowName").equals(vsFlowName)) {
													vlContactId.add(vsIdConversation);
													voDetailsConversations.put("flowName", voJSONFlows.getString("flowName"));
													voDetailsConversations.put("flowType", voJSONFlows.getString("flowType"));
													voDetailsConversations.put("ani", voJSONSessions.getJSONObject(k).getString("ani"));
													voDetailsConversations.put("dnis", voJSONSessions.getJSONObject(k).getString("dnis"));
													voConversations.put(vsIdConversation, voDetailsConversations);
												}
											}
										}
									}
								}
								voDetailsConversations = null;
							}
							
							Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> FLOWS FOUND WITH THE NAME [bbva_bbvamxap_ivr][" + vlContactId.size()	 + "]");
							
							Integer viContadorEncontrados = 0;
							for(String vsContactId : vlContactId) {
								String vsURLConversation = vsURLPCCall + vsContactId;
								viContadorEncontrados++;
								Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> [" + (viContadorEncontrados) + "] ENDPOINT[" + vsURLConversation + "]");
								try {
									voConexionResponseCall = voConexionHttp.executeGet(vsURLConversation, viTimeOut, voHeader, null);
								} catch (Exception e) {
									Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> CONTACT_ID [" + vsContactId + "] : " + e.getMessage());
								}
								JSONObject voJsonResponseCall = new JSONObject(voConexionResponseCall.getMensajeRespuesta());
								Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> [" + (viContadorEncontrados) + 
										"] STATUS[" + voConexionResponseCall.getCodigoRespuesta() + "], RESPONSE[" + voConexionResponseCall.getMensajeRespuesta().replace("\r\n", "") + "]");
								
								if(voJsonResponseCall.has("participants")) {
									JSONArray voJsonArrayResponseCall = voJsonResponseCall.getJSONArray("participants");
									for(int j=0;j<voJsonArrayResponseCall.length();j++) {
										if(voJsonArrayResponseCall.getJSONObject(j).getJSONObject("attributes").length() > 0) {
											
											Map<String,String> voDetails = voConversations.get(vsContactId);
											voDetails.put("ani", String.valueOf(voJsonArrayResponseCall.getJSONObject(j).get("ani")).replace("tel:", ""));
											voDetails.put("dnis", String.valueOf(voJsonArrayResponseCall.getJSONObject(j).get("dnis")).replace("tel:", ""));
											JSONObject voJSONAttributes = voJsonArrayResponseCall.getJSONObject(j).getJSONObject("attributes");
											if(voJSONAttributes.has("BreadCrumbs")) {
												voStringBufferBC.append(voJSONAttributes.getString("BreadCrumbs") + "\n");
												voDetails.put("BreadCrumbs", voJSONAttributes.getString("BreadCrumbs").replace(",", ">"));
												Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> [" + (viContadorEncontrados) + "] BREADCRUMBS[" + voJSONAttributes.getString("BreadCrumbs") + "]");
												voConversations.replace(vsContactId, voDetails);
											} else 
												Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> [" + (viContadorEncontrados) + "] BREADCRUMBS[NOT FOUND] IN CONTACT ATTRIBUTES[" + voJSONAttributes.toString() + "]");
											break;
										}
									}
								}
							}
						}
					} else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> CODE[" + voConexionResponse.getCodigoRespuesta() + "], MESSAGE ERROR[" + voConexionResponse.getMensajeError() + "]");
		    	}while(true);
		    	
		    	Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> TOTAL CONVERSATION WITH BREADCRUMBS[" + voConversations.size() + "]");

		    	String vsPathExcel = "C:/Appl/BBVA/Reporteador/CSV/Report_" + vsFechaInicio + ".csv";
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
				if(voExcel.createCSV(vsPathExcel)) Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> SUCCESSFULLY CREATED FILE");
				else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> ERROR CREATING FILE");
				
				Map<String,Integer> voMapBreadCrumbs = new HashMap<String,Integer>();
				voMapBreadCrumbs.put("bbvamxap_001", 0);
				voMapBreadCrumbs.put("bbvamxap_002", 0);
				voMapBreadCrumbs.put("bbvamxap_007", 0);
				voMapBreadCrumbs.put("bbvamxap_008", 0);
				voMapBreadCrumbs.put("bbvamxap_009", 0);
				voMapBreadCrumbs.put("bbvamxap_010", 0);
				voMapBreadCrumbs.put("bbvamxap_011", 0);
				voMapBreadCrumbs.put("bbvamxap_012", 0);
				voMapBreadCrumbs.put("bbvamxap_013", 0);
				voMapBreadCrumbs.put("bbvamxap_014", 0);
				voMapBreadCrumbs.put("bbvamxap_015", 0);
				voMapBreadCrumbs.put("identification_001", 0);
				voMapBreadCrumbs.put("identification_002", 0);
				voMapBreadCrumbs.put("identification_003", 0);
				voMapBreadCrumbs.put("identification_004", 0);
				voMapBreadCrumbs.put("identification_005", 0);
				
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> COUNT METRICS");
				Metrics voMetrics = new Metrics(voStringBufferBC);
				voMetrics.ContainsBreadCrumbs(voMapBreadCrumbs);				
				voMapBreadCrumbs.put("bbvamxap_002,bbvamxap_004", voMetrics.ContainsBreadCrumbs("bbvamxap_002,bbvamxap_004"));
				
				String vsRutaMetrics = "C:/Appl/BBVA/Reporteador/Metricas/reporte-bbvamxap-" + vsFechaInicio + ".txt";
				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> GENERATING REPORT METRICS[" + vsRutaMetrics + "]");
				
				if(voMetrics.GenerateMetrics(vsRutaMetrics, voMapBreadCrumbs, vsFechaInicio, vsFechaFin, vsFlowName, voConversations.size())) 
					Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> SUCCESSFULLY CREATED FILE METRICS");
				else Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][ERROR] ---> ERROR CREATING FILE METRICS");

				Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ******************** SENDING RESPONSE *******************\n");
				response.status(200);
				return pc.getJSONResponse("process.success", "Finished process successfully.", 200);
		    });
		}
	}
	
	public static void main(String[] args) {
		new ReportLog();
	}
	
}
