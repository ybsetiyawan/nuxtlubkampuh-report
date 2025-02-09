/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pos.report.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ustadho
 */
@Service
public class ReportService {

    @Autowired
    private ApplicationContext appContext;
    @Autowired
    ServletContext servletContext;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    DataSource dataSource;

    JasperPrint jasperPrint;

    public void generateView(String report, String format, Map<String, Object> parameters,
            Object datasource, HttpServletResponse response, String fileName) {
        response.setContentType("text/html");
        JRBeanCollectionDataSource dataSource = null;

        if (datasource != null) {
            dataSource = new JRBeanCollectionDataSource(convertObjectToList(datasource));
        }
        System.out.println("dataSource");
        System.out.println(dataSource);
        String reportResource = "classpath:templates/jrxml/simpeg/" + report + ".jrxml";
        System.out.println("context.getRealPath" + appContext.getResource("/").toString());
        System.out.println("reportResource: " + reportResource);

        try {
            InputStream jrxmlInput = resourceLoader.getResource(reportResource).getInputStream();
            JasperDesign design = JRXmlLoader.load(jrxmlInput);
            JasperReport jasperReport = JasperCompileManager.compileReport(design);
            jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            if (format.equalsIgnoreCase("pdf")) {
                exportToPdf(fileName, report, response);
            } else if (format.contains("xls")) {
                exportToXlsx(fileName, report, response);
            } else if (format.equalsIgnoreCase("csv")) {
                exportToCsv(fileName, response);
            } else if (format.equalsIgnoreCase("html")) {
                exportToHtml(fileName, response);
            }
        } catch (JRException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<?> convertObjectToList(Object obj) {
        List<?> list = new ArrayList<Object>();
        if (obj.getClass().isArray()) {
            list = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection) {
            list = new ArrayList<>((Collection<?>) obj);
        }
        return list;
    }

    public void exportToPdf(String fileName, String author, HttpServletResponse response) {

        // print report to file
        JRPdfExporter exporter = new JRPdfExporter();
        ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReportStream));

        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor(author);
        exportConfig.setEncrypted(false);
        exportConfig.setAllowedPermissionsHint("PRINTING");

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);
        try {
            exporter.exportReport();
            response.setContentType("application/pdf");
            response.setHeader("Content-Length", String.valueOf(pdfReportStream.size()));
            response.setHeader("Content-Disposition", "Inline; filename=" + fileName + ".pdf;");

            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(pdfReportStream.toByteArray());
            responseOutputStream.close();
            pdfReportStream.close();
        } catch (JRException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exportToXlsx(String fileName, String sheetName, HttpServletResponse response) {
        JRXlsxExporter exporter = new JRXlsxExporter();
        ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfReportStream));

        SimpleXlsxReportConfiguration reportConfig = new SimpleXlsxReportConfiguration();
        reportConfig.setSheetNames(new String[]{sheetName});

        exporter.setConfiguration(reportConfig);

        try {
            exporter.exportReport();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Length", String.valueOf(pdfReportStream.size()));
            response.setHeader("Content-Disposition", "Inline; filename=" + fileName + ".xlsx;");

            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(pdfReportStream.toByteArray());
            responseOutputStream.close();
            pdfReportStream.close();
        } catch (JRException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exportToCsv(String fileName, HttpServletResponse response) {
        JRCsvExporter exporter = new JRCsvExporter();
        ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(pdfReportStream));

        try {
            exporter.exportReport();
            response.setContentType("text/csv");
            response.setHeader("Content-Length", String.valueOf(pdfReportStream.size()));
            response.setHeader("Content-Disposition", "Inline; filename=" + fileName + ".xlsx;");

            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(pdfReportStream.toByteArray());
            responseOutputStream.close();
            pdfReportStream.close();
        } catch (JRException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exportToHtml(String fileName, HttpServletResponse response) {
        HtmlExporter exporter = new HtmlExporter();
        ByteArrayOutputStream pdfReportStream = new ByteArrayOutputStream();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(pdfReportStream));

        try {
            exporter.exportReport();
            response.setContentType("text/html");
            response.setHeader("Content-Length", String.valueOf(pdfReportStream.size()));
            response.setHeader("Content-Disposition", "Inline; filename=" + fileName + ".xlsx;");

            OutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(pdfReportStream.toByteArray());
            responseOutputStream.close();
            pdfReportStream.close();
        } catch (JRException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReportService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void generateReport(String jasperFile, String format, Map<String, Object> parameters,
                               HttpServletResponse response, String outputName) {
        String reportResource = "classpath:templates/jrxml/report/" + jasperFile + ".jasper";
        JasperReport jasperReport;
        try (InputStream jasperStream = resourceLoader.getResource(reportResource).getInputStream();
                Connection con=dataSource.getConnection()) {
            jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            parameters.put("IS_IGNORE_PAGINATION", format.contains("xls"));
            jasperReport.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);
            if (format.contains("xls")) {
                JRXlsxExporter exporterXls = new JRXlsxExporter();
                SimpleXlsxReportConfiguration reportConfigXLS = new SimpleXlsxReportConfiguration();
                reportConfigXLS.setSheetNames(new String[] { outputName });
                exporterXls.setConfiguration(reportConfigXLS);
                exporterXls.setExporterInput(new SimpleExporterInput(jasperPrint));

                exporterXls.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
                response.setHeader("Content-Disposition", "attachment;filename="+outputName+".xlsx");
                response.setContentType("application/octet-stream");
                exporterXls.exportReport();
            }else{
                response.setHeader("Content-disposition", "attachment; filename=" + outputName + ".pdf");
                response.setContentType("application/x-pdf");
                final OutputStream outStream = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, outStream);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JRException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public File exportPdfReport(String jasperFile, String out, Map<String, Object> parameters) throws IOException {
        File pdf = null;

        String reportResource = "classpath:templates/jrxml/simpeg/" + jasperFile + ".jasper";
        JasperReport jasperReport;
        try (InputStream jasperStream = resourceLoader.getResource(reportResource).getInputStream();
             Connection con=dataSource.getConnection()) {
            jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, con);

            // export it!
            pdf = File.createTempFile(out + ".", ".pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(pdf));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JRException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return pdf;
    }


}
