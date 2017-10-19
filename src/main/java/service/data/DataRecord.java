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
package service.data;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 27/11/2013
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class DataRecord {

    private String st_a;
    private String sa_a;
    private String e_a;
    private String r_a;
    private String t_i;
    private String s_n;
    private String i_p;
    private String i_m;
    private String l_n;
    private String l_l;
    private String l_st;
    private String l_so;
    private String l_se;
    private String i;
    private String d;
    private String f_p;
    private String f_f;

    public DataRecord(String study_accession, String sample_accession, String experiment_accession,
                      String run_accession, String tax_id, String scientific_name, String instrument_platform,
                      String instrument_model, String library_name, String library_layout, String library_strategy,
                      String library_source, String library_selection, String insert, String deviation,
                      String first_public, String fastq_ftp) {
        this.st_a = study_accession;
        this.sa_a = sample_accession;
        this.e_a = experiment_accession;
        this.r_a = run_accession;
        this.t_i = tax_id;
        this.s_n = scientific_name;
        this.i_p = instrument_platform;
        this.i_m = instrument_model;
        this.l_n = library_name;
        this.l_l = library_layout;
        this.l_st = library_strategy;
        this.l_so = library_source;
        this.l_se = library_selection;
        this.i = insert;
        this.d = deviation;
        this.f_p = first_public;
        this.f_f = fastq_ftp;
    }

    public String getStudy_accession() {
        return st_a;
    }

    public String getSample_accession() {
        return sa_a;
    }

    public String getExperiment_accession() {
        return e_a;
    }

    public String getRun_accession() {
        return r_a;
    }

    public String getTax_id() {
        return t_i;
    }

    public String getScientific_name() {
        return s_n;
    }

    public String getInstrument_platform() {
        return i_p;
    }

    public String getInstrument_model() {
        return i_m;
    }

    public String getLibrary_name() {
        return l_n;
    }

    public String getLibrary_layout() {
        return l_l;
    }

    public String getLibrary_strategy() {
        return l_st;
    }

    public String getLibrary_source() {
        return l_so;
    }

    public String getLibrary_selection() {
        return l_se;
    }

    public String getInsert() {
        return i;
    }

    public String getDeviation() {
        return d;
    }

    public String getFirst_public() {
        return f_p;
    }

    public String getFastq_ftp() {
        return f_f;
    }

    private static String SEP = "\t";

    public String printRecord() {
        return
        this.st_a + SEP +
        this.sa_a + SEP +
        this.e_a + SEP +
        this.r_a + SEP +
        this.t_i + SEP +
        this.s_n + SEP +
        this.i_p + SEP +
        this.i_m + SEP +
        this.l_n + SEP +
        this.l_l + SEP +
        this.l_st + SEP +
        this.l_so + SEP +
        this.l_se + SEP +
        this.i + SEP +
        this.d + SEP +
        this.f_p + SEP +
        this.f_f;
    }

    public static String printHeader() {
        return
        "study_accession" + SEP +
        "sample_accession" + SEP +
        "experiment_accession" + SEP +
        "run_accession" + SEP +
        "tax_id" + SEP +
        "scientific_name" + SEP +
        "instrument_platform" + SEP +
        "instrument_model" + SEP +
        "library_name" + SEP +
        "library_layout" + SEP +
        "library_strategy" + SEP +
        "library_source" + SEP +
        "library_selection" + SEP +
        "insert" + SEP +
        "deviation" + SEP +
        "first_public" + SEP +
        "fastq_ftp";
    }

}

