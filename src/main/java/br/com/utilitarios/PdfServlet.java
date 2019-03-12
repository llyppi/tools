/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.utilitarios;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Felipe L. Garcia
 */
public class PdfServlet  extends HttpServlet {
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("PdfServlet");
        
        String value = request.getParameter("pdfbyte");
        if (value==null) {
            return;
        }        
        System.out.println("pdfbyte");
        
        ByteArrayInputStream input = null;
        OutputStream output = null;
        try {
            byte[] bty = value.getBytes("UTF-16LE");
            
            input = new ByteArrayInputStream(bty);
            
            output = response.getOutputStream();
            
            int contentLength = input.available();

            response.reset();
            response.setContentLength(contentLength);
            response.setHeader(
                    "Content-disposition", "attachment; filename=\"document.pdf \"");
//            response.setHeader("Cache-Control", "cache, must-revalidate");
            
            while (contentLength-- > 0) {
                output.write(input.read());
            }

            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
            }
        }
    }
}
