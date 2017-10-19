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
package service.bfarm;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.util.MailClient;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 28/11/2013
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */

public class BFSubmission extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private boolean isMultipart;

    private String storagePath;
    private String newJobsPath;

    public BFSubmission() {
   		super();
   	}

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BFRequest req = new BFRequest(
                "tmpJob1", "tmpFile1", "0", "250", BFType.SUBMITTED);


        // Check that we have a file upload request
        isMultipart = ServletFileUpload.isMultipartContent(request);

        response.setContentType("text/html");

        PrintWriter out = response.getWriter( );

        if( !isMultipart ){
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>No file uploaded</p>");
            out.println("</body>");
            out.println("</html>");
            return;
        }

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet upload</title>");
        out.println("</head>");
        out.println("<body>");

        // Parse the request
        try {

            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    String value = Streams.asString(stream);

                    //out.println("<p>Form field " + name + " with value "
                    //        + value + " detected.<p>");

                    if ("jobname".equals(name)) {
                        req.jobname = value;
                    }

                    if ("start".equals(name)) {
                        req.start = value;
                    }

                    if ("stop".equals(name)) {
                        req.stop = value;
                    }

                } else {
                    req.filname = item.getName();

                    //out.println("<p>File field " + name + " with file name "
                    //        + fileName + " detected.<p>");

                    // Process the input stream
                    OutputStream outFile = null;

                    try {
                        outFile = new FileOutputStream(new File(storagePath + File.separator + req.filname));

                        int read = 0;
                        final byte[] bytes = new byte[4096];

                        while ((read = stream.read(bytes)) != -1) {
                            outFile.write(bytes, 0, read);
                        }

                        out.println("Uploaded: " + req.filname + "<br>");

                    } catch (FileNotFoundException fne) {
                        out.println("You either did not specify a file to upload or are "
                                + "trying to upload a file to a protected or nonexistent "
                                + "location.");
                        out.println("<br/> ERROR: " + fne.getMessage());

                    } finally {
                        if (outFile != null) {
                            outFile.close();
                        }
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }
            }

            while (getSubmissionRequest(req.jobname) != null) {
                req.jobname = req.jobname + "0";
            }

            out.println("Submitted request: " + req.jobname + "</br>");

            String url = request.getRequestURL().toString();
            String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
            String statusURL = baseURL + "/status?" + BFType.JOB + "=" + req.jobname;
            out.println("Status: <a href=" + statusURL + ">" + statusURL + "</a></br>");

            submitProcessingRequest(req);
            saveSubmissionRequest(req);
            //sendSubmissionReport(req, statusURL);

        } catch (Exception ex) {
            out.println("Exception:" + ex.getMessage() + "</br>");
        }

        out.println("</body>");
        out.println("</html>");
        out.flush();
        //out.close();
    }

    private void submitProcessingRequest(BFRequest request) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(newJobsPath + File.separator + request.jobname), "utf-8"));
            writer.write(request.jobname + "," + request.filname + "," + request.start + "," + request.stop+ "\n");
        } catch (IOException ex) {
            // report
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {}
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {

         throw new ServletException("GET method used with " +
                 getClass( ).getName( )+": POST method required.");
    }

    public static void sendSubmissionReport(BFRequest req, String statusURL){

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());

        String subj = "BFARM Submission Report " + date;

        try {
            MailClient client = new MailClient();

            String text   =
                "<table border=\"0\" width=\"50%\">\n" +
                "        <tr bgcolor=#e0e0e0><td>Date: </td><td><p align=left>"+date+"</p></td></tr>\n" +
                "        <tr bgcolor=#e0e0e0><td>Request: </td><td><p align=left>"+req.jobname+"</p></td></tr>\n" +
                "        <tr bgcolor=#e0e0e0><td>Uploaded: </td><td><p align=left>"+req.filname+"</p></td></tr>\n" +
                "        <tr bgcolor=#e0e0e0><td>Status: </td><td><p align=left><a href="+statusURL+">"+statusURL+"</a></p></td></tr>\n" +
                "</table>\n";

            // get arrays
            String[] to = { "andrew@ebi.ac.uk", "andrew.tikhonov@gmail.com" };
            String[] a = {};

            client.sendMail( "rcloud@ebi.ac.uk", to, subj, text, a );

        } catch ( Exception e ) {
            //e.printStackTrace();
        }
    }

    public void init(ServletConfig sConfig) throws ServletException {
   		super.init(sConfig);

        try {
            log.info(sConfig.getServletName() + " Init");

            // Get the file location where it would be stored.
            storagePath    = getServletContext().getInitParameter("storage");
            newJobsPath    = getServletContext().getInitParameter("new");

            log.info(sConfig.getServletName() + " Init Completed");

        } catch (Exception ex) {
            log.error("Error!", ex);
        }
   	}

    public HashMap<String, BFRequest> getSourceMap() {
        Object o = getServletContext().getAttribute(BFType.REQUESTMAP);

        if (o == null) {
            HashMap<String, BFRequest> map = new HashMap<String, BFRequest>();

            getServletContext().setAttribute(BFType.REQUESTMAP, map);

            o = map;
        }

        return (HashMap<String, BFRequest>)o;
    }

    public void saveSubmissionRequest(BFRequest request) {
        getSourceMap().put(request.jobname, request);
    }

    public BFRequest getSubmissionRequest(String jobname) {
        return getSourceMap().get(jobname);
    }

}
