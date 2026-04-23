package org.example.web.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class ExcelReader implements AutoCloseable {

    private final Workbook workbook;
    private final Sheet sheet;

    public ExcelReader(String classpathResource) {
        InputStream in = getClass().getClassLoader().getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found on classpath: " + classpathResource);
        }
        try {
            this.workbook = new XSSFWorkbook(in);
            this.sheet = workbook.getSheetAt(0);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open workbook: " + classpathResource, e);
        }
    }

    public String read(int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            throw new IllegalArgumentException("Row " + rowIndex + " does not exist");
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            throw new IllegalArgumentException("Cell at (" + rowIndex + "," + columnIndex + ") does not exist");
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }
}
