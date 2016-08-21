/*
 * Copyright (c) 2016 Jan Böhmer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jbtronics.recolldroid.api;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.InputMismatchException;

/**
 * This class represents a filesize. It features functions to convert the bytes value (automatically) into another unit.
 */
public class FileSize {
    private Integer bytes_size;

    public static final int FACTOR_B = 1;
    public static final int FACTOR_KB = 1000;
    public static final int FACTOR_MB = 1000 * 1000;
    public static final int FACTOR_GB = 1000 * 1000 * 1000;
    public static final long FACTOR_TB = 1000 * 1000 * 1000 * 1000;

    /**
     * Creates a new file size with the given bytes value.
     * @param bytes The size in byte
     */
    public FileSize(Integer bytes)
    {
        if(bytes < 0)
        {
            throw new InputMismatchException("Size must be >0!");
        }
        else
        {
            bytes_size = bytes;
        }
    }

    public static FileSize ParseString(String bytes_str)
    {
        if(bytes_str==null||bytes_str=="")
        {
            return null;
        }
        bytes_str = bytes_str.trim();
        return new FileSize( Integer.parseInt(bytes_str));
    }

    /**
     * Gets the filesize.
     * @return Filesize in Bytes
     */
    public Integer getBytes_size()
    {
        return bytes_size;
    }

    /**
     * Converts the Size into the given target unit.
     * @param targetunit The target unit as String ("B","kB","MB","GB","TB")
     * @return The filesize in the new unit.
     */
    public Double convertUnit(String targetunit)
    {
        Double d=bytes_size.doubleValue();
        if(targetunit.contains("k"))    //kB
        {
            return d / FACTOR_KB;
        }
        else if(targetunit.contains("M"))  //MB
        {
            return d / FACTOR_MB;
        }
        else if(targetunit.contains("G"))   //GB
        {
            return d / FACTOR_GB;
        }
        else if(targetunit.contains("T"))   //TB
        {
            return d / FACTOR_TB;
        }
        else
        {
            return d;
        }
    }

    /**
     * Returns a string with the filesize automatically converted into a proper unit.
     * @param precision Number of digits after the point.
     * @return The filesize as String in the correct unit.
     */
    public String toString(Integer precision)
    {
        Double tmp = 0.0;
        String unit = "B";
        if(bytes_size>=0&&bytes_size<FACTOR_KB)
        {
            tmp = convertUnit("B");
            unit = "B";
        }
        else if(bytes_size>=FACTOR_KB&&bytes_size<FACTOR_MB)
        {
            tmp = convertUnit("kB");
            unit = "kB";
        }
        else if(bytes_size>=FACTOR_MB&&bytes_size<FACTOR_GB)
        {
            tmp = convertUnit("MB");
            unit = "MB";
        }
        else if(bytes_size>=FACTOR_GB&&bytes_size<FACTOR_TB)
        {
            tmp = convertUnit("GB");
            unit = "GB";
        }
        else if(bytes_size>=FACTOR_TB)
        {
            tmp = convertUnit("TB");
            unit = "TB";
        }

        String format = "0.";
        for(int i=0;i<precision;i++)
        {
            format = format + '#';
        }
        DecimalFormat df = new DecimalFormat(format);
        df.setRoundingMode(RoundingMode.CEILING);
        String s = df.format(tmp);
        s = s + " " + unit;
        return s;
    }

    /**
     * Returns a string with the filesize automatically converted into a proper unit. Rounded to two decimal places!
     * @return The filesize as String in the correct unit.
     */
    public String toString()
    {
        return toString(2);
    }

}
