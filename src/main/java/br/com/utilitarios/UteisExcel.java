/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Felipe L. Garcia
 */
public class UteisExcel {
    public static byte[] export(Object[][] datatypes) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Datatypes in Java");                
        
//        Object[][] datatypes = {
//            {"Datatype", "Type", "Size(in bytes)"},
//            {"int", "Primitive", 2},
//            {"float", "Primitive", 4},
//            {"double", "Primitive", 8},
//            {"char", "Primitive", 1},
//            {"String", "Non-Primitive", "No fixed size"}
//        };

        int rowNum = 0;

        for (Object[] datatype : datatypes) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : datatype) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                }
                if (field instanceof Date) {
                    cell.setCellValue((Date) field);
                }
                if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
                if (field instanceof Double) {
                    cell.setCellValue((Double) field);
                }
                if (field instanceof Boolean) {
                    cell.setCellValue((Boolean) field);
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return outputStream.toByteArray();
    }
}
