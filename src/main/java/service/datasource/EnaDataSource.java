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
package service.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import service.data.DataRecord;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 27/11/2013
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public class EnaDataSource extends GenericDataSource {

    private final static Logger log = LoggerFactory.getLogger(EnaDataSource.class);

    public String getStats() {
        StringBuilder b = new StringBuilder();

        b.append("features_total=");
        b.append(loaded_total);

        b.append(" missing_data=");
        b.append(missing_data);

        b.append(" ");

        return b.toString();
    }

    public EnaDataSource(InputStream stream, int size) throws Exception {
        data0 = initDataSource(stream, size);

        initIndexMaps();
	}

    protected void initIndexMaps() {
        log.info("creating index maps");

        for (int i = 0;i < data0.size();i++) {
            DataRecord f = data0.get(i);


            // run index
            String run_key = f.getRun_accession();
            ArrayList<Integer> run_list0 = run2FeatureIndexMap.get(run_key);

            if (run_list0 == null) {
                run_list0 = new ArrayList<Integer>();
                run2FeatureIndexMap.put(run_key, run_list0);
            }

            run_list0.add(i);

            // exp index
            String exp_key = f.getExperiment_accession();
            ArrayList<Integer> exp_list0 = exp2FeatureIndexMap.get(exp_key);

            if (exp_list0 == null) {
                exp_list0 = new ArrayList<Integer>();
                exp2FeatureIndexMap.put(exp_key, exp_list0);
            }

            exp_list0.add(i);
        }
    }

    protected void processLine(String aLine, ArrayList<DataRecord> data) throws Exception {

        if (aLine.endsWith("\t")) {
            // last value is missing, add "null"
            aLine += "null";
            missing_data++;
        }

        String[] parts = aLine.split("\t");

   		if (parts.length < 17) {
            // split line, return and get more data
            throw new NotEnoughDataException();
        }

        if (parts.length > 17) {
            log.error("Error: wrong number of fields ["+aLine+"]");
            return;
        }

        if ("study_accession".equals(parts[0])) {
            // first line, skip it
            return;
        }

        loaded_total++;

        DataRecord newRecord = new DataRecord(
                parts[0], // study_accession,
                parts[1], // sample_accession,
                parts[2], // experiment_accession,
                parts[3], //run_accession,
                parts[4], // tax_id,
                parts[5], // scientific_name,
                parts[6], // instrument_platform,
                parts[7], // instrument_model,
                parts[8], // library_name,
                parts[9], // library_layout,
                parts[10], // library_strategy,
                parts[11], // library_source,
                parts[12], // library_selection,
                parts[13], // insert,
                parts[14], // deviation,
                parts[15], // first_public,
                parts[16]); // fastq_ftp

        try {
            data.add(newRecord);
        } catch (Exception ex) {
            log.error("Error!", ex);
        }
   	}

    //
    // M A I N
    //

	public static void main(String[] a){

        try {
            log.info("loading..");

            InputStream is = new FileInputStream("./aehts-orig");

            EnaDataSource source = new EnaDataSource(is, is.available());

            log.info("loaded");
            log.info(source.getStats());

            ArrayList<DataRecord> result = null;

            long startTime = System.currentTimeMillis();

            int n = 1;
            for(int i=0;i<n;i++) {
                //result = source.locateRecordsByRunID("ERR097295");
                result = source.locateRecordsByRunID("SRR058089");
            }

            long estimatedTime = System.currentTimeMillis() - startTime;

            log.info("Estimated run time:" + estimatedTime + " Millis");

            log.info("result.size() = " + result.size());

            for (DataRecord f : result) {
                log.info("1 run: " + f.getRun_accession() + " exp: " + f.getExperiment_accession());
            }

		} catch (FileNotFoundException e) {
			//  Auto-generated catch block
			log.error("Error!", e);
		} catch (Exception e) {
			//  Auto-generated catch block
            log.error("Error!", e);
		}
	}

}
