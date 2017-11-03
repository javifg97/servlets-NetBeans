/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package articulos;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author javier.fernandez3
 */
public class servlet extends HttpServlet {

    private Connection con;

    public void init(ServletConfig conf) throws ServletException {

        super.init(conf);
        String driver = null;
        driver = conf.getInitParameter("cadenaDriver");  
        
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException cne) {
        }

        con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost/noticias", "root", "");

        } catch (SQLException sql) {
        }
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String usuario = req.getParameter("user");
        String boton = req.getParameter("accion");
        String autorizado;
        String codigo = null;
        String consulta = "";

        Statement stmt = null;
                res.setContentType("text/html");
        PrintWriter pw = res.getWriter();
        try {
            stmt = con.createStatement();
        

        consulta = "SELECT CodUsuario FROM usuarios WHERE Nombre ='" + req.getParameter("user") + "' AND Clave ='"+req.getParameter("pass")+"'";

        ResultSet rs = null;
       
            rs = stmt.executeQuery(consulta);
                        rs.next();
                codigo = rs.getString(1);
            
            
                    
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            pw.println("salsa");
        }


        try {
            autorizado = autorización(req);

            if (boton.equals("Leer_Articulos")
                    && !autorizado.equals("ACCESO DENEGADO")) {
                leerArticulo(req, res);
            } else if (boton.equals("Enviar")
                    && autorizado.equals("POST")) {
                
                enviarArticulo(req, res,codigo);
            } else if (boton.equals("Enviar_Articulos")) {
                if (usuario == null) {
                    usuario = " ";
                }

                pw.println("<HTML>");
                pw.println("<HEAD><TITLE>Envío de Artículos</TITLE></HEAD>");
                pw.println("<BODY>");
                pw.println("<CENTER><H1>Envío de Artículos</H2></CENTER>");
                pw.println("<FORM  ACTION=http://localhost:8080/javierfernandez/articulos/articulos.servlet");
                pw.println("METHOD=POST>");
                pw.println("<H2>Envío de Artículos</H2>");
                pw.println("<P>Por este nombre te reconoce el Sistema: <BR>");
                pw.println("<INPUT NAME=\"user\" TYPE=\"text\" VALUE='" + usuario + "'");
                pw.println("SIZE=16 MAXLENGTH=16><BR>");
                pw.println("Introduce tu contraseña: <BR>");
                pw.println("<INPUT NAME=\"pass\" TYPE=\"password\" SIZE=8 MAXLENGTH=8>");
                pw.println("</P>");
                pw.println("<H3>Título del Artículo a enviar:</H3>");
                pw.println("<P><INPUT NAME=titulo TYPE=text SIZE=25");
                pw.println("MAXLENGTH=50></P>");
                pw.println("<H3>Cuerpo del Artículo</H3>");
                pw.println("<P><TEXTAREA NAME=cuerpo ROWS=10 COLS=50>");
                pw.println("</TEXTAREA></P>");
                pw.println("<P><INPUT NAME=accion TYPE=submit VALUE=\"Enviar\">");
                pw.println("</FORM>");
                pw.println("</BODY></HTML>");
            } else {

                pw.println("<html>");
                pw.println("<head><title>Acceso Denegado</title></head>");
                pw.println("<body>");
                pw.println("Se ha producido un error revisa el usuario y contraseña");
                pw.println("</body></html>");

            }

        } catch (SQLException e) {

        }

    }

    public String autorización(HttpServletRequest req) throws SQLException {

        Statement stmt = null;
        try {
            stmt = con.createStatement();
        } catch (SQLException ex) {

        }
        String consulta;
        ResultSet rs = null;
        String valido = "";
        String usuario = req.getParameter("user");
        String clave = req.getParameter("pass");
        String permiso = "";

        consulta = "SELECT admitirEnvio FROM usuarios WHERE Nombre = '" + usuario + "' AND clave = '" + clave + "'";
        try {
            rs = stmt.executeQuery(consulta);
            while (rs.next()) {
                valido = rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {

        }

        if (valido.equals("")) {
            permiso = "ACCESO DENEGADO";
        } else {

            if (valido.equals("N")) {
                permiso = "GET";

            } else if (valido.equals("S")) {
                permiso = "POST";
            }
        }

        return permiso;

    }

    public void enviarArticulo(HttpServletRequest req, HttpServletResponse res,String codigo)
            throws IOException, SQLException {
        res.setContentType("text/html");
        PrintWriter pw = res.getWriter();
        
        Statement stmt = con.createStatement();
        String consulta = "";
        String usuario = req.getParameter("usuario");

        

        
        pw.println("<HTML>");
        pw.println("<HEAD></HEAD>");
        pw.println("<BODY>");

        consulta = "INSERT INTO articulos VALUES( '" + req.getParameter("titulo") + "','" + codigo + "','" + req.getParameter("cuerpo") + "')";
        int result = stmt.executeUpdate(consulta);

        if (result != 0) {
            pw.println("Hemos aceptado tu articulo.");
        } else {
            pw.println("Ha habido un error al insertar el artíCULO.");
        }
        pw.println("</BODY></HTML>");

    }

    public void leerArticulo(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException {
        
        PrintWriter pw = res.getWriter();

        Statement stmt = con.createStatement();
        ResultSet rs;
        String consulta;
        
        consulta = "SELECT articulos.cuerpo,articulos.titulo, articulos.CodUsuario,usuarios.nombre,usuarios.empresa FROM articulos JOIN usuarios ON usuarios.CodUsuario = articulos.CodUsuario WHERE usuarios.nombre = '" + req.getParameter("user") + "'";
        
        rs = stmt.executeQuery(consulta);
        
        
        pw.println("<HTML>");
        pw.println("<HEAD></HEAD>");
        pw.println("<BODY>");

        while (rs.next()) {
            pw.println("<H2>");
            pw.println(rs.getString(1));
            pw.println("</H2><p>");
            pw.println("<I>Enviado desde: " + rs.getString(5) + "</I><BR> ");
            pw.println("<B>" + rs.getString(2) + "</B>, por " + rs.getString(4));
            pw.println("<br>");
        }
        pw.println("</BODY></HTML>");
    }

}
