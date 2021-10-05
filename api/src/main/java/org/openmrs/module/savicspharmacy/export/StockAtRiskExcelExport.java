/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.export;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.savicspharmacy.api.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author anatoleabe
 */
public class StockAtRiskExcelExport {
	
	private XSSFWorkbook workbook;
	
	private XSSFSheet sheet;
	
	private List<Item> listItems;
	
	private Boolean atriskOnly;
	
	@Autowired
	private MessageSourceService messageSourceService;
	
	public StockAtRiskExcelExport(List<Item> listItems, Boolean atriskOnly) {
		this.listItems = listItems;
		this.atriskOnly = atriskOnly;
		workbook = new XSSFWorkbook();
	}
	
	private void writeHeaderLine() {
		sheet = workbook.createSheet("Stock à risque");
		
		Row row = sheet.createRow(0);
		XSSFFont font = workbook.createFont();
		font.setBold(true);
		font.setFontHeight(16);
		
		XSSFColor color = new XSSFColor(new java.awt.Color(244, 204, 204), null);
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		
		cellStyle.setFillForegroundColor(color);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setFont(font);
		
		int index = 0;
		createCell(row, index++, "Code", cellStyle);
		createCell(row, index++, "Designation", cellStyle);
		createCell(row, index++, "Unité", cellStyle);
		createCell(row, index++, "Route", cellStyle);
		createCell(row, index++, "Min", cellStyle);
		createCell(row, index++, "Max", cellStyle);
		createCell(row, index++, "Quantité Virtuelle", cellStyle);
		createCell(row, index++, "Quantité en stock", cellStyle);
		createCell(row, index++, "Quantité expirée", cellStyle);
		createCell(row, index++, "Statut quantité", cellStyle);
	}
	
	private void createCell(Row row, int columnCount, Object value, CellStyle style) {
		sheet.autoSizeColumn(columnCount);
		Cell cell = row.createCell(columnCount);
		if (value instanceof Integer) {
			cell.setCellValue((Integer) value);
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else {
			cell.setCellValue((String) value);
		}
		cell.setCellStyle(style);
	}
	
	private void writeDataLines() {
		int rowCount = 1;
		
		CellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setFontHeight(14);
		style.setFont(font);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		
		for (Item item : listItems) {
			if (atriskOnly && (item.getNumberOfExpiredLots() > 0 || item.getSoh() < item.getStockMin())) {
				
				Row row = sheet.createRow(rowCount++);
				int columnCount = 0;
				
				createCell(row, columnCount++, item.getId(), style);
				createCell(row, columnCount++, item.getName(), style);
				createCell(row, columnCount++, item.getUnit().getName(), style);
				createCell(row, columnCount++, item.getRoute().getName(), style);
				createCell(row, columnCount++, item.getStockMin(), style);
				createCell(row, columnCount++, item.getStockMax(), style);
				createCell(row, columnCount++, item.getVirtualstock(), style);
				createCell(row, columnCount++, item.getSoh(), style);
				createCell(row, columnCount++, item.getExpiredQuantity(), style);
				String status = (item.getSoh() != null && item.getStockMax() != null && item.getSoh() > item.getStockMax()) ? "Quantité excédentaire"
				        : (item.getSoh() != null && item.getStockMin() != null && item.getSoh() < item.getStockMin()) ? "En dessous de la quantité minimale"
				                : "";
				createCell(row, columnCount++, status, style);
			} else if (!atriskOnly) {
				Row row = sheet.createRow(rowCount++);
				int columnCount = 0;
				
				createCell(row, columnCount++, item.getId(), style);
				createCell(row, columnCount++, item.getName(), style);
				createCell(row, columnCount++, item.getUnit().getName(), style);
				createCell(row, columnCount++, item.getRoute().getName(), style);
				createCell(row, columnCount++, item.getStockMin(), style);
				createCell(row, columnCount++, item.getStockMax(), style);
				createCell(row, columnCount++, item.getVirtualstock(), style);
				createCell(row, columnCount++, item.getSoh(), style);
				createCell(row, columnCount++, item.getExpiredQuantity(), style);
				String status = (item.getSoh() != null && item.getStockMax() != null && item.getSoh() > item.getStockMax()) ? "Quantité excédentaire"
				        : (item.getSoh() != null && item.getStockMin() != null && item.getSoh() < item.getStockMin()) ? "En dessous de la quantité minimale"
				                : "";
				createCell(row, columnCount++, status, style);
			}
		}
	}
	
	public void export(HttpServletResponse response) throws IOException {
		writeHeaderLine();
		writeDataLines();
		
		ServletOutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		workbook.close();
		
		outputStream.close();
		
	}
}
