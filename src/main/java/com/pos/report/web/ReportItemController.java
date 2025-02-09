package com.pos.report.web;

import com.pos.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("reports/lab/")
public class ReportItemController {

    @Autowired
    ServletContext context;

    @Autowired
    ReportService reportService;
    
    // @RequestMapping(value = "surat-jalan*", method = RequestMethod.GET)
    // public void RptSuratJalan(HttpServletRequest request, HttpServletResponse response) {
    //     String uri = request.getRequestURI();
    //     String format = uri.substring(uri.lastIndexOf(".") + 1);
    //     String imgPath = context.getRealPath("/img") + System.getProperty("file.separator");
    //     System.out.println("uri : " + uri + ", context : " + context.getRealPath("/templates/jrxml/report/"));
    //     String id = request.getParameter("id");
    //     System.out.println("id" + id);

    //     Map<String, Object> parameters = new HashMap<>();
    //     parameters.put("imgPath", imgPath);
    //     parameters.put("id", id);

    //     reportService.generateReport("surat-jalan", format, parameters, response, "surat-jalan");
    // }

    @RequestMapping(value = "penerimaan*", method = RequestMethod.GET)
    public void RptSuratJalan(HttpServletRequest request, HttpServletResponse response) {
    String uri = request.getRequestURI();
    String format = uri.substring(uri.lastIndexOf(".") + 1);
    
    // Path gambar di dalam aplikasi
    String imgPath = context.getRealPath("/img") + System.getProperty("file.separator");
    String logoPath = imgPath + "logo.png";  // Pastikan file "logo.png" ada di folder img

    System.out.println("URI : " + uri + ", Context : " + context.getRealPath("/templates/jrxml/report/"));

    String id = request.getParameter("id");
    System.out.println("ID: " + id);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("imgPath", imgPath);
    parameters.put("logoPath", logoPath);  // Tambahkan path gambar ke parameter
    parameters.put("id", id);

    reportService.generateReport("Blank_A4", format, parameters, response, "Blank_A4");
}

}
