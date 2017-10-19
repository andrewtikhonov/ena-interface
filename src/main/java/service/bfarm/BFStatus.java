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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by andrew on 24/10/2014.
 */
public class BFStatus extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

    final private Logger log = LoggerFactory.getLogger(getClass());

    private String newJobsPath;
    private String processingPath;
    private String donePath;
    private String outputPath;

    public BFStatus() {
   		super();
   	}

    private String checkStatus(String jobname) {
        File f1 = new File(newJobsPath + File.separator + jobname);
        File f2 = new File(processingPath + File.separator + jobname);
        File f3 = new File(donePath + File.separator + jobname);

        if (f3.exists()) {
            return BFType.DONE;
        }

        if (f2.exists()) {
            return BFType.INPROGRESS;
        }

        if (f1.exists()) {
            return BFType.SUBMITTED;
        }

        return "UNDEFINED";
    }

    private boolean checkResultAvailable(String jobname) {
        File f1 = new File(outputPath + File.separator + jobname + ".tar.gz");
        return f1.exists();
    }

    private String readStatusLog(String jobname) {

        try {
            File f0 = new File(processingPath + File.separator + jobname + ".status");
            if (f0.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(f0));
                StringBuilder b = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    b.append(line);
                    b.append("<br>");
                }

                return b.toString();
            } else {
                return "";
            }
        } catch (Exception ex) {
            return "";
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
               throws ServletException, IOException {

        String jobname = request.getParameter(BFType.JOB);
        String download = request.getParameter(BFType.DOWNLOAD);

        if (jobname == null || jobname.length() == 0 || jobname.contains("..")) {
            return;
        }

        if (download != null) {
            // download the result
            File f0 = new File(outputPath + File.separator + jobname + ".tar.gz");

            if (f0.exists()){
                response.setContentType("application/x-gzip");
                //response.setContentType("application/x-compressed");

                OutputStream out = response.getOutputStream();
                FileInputStream in = new FileInputStream(f0);

                byte[] buffer = new byte[4096];
                int length;
                while ((length = in.read(buffer)) > 0){
                    out.write(buffer, 0, length);
                }
                in.close();
                out.flush();
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter( );
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Error</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("File not found");

            }


        } else {
            // get status
            String status = checkStatus(jobname);
            String log = readStatusLog(jobname);

            response.setContentType("text/html");

            PrintWriter out = response.getWriter( );

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Status</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("Status: " + status + "<br>");

            if (log != null && log.length() > 0) {
                out.println("<br>");
                out.println(log);
                out.println("<br>");
            }

            if (checkResultAvailable(jobname)) {
                String url = "status?" + BFType.JOB + "=" + jobname + "&" + BFType.DOWNLOAD;
                out.println("Link: <a href=" + url + ">" + url + "</a></p>");
            } else {
                out.println("Link: not yet available</p>");
            }

            out.println("</body>");
            out.println("</html>");
            out.flush();
        }

        return;
    }

    public void init(ServletConfig sConfig) throws ServletException {
   		super.init(sConfig);

        try {
            log.info(sConfig.getServletName() + " Init");

            // Get the file location where it would be stored.
            newJobsPath    = getServletContext().getInitParameter("new");
            processingPath = getServletContext().getInitParameter("processing");
            donePath       = getServletContext().getInitParameter("done");
            outputPath     = getServletContext().getInitParameter("output");

            log.info(sConfig.getServletName() + " Init Completed");

        } catch (Exception ex) {
            log.error("Error!", ex);
        }
   	}
}

