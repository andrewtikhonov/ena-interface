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

/**
 * Created by andrew on 24/10/2014.
 */
public class BFRequest {
    public BFRequest(String jobname, String filname, String start, String stop, String status) {
        this.jobname = jobname;
        this.filname = filname;
        this.start = start;
        this.stop = stop;
        this.status  = status;
    }
    public String jobname;
    public String filname;
    public String start;
    public String stop;
    public String status;
}
