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
 * Date: 28/11/2012
 * Time: 11:46
 * To change this template use File | Settings | File Templates.
 */
public class Util {

    public static int oneK = 1024;
    public static int oneM = oneK * oneK;

    public static boolean isWithin(int pos, int start, int stop) {
        return (pos >= start && pos <= stop);
    }

    public static String humanizeSize(int size) {
        return (size > oneM ? (size / oneM + "M") : (size / oneK + "K"));
    }


}
