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
package service.util;

/**
 * Created with IntelliJ IDEA.
 * User: andrew
 * Date: 05/12/2012
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
public class ProgressTracker {

    public int available = 0;
    public int loaded0 = 0;
    public int loaded1 = 0;

    public ProgressTracker(int available) {
        this.available = available;
        this.loaded1   = available / 10;
    }

    public boolean track(int loaded0) {
        this.loaded0 += loaded0;
        return (this.loaded0 > loaded1);
    }

    public String report() {
        loaded1 += available / 10;
        return ((long)loaded0 * 100 / available) + "%";
    }
}
