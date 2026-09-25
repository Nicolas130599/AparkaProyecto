package com.aparka.controller;

import com.aparka.model.Zona;
import com.aparka.service.ZonaService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

/**
 * Exporta el ranking de flujo vehicular a un archivo Excel (.xlsx) usando Apache POI,
 * para que el administrador pueda descargar y compartir el reporte fuera del sistema.
 */
@WebServlet("/admin/reporte-excel")
public class ReporteExcelServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ReporteExcelServlet.class);

    private final ZonaService zonaService = new ZonaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Zona> ranking = zonaService.obtenerRankingFlujoVehicular();

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                Sheet hoja = workbook.createSheet("Flujo Vehicular");

                CellStyle estiloEncabezado = workbook.createCellStyle();
                Font fuenteEncabezado = workbook.createFont();
                fuenteEncabezado.setBold(true);
                fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
                estiloEncabezado.setFont(fuenteEncabezado);
                estiloEncabezado.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
                estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                String[] columnas = {"#", "Zona", "Ubicación", "Ocupados", "Capacidad", "% Ocupación", "Nivel de Flujo", "Tarifa/hora"};
                Row filaEncabezado = hoja.createRow(0);
                for (int i = 0; i < columnas.length; i++) {
                    Cell celda = filaEncabezado.createCell(i);
                    celda.setCellValue(columnas[i]);
                    celda.setCellStyle(estiloEncabezado);
                }

                int numeroFila = 1;
                for (Zona z : ranking) {
                    Row fila = hoja.createRow(numeroFila);
                    fila.createCell(0).setCellValue(numeroFila);
                    fila.createCell(1).setCellValue(z.getNombre());
                    fila.createCell(2).setCellValue(z.getUbicacion());
                    fila.createCell(3).setCellValue(z.getCapacidadOcupada());
                    fila.createCell(4).setCellValue(z.getCapacidadTotal());
                    fila.createCell(5).setCellValue(Math.round(z.getPorcentajeOcupacion()) + "%");
                    fila.createCell(6).setCellValue(z.getNivelFlujo());
                    fila.createCell(7).setCellValue("S/ " + z.getTarifaHora());
                    numeroFila++;
                }

                for (int i = 0; i < columnas.length; i++) {
                    hoja.autoSizeColumn(i);
                }

                resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                resp.setHeader("Content-Disposition", "attachment; filename=\"reporte_flujo_vehicular.xlsx\"");

                try (OutputStream out = resp.getOutputStream()) {
                    workbook.write(out);
                }
            }

            log.info("Reporte Excel de flujo vehicular generado ({} zonas)", ranking.size());

        } catch (SQLException e) {
            log.error("Error al generar el reporte Excel", e);
            throw new ServletException("Error al generar el reporte Excel", e);
        }
    }
}
