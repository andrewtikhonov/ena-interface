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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import service.data.DataRecord;
import service.util.ProgressTracker;
import service.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.data.DataRecord;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 27/11/2013
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class GenericDataSource implements DataSourceInterface {

    private final static Logger log = LoggerFactory.getLogger(GenericDataSource.class);

    protected ArrayList<DataRecord> data0;
    protected int loaded_total = 0;
    protected int missing_data = 0;

    protected HashMap<String, ArrayList<Integer>> run2FeatureIndexMap = new HashMap<String, ArrayList<Integer>>();
    protected HashMap<String, ArrayList<Integer>> exp2FeatureIndexMap = new HashMap<String, ArrayList<Integer>>();


    //
    // P R O C E S S I N G
    //
    public ArrayList<DataRecord> initDataSource(InputStream inputstream, int available) throws Exception {

        //Scanner scanner = new Scanner(inputstream);
        ArrayList<DataRecord> newdata = new ArrayList<DataRecord>();

        log.info("size = " + Util.humanizeSize(available));
        ProgressTracker tracker = new ProgressTracker(available);

        String line0 = "";
        String line1 = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

   			//first use a Scanner to get each line
            //while ( scanner.hasNextLine() ) {
            while ((line1 = reader.readLine()) != null) {

                line0 = line0 + line1;

                //line = line + scanner.nextLine();

                if (line0.length() > 0) {
                    try {
                        processLine(line0, newdata);

                        // track progress
                        if (tracker.track(line0.length())) {
                            log.info(tracker.report());
                        }

                    } catch (NotEnoughDataException ex) {
                        // line is not cleared
                        // until full row is read
                    } finally {
                        // clear the line
                        line0 = "";
                    }
                }
   			}
   		} finally {
   			//ensure the underlying stream is always closed
   			//scanner.close();
            inputstream.close();

            System.gc();
   		}

        return newdata;
   	}

    abstract protected void processLine(String aLine, ArrayList<DataRecord> data) throws Exception;

    //
    // G E T T E R S
    //

    public ArrayList<DataRecord> getData() {
        return data0;
    }

    public ArrayList<DataRecord> locateRecordsByRunID(String run_id) {
        ArrayList<DataRecord> result = new ArrayList<DataRecord>();

        ArrayList<Integer> indexList = run2FeatureIndexMap.get(run_id);
        if (indexList != null) {
            for (Integer index : indexList) {
                result.add(data0.get(index));
            }
        }
        return result;
    }

    public ArrayList<DataRecord> locateRecordsByExpID(String exp_id) {
        ArrayList<DataRecord> result = new ArrayList<DataRecord>();

        ArrayList<Integer> indexList = exp2FeatureIndexMap.get(exp_id);
        if (indexList != null) {
            for (Integer index : indexList) {
                result.add(data0.get(index));
            }
        }
        return result;
    }

}
