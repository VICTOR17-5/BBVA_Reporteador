package com.kranon.bbva.reporteador.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class Excel {
	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private HSSFRow row;
	private HSSFCell cell;
	private int rowCount = -1;
	private String vsUUI;
	
	
	public Excel(String vsUUI) {
		workbook = new HSSFWorkbook();
		sheet = workbook.createSheet("Java");
		this.vsUUI = vsUUI;
	}
	
	public void addInfo(Map<String, Object> voHeaders, Map<String, Map<String, String>> voContents) {
		row = sheet.createRow(++rowCount);
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ADD HEADERS[conversationId,conversationStart,conversationEnd,ani,dnis,flowName,flowType,BreadCrumbs]");
		for (Entry<String, Object> voEntry : voHeaders.entrySet()) {
			String vsValue = voEntry.getKey();
			Object voIndex = voEntry.getValue();
			HSSFCell cell = row.createCell((Integer) voIndex);
			if (vsValue instanceof String) {
				cell.setCellValue((String) vsValue);
			}
		}
		Log.GuardaLog("[" + new Date() + "][" + vsUUI + "][ReportLog][INFO] ---> ADDING INFORMATION FROM CONVERSATIONS");
		for (Map.Entry<String, Map<String, String>> voMapContent : voContents.entrySet()) {
			row = sheet.createRow(++rowCount);
			for (Entry<String, Object> voHead : voHeaders.entrySet()) {
				String key = voHead.getKey();
				Object index = voHead.getValue();
				String id = voMapContent.getKey();
				Map<String, String> value = voMapContent.getValue();
				Object valor = value.get(key);
				cell = row.createCell((Integer) index);
				if((Integer)index == 0) {
					cell.setCellValue((String) id);
				} else if (valor instanceof String) {
					cell.setCellValue((String) valor);
				} else if (valor instanceof Integer) {
					cell.setCellValue((Integer) valor);
				}
			}
		}	
	}
	
	public void createExcel(String vsPath) {
		File voFile = new File(vsPath);
		if (voFile.exists()) voFile.delete();
		try (FileOutputStream outputStream = new FileOutputStream(voFile)) {			
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			Log.GuardaLog("[" + new Date() + "][createExcel][ERROR] ---> " + e.getMessage());
		}catch (IOException e) {
			Log.GuardaLog("[" + new Date() + "][createExcel][ERROR] ---> " + e.getMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	public boolean createCSV(String vsPath) {
		StringBuffer voSBData = new StringBuffer();
		Row row;
		Cell cell;
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				cell = cellIterator.next();
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					voSBData.append(cell.getBooleanCellValue() + ",");
					break;
				case Cell.CELL_TYPE_NUMERIC:
					voSBData.append(cell.getNumericCellValue() + ",");
					break;
				case Cell.CELL_TYPE_STRING:
					if (!cell.getStringCellValue().equals("N/A"))
						voSBData.append(cell.getStringCellValue() + ",");
					else
						voSBData.append("" + ",");
					break;
				case Cell.CELL_TYPE_BLANK:
					voSBData.append("0" + ",");
					break;
				default:
					voSBData.append(cell + ",");
				}
			}
			
			voSBData.append("\n");
		}
		File voFile = new File(vsPath);
		if(voFile.exists()) voFile.delete();
		if(!voFile.getParentFile().exists()) voFile.mkdirs();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(vsPath);
			fos.write(voSBData.toString().getBytes());
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
