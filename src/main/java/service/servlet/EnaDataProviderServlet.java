/**
 * Copyright 2012-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Andrew Tikhonov <andrew.tikhonov@gmail.com>
 **/
package service.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.data.DataRecord;
import service.datasource.EnaDataSource;
import service.util.MailClient;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.zip.GZIPInputStream;


/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 28/11/2013
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */

public class EnaDataProviderServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    final private Logger log = LoggerFactory.getLogger(getClass());

    protected String sourceName = "undef";
    protected String resourceName = "undef";

    public EnaDataProviderServlet() {
   		super();
   	}

    public static class ServletParameters {
        public String Id;
        public String Mode;
    }


    protected void doAny(final HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            ServletParameters params   = getParameters(request);

            Object o = getDataSource(sourceName);

            if (o instanceof EnaDataSource) {
                ArrayList<DataRecord> list = null;

                if (params.Mode.equalsIgnoreCase(ServletParameterType.RUNID)) {
                    list = ((EnaDataSource)o).locateRecordsByRunID(params.Id);
                }

                if (params.Mode.equalsIgnoreCase(ServletParameterType.EXPID)) {
                    list = ((EnaDataSource)o).locateRecordsByExpID(params.Id);
                }

                response.addHeader("Access-Control-Allow-Origin", "*");

                response.setContentType("text/html; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");

                PrintWriter out = response.getWriter();

                out.println(DataRecord.printHeader());

                if (list != null && list.size() > 0) {
                    for(DataRecord r : list) {
                        String record_string = r.printRecord();
                        out.println(record_string);
                    }
                }

                out.flush();

            } else {
                throw new Exception("Data Source " + sourceName +
                        ", not expected type " + o.getClass().toString());
            }

        } catch(Exception ex) {

            response.addHeader("Access-Control-Allow-Origin", "*");

            ServletOutputStream out = response.getOutputStream();
            out.print("error\t" + ex.toString());
            out.flush();
        }
    }

    public static void sendLoadingReport(String useremail, String subj, String date,
                                         String operation, String resource, String status, String reason){
        try {
            String bgc = "#e0e0e0";// normal

            if (ServletDataType.STATUS_FAILURE.equals(status)) {
                bgc = "#e01010";
            }


            MailClient client = new MailClient();

            operation     = operation.replaceAll("\n", "<br>");
            String text   =
                "<table border=\"0\" width=\"50%\">\n" +
                "        <tr bgcolor="+bgc+"><td>Date: </td><td><p align=left>"+date+"</p></td></tr>\n" +
                "        <tr bgcolor="+bgc+"><td>Operation: </td><td><p align=left>"+operation+"</p></td></tr>\n" +
                "        <tr bgcolor="+bgc+"><td>Resource: </td><td><p align=left>"+resource+"</p></td></tr>\n" +
                "        <tr bgcolor="+bgc+"><td>Status: </td><td><p align=left>"+status+"</p></td></tr>\n" +
                "        <tr bgcolor="+bgc+"><td>Reason: </td><td><p align=left>"+reason+"</p></td></tr>\n" +
                "</table>\n";

            // get arrays
            String[] to = { "andrew@ebi.ac.uk" };
            String[] a = {};

            client.sendMail( useremail, to, subj, text, a );

        } catch ( Exception e ) {
            //e.printStackTrace();
        }
    }

    public EnaDataSource createDataSource(String resourceUrl) throws Exception {
        log.info("creating data source from " + resourceUrl);

        EnaDataSource source = null;
        String status = "";
        String reason = "";

        try {
            URLConnection connection = new URL(resourceUrl).openConnection();
            int size = connection.getContentLength();
            GZIPInputStream is = new GZIPInputStream(connection.getInputStream());
            source = new EnaDataSource(is, size);

            status = ServletDataType.STATUS_SUCCESS;
        } catch (Exception ex) {
            status = ServletDataType.STATUS_FAILURE;
            reason = ex.toString();
        } finally {

            System.gc();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());

        sendLoadingReport("rcloud@ebi.ac.uk",
                "ENA Data Provider Report " + date, date,
                "ENA Data Loading", resourceName, status, reason);

        return source;
    }

    public void init(ServletConfig sConfig) throws ServletException {
   		super.init(sConfig);

        try {
            log.info(sConfig.getServletName() + " Init");

            sourceName = sConfig.getInitParameter("track.name");

            if (sourceName == null) {
                throw new Exception("track.name parameter is not defined use <init-param> in web.xml");
            }

            log.info("track name " + sourceName);

            log.info("loading properties ..");

            initProperties();

            resourceName = getServletProperties().getProperty(sourceName);

            log.info("loading data file from " + resourceName);

            Object o = getDataSource(sourceName);

            if (o != null) {
                log.info("source " + sourceName + " already initialized");
            } else {
                log.info("initializing data source " + sourceName);

                EnaDataSource datareader = createDataSource(resourceName);
                saveDataSource(sourceName, datareader);
            }

            log.info(sConfig.getServletName() + " Init Completed");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while( true ) {
                        try {
                            Thread.sleep(1000 * 60 * 60 * 24); // a day
                        } catch (Exception ex) {
                        }

                        try {
                            EnaDataSource datareader = createDataSource(resourceName);

                            // save only if new instance is created
                            if (datareader != null) {
                                saveDataSource(sourceName, datareader);
                            }
                            System.gc();
                        } catch (Exception ex) {
                            log.error("Error!", ex);
                        }
                    }
                }
            }).start();

        } catch (Exception ex) {
            log.error("Error!", ex);
        }
   	}

   	protected void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {
        try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected Exception ", ex);
        }
   	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {
        try {
            doAny(request, response);
        } catch (Exception ex) {
            log.error("Unexpected Exception ", ex);
        }
    }

    protected String getParameterHelp() {
        return "should be ?" + ServletParameterType.MODE + "=<" +
                ServletParameterType.RUNID + "|" +
                ServletParameterType.EXPID + ">&" +
                ServletParameterType.ID + "=ERR005871";
    }

    public ServletParameters getParameters(HttpServletRequest request) throws Exception {

        ServletParameters params = new ServletParameters();

        params.Id = request.getParameter(ServletParameterType.ID);
        params.Mode = request.getParameter(ServletParameterType.MODE);

        if (params.Id == null) {
            throw new Exception("No 'id' parameter found, " + getParameterHelp());
        }

        if (params.Mode == null) {
            throw new Exception("No 'm' parameter found, " + getParameterHelp());
        }

        if (!params.Mode.equalsIgnoreCase(ServletParameterType.RUNID) &&
                !params.Mode.equalsIgnoreCase(ServletParameterType.EXPID)) {

            throw new Exception("Illegal value in 'm': " + params.Mode + ", " + getParameterHelp());
        }

        log.info("locating " + params.Id + " in " + this.sourceName + " mode: " + params.Mode);

        return params;
    }

    public Properties getServletProperties() {
        return (Properties) getServletContext().getAttribute(ServletDataType.PROPERTIES);
    }

    public void initProperties() throws ServletException {
        Properties prop = new Properties();
       	try {
            //load a properties file
       		prop.load(EnaDataProviderServlet.class.getResourceAsStream("/config.properties"));
            getServletContext().setAttribute(ServletDataType.PROPERTIES, prop);

       	} catch (IOException ex) {
           ex.printStackTrace();
        }
    }

    public HashMap<String, Object> getSourceMap() throws Exception {
        Object o = getServletContext().getAttribute(ServletDataType.SOURCEMAP);

        if (o == null) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            getServletContext().setAttribute(ServletDataType.SOURCEMAP, map);

            o = map;
        }

        return (HashMap<String, Object>)o;
    }

    public Object getDataSource(String sourceName) throws Exception {
        return getSourceMap().get(sourceName);
    }

    public void saveDataSource(String sourceName, Object reader) throws Exception {
        getSourceMap().put(sourceName, reader);
    }

}
