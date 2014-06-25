/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.ansible;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.infcomtec.javahtml.JHFragment;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
@WebServlet(name = "EditYml", urlPatterns = {"/EditYml"})
public class EditYml extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fnam = request.getParameter("file");
        if (request.getParameter("oops") != null) {
            response.sendRedirect("EditAny?file=" + fnam);
            return;
        }
        File f = new File(fnam);
        AnsObject o;
        if (request.getParameter("edit") != null) {
            o = new AnsObject(null, f, request.getParameter("edit"));
        } else if (f.exists()) {
            try {
                o = new AnsObject(null, f);
            } catch (IOException ex) {
                // not YAML, send to general editor to fix
                response.sendRedirect("EditAny?warn=true&file=" + fnam);
                return;
            }
        } else {
            o = new AnsObject(null, f, "{}");
        }
        if (request.getParameter("save") != null) {
            try (PrintWriter pw = new PrintWriter(f)) {
                pw.print(request.getParameter("edit"));
            }
            // and reload the file!
            try {
                o = new AnsObject(null, f);
                response.sendRedirect("index.jsp");
            } catch (Exception ex) {
                // not YAML (anymore), send to general editor to fix
                response.sendRedirect("EditAny?warn=true&file=" + fnam);
                return;
            }
        }
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            String title;
            if (fnam.length() > 28) {
                title = fnam.substring(fnam.length() - 28);
            } else {
                title = fnam;
            }
            out.println("<title>" + title + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<form action=\"EditYml\" method=\"POST\">");
            out.println("<input type=\"hidden\" name=\"file\" value=\"" + fnam + "\" />");
            out.println("<h1>" + fnam + "</h1>");
            String toHtml;
            if (request.getParameter("json") == null) {
                out.println("<p><input type=\"submit\" name=\"json\" value=\"JSON mode\">");
                toHtml = o.makeString();
            } else {
                out.println("<p><input type=\"submit\" name=\"notjson\" value=\"YAML mode\">");
                Object tmp = YamlJson.yaml2Json(o.makeString());
                if (tmp instanceof JSONObject)
                    toHtml = ((JSONObject)tmp).toString(4);
                else if (tmp instanceof JSONArray)
                    toHtml = ((JSONArray)tmp).toString(4);
                else
                    toHtml=tmp.toString();
            }
            out.println("This does not look right; <input type=\"submit\" name=\"oops\" value=\"Open in text editor instead\" /></p>");
            out.println("<textarea name=\"edit\" rows=\"36\" cols=\"150\">");
            out.println(JHFragment.html(toHtml));
            out.println("</textarea><br />");
            out.println("<input type=\"submit\" name=\"save\" value=\"Save\" />");
            out.println("<A href=\"index.jsp\">Cancel: return to the main page, discarding edits.</a>");
            out.println("</form>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
