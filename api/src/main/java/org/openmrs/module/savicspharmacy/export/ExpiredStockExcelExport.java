/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.savicspharmacy.export;

import java.io.IOException;
import java.util.Date;
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
import org.openmrs.module.savicspharmacy.api.entity.ItemsLine;

/**
 * @author anatoleabe
 */
public class ExpiredStockExcelExport {
	
	private XSSFWorkbook workbook;
	
	private XSSFSheet sheet;
	
	private List<ItemsLine> listItemsLines;
	
	private boolean expiredOnly;
	
	public ExpiredStockExcelExport(List<ItemsLine> listItemsLines, boolean expiredOnly) {
		this.listItemsLines = listItemsLines;
		this.expiredOnly = expiredOnly;
		workbook = new XSSFWorkbook();
	}
	
	private void writeHeaderLine() {
		sheet = workbook.createSheet("Stock expiré");
		
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
		createCell(row, index++, "Désignation", cellStyle);
		createCell(row, index++, "Lot", cellStyle);
		createCell(row, index++, "Forme", cellStyle);
		createCell(row, index++, "Voie d’admission", cellStyle);
		createCell(row, index++, "Quantité", cellStyle);
		createCell(row, index++, "Date expiration", cellStyle);
		createCell(row, index++, "Statut", cellStyle);
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
		
		for (ItemsLine itemline : listItemsLines) {
			Row row = sheet.createRow(rowCount++);
			if (expiredOnly && (new Date()).after(itemline.getItemExpiryDate())) {
				int columnCount = 0;
				createCell(row, columnCount++, itemline.getId(), style);
				createCell(row, columnCount++, itemline.getItem().getName(), style);
				createCell(row, columnCount++, itemline.getItemBatch(), style);
				createCell(row, columnCount++, itemline.getItem().getUnit().getName(), style);
				createCell(row, columnCount++, itemline.getItem().getRoute().getName(), style);
				createCell(row, columnCount++, itemline.getItemSoh(), style);
				createCell(row, columnCount++, itemline.getItemExpiryDate().toString(), style);
				String status = "";
				if ((new Date()).after(itemline.getItemExpiryDate())) {
					status = "Expiré";
				}
				createCell(row, columnCount++, status, style);
			} else if (!expiredOnly) {
				int columnCount = 0;
				createCell(row, columnCount++, itemline.getId(), style);
				createCell(row, columnCount++, itemline.getItem().getName(), style);
				createCell(row, columnCount++, itemline.getItemBatch(), style);
				createCell(row, columnCount++, itemline.getItem().getUnit().getName(), style);
				createCell(row, columnCount++, itemline.getItem().getRoute().getName(), style);
				createCell(row, columnCount++, itemline.getItemSoh(), style);
				createCell(row, columnCount++, itemline.getItemExpiryDate().toString(), style);
				String status = "";
				if ((new Date()).after(itemline.getItemExpiryDate())) {
					status = "Expiré";
				}
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
