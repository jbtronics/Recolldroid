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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

import jbtronic.recolldroid.R;

/**
 * Created by janhb on 18.08.2016.
 */
public class Result {

    private String sha;
    private String snippet;
    private String title;
    private URL url;
    private String abstract_str;
    private String author;
    private FileSize fbytes;
    private String filename;
    private String mtype;
    private String ipath; //value private to the app.: internal access path inside file
    private Date fmtime; //File Modification Date
    private FileSize dbytes; //Documents Bytes
    private String sig; //app-defined file modification signature. For up to date checks
    private Date mtime; //dmtime if set else fmtime
    private Date dmtime; //document creation/modification date
    private String label;
    private Integer relevancy;
    private String origcharset;
    private String keywords;
    private FileSize size; //dbytes if set, else fbytes


    public Result(JSONObject result)
    {
        try {
            sha = result.getString("sha");
            snippet = result.getString("snippet");
            title = result.getString("title");
            url = new URL(result.getString("url"));
            abstract_str = result.getString("abstract");
            author = result.getString("author");
            fbytes = FileSize.ParseString(result.getString("fbytes"));
            filename = result.getString("filename");
            mtype = result.getString("mtype");
            ipath = result.getString("ipath");
            fmtime = parseTime(result.getString("fmtime")); //Date expect timestamp in ms
            dbytes = FileSize.ParseString(result.getString("dbytes"));
            sig = result.getString("dbytes");
            keywords = result.getString("keywords");
            mtime = parseTime(result.getString("mtime")); //Date expect timestamp in ms
            dmtime = parseTime(result.getString("dmtime")); //Date expect timestamp in ms
            label = result.getString("label");
            origcharset = result.getString("origcharset");
            size = FileSize.ParseString(result.getString("size"));

            String relevancy_str = result.getString("relevancyrating");
            relevancy_str = relevancy_str.replace("%","").trim();
            relevancy = Integer.parseInt(relevancy_str);
        }
        catch(JSONException ex)
        {
            Log.w("Result","Could not parse JSON", ex);
        } catch (MalformedURLException e) {
            Log.w("Result","Malformed URL", e);
        }
    }

    private static Date parseTime(String s)
    {
        if(s==null || s.equals(""))
        {
            return null;
        }
        else
        {
            return new Date(Long.parseLong(s) * 1000);
        }
    }

    public String getSha() {
        return sha;
    }

    public String getSnippet(){
        return snippet;
    }

    public String getTitle() {
        return title;
    }

    public URL getURL() {
        return url;
    }

    public String getAbstract() {
        return abstract_str;
    }

    public String getAuthor() {
        return author;
    }

    public FileSize getSize() {
        //TODO: Implement Handling if size is empty!
        return size;
    }

    public String getFilename(){
        return filename;
    }

    public String getMType()
    {
        return mtype;
    }

    public String getIpath()
    {
        return ipath;
    }

    public Date getMtime()
    {
        if(mtime!=null) {
            return mtime;
        }
        else if(dmtime!=null)
        {
            return dmtime;
        }
        else if(fmtime!=null)
        {
            return fmtime;
        }
        else{
            return null;
        }
    }

    public String getMtimeStr()
    {
        DateFormat df;
        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM );
        return df.format(getMtime());
    }

    public String getLabel()
    {
        return label;
    }

    public Integer getRelevancy()
    {
        return relevancy;
    }

    public String getOrigcharset()
    {
        return origcharset;
    }

    public String getHeader()
    {
        return filename;
    }

    public String getFolder(String path)
    {
        path = path.replace("\\","/");
        if(path.charAt(path.length()-1) == '/')
        {
            path = path.substring(0,path.length() - 1);
        }
        String tmp = url.toString().replace(filename,"");
        tmp = tmp.replace(path,"");
        return tmp;
    }

    public String getFormattedSnippet()
    {
        snippet = snippet.replace("<span class=\"search-result-highlight\">","<b>");
        snippet = snippet.replace("</span>","</b>");
        return snippet;
    }

    /**
     * Returns an resource file with the correct icon for this filetype.
     * @return The icon for this filetype
     */
    public int getIconRes()
    {
        String ext = getExtension();
        if(ext.equals("pdf") || ext.equals("ps"))   //PDF Docs
        {
            return R.drawable.type_pdf;
        }
        else if(ext.equals("doc") || ext.equals("docx") || ext.equals("docm") || ext.equals("odt"))  //Word
        {
            return R.drawable.type_word;
        }
        else if(ext.equals("xls") || ext.equals("xlsx") || ext.equals("xlsm") || ext.equals("ods"))  //Excel
        {
            return R.drawable.type_excel;
        }
        else if(ext.equals("ppt") || ext.equals("pptx") || ext.equals("pptm") || ext.equals("odp"))  //Powerpoint
        {
            return R.drawable.type_powerpoint;
        }
        else if(ext.equals("txt") || ext.equals("rtf") || ext.equals("rtf") || ext.equals("md") //Text
                || ext.equals("tex") || ext.equals("csv") || ext.equals("conf") || ext.equals("chm"))
        {
            return R.drawable.type_text;
        }
        else if(ext.equals("mp3") || ext.equals("wav") || ext.equals("m4a") || ext.equals("ogg")    //Music
                || ext.equals("aac") || ext.equals("wma") || ext.equals("aiff") || ext.equals("amr")
                || ext.equals("flac") || ext.equals("m4b"))
        {
            return R.drawable.type_music;
        }
        else if(ext.equals("mp4") || ext.equals("wmv") || ext.equals("flv") || ext.equals("mkv")    //Videos
                || ext.equals("vob") || ext.equals("mov") || ext.equals("yuv") || ext.equals("m4v")
                || ext.equals("m4p") || ext.equals("asf") || ext.equals("mpg") || ext.equals("mpeg")
                || ext.equals("m2v") || ext.equals("3gb") || ext.equals("webm"))
        {
            return R.drawable.type_video;
        }
        else if(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("png")   //Images
                || ext.equals("raw") || ext.equals("dng") || ext.equals("bmp") || ext.equals("tiff")
                || ext.equals("xcf") || ext.equals("tga"))
        {
            return R.drawable.type_image;
        }
        else if(ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || ext.equals("tar")  //Archives
                || ext.equals("tgz") || ext.equals("gz") || ext.equals("bz") || ext.equals("bz2")
                || ext.equals("gz2") || ext.equals("lzma") || ext.equals("lz") || ext.equals("xz"))
        {
            return R.drawable.type_zip;
        }
        else if(ext.equals("htm") || ext.equals("html") || ext.equals("xml") || ext.equals("css") //Code
                || ext.equals("js") || ext.equals("c") || ext.equals("cpp") || ext.equals("h")
                || ext.equals("hpp") || ext.equals("java") || ext.equals("cs") || ext.equals("vb")
                || ext.equals("py") || ext.equals("sh") || ext.equals("pl") || ext.equals("rb")
                || ext.equals("class") || ext.equals("ino") || ext.equals("pde") || ext.equals("")
                || ext.equals("cmd") || ext.equals("bat") || ext.equals("mk") || ext.equals("mod")
                || ext.equals("xrb") || ext.equals("mm") || ext.equals("swift"))
        {
            return R.drawable.type_code;
        }
        else if(ext.equals("apk") || ext.equals("dex")) //Android files
        {
            return R.drawable.type_android;
        }
        else if(ext.equals("ipa") || ext.equals("plist") || ext.equals("ios"))   //Apple files
        {
            return R.drawable.type_apple;
        }
        else if(ext.equals("iso") || ext.equals("img") || ext.equals("cue") || ext.equals("vhd") //Image files
                || ext.equals("vdi") || ext.equals("vdi") )
        {
            return R.drawable.type_disk;
        }
        else if(ext.equals("db") || ext.equals("sqlite") || ext.equals("sql"))       //Databases
        {
            return R.drawable.type_db;
        }
        else if(ext.equals("exe") || ext.equals("sys") || ext.equals("dll")    //Windows binaries
            || ext.equals("cur") || ext.equals("ico") )
        {
            return R.drawable.type_windows;
        }
        else if(ext.equals("ko") || ext.equals("so") || ext.equals("a"))    //Linux binaries
        {
            return R.drawable.type_linux;
        }
        else if(ext.equals("accdb") || ext.equals("pub")) //other Office files
        {
            return R.drawable.type_office;
        }
        else if(ext.equals("blend") || ext.equals("3ds") || ext.equals("dxf") //Blender/3d Files
                || ext.equals("wrl") || ext.equals("x3d"))
        {
            return R.drawable.type_blender;
        }

        else if(ext.equals("stl") || ext.equals("amf") || ext.equals("gcode")  //3D Printer /CNC files
                || ext.equals("nc"))
        {
            return R.drawable.type_3d;
        }
        else if(ext.equals("onion"))    //An Egg for Easter ;)
        {
            return R.drawable.type_tor;
        }
        else if(ext.equals("dfw") || ext.equals("mathml") || ext.equals("nb") //Derive 6 files :)
                || ext.equals("mat"))
        {
            return R.drawable.type_math;
        }
        else
        {
            return R.drawable.type_file;
        }


    }

    public int getIconResWhite()
    {
        int res = getIconRes();
        switch (res)
        {
            case R.drawable.type_file:
                return R.drawable.type_file_white;
            case R.drawable.type_3d:
                return R.drawable.type_3d_white;
            case R.drawable.type_android:
                return R.drawable.type_android_white;
            case R.drawable.type_apple:
                return R.drawable.type_apple_white;
            case R.drawable.type_blender:
                return R.drawable.type_blender_white;
            case R.drawable.type_code:
                return R.drawable.type_code_white;
            case R.drawable.type_db:
                return R.drawable.type_db_white;
            case R.drawable.type_disk:
                return R.drawable.type_disk_white;
            case R.drawable.type_excel:
                return R.drawable.type_excel_white;
            case R.drawable.type_image:
                return R.drawable.type_image_white;
            case R.drawable.type_linux:
                return R.drawable.type_linux_white;
            case R.drawable.type_math:
                return R.drawable.type_math_white;
            case R.drawable.type_music:
                return R.drawable.type_music_white;
            case R.drawable.type_office:
                return R.drawable.type_office_white;
            case R.drawable.type_pdf:
                return R.drawable.type_pdf_white;
            case R.drawable.type_powerpoint:
                return R.drawable.type_powerpoint_white;
            case R.drawable.type_text:
                return R.drawable.type_text_white;
            case R.drawable.type_tor:
                return R.drawable.type_tor_white;
            case R.drawable.type_video:
                return R.drawable.type_video_white;
            case R.drawable.type_windows:
                return R.drawable.type_windows_white;
            case R.drawable.type_word:
                return R.drawable.type_word_white;
            case R.drawable.type_zip:
                return R.drawable.type_zip_white;

            //Use black item if no white is available
            default:
                return getIconRes();
        }

    }


    /**
     * Gets the extension of the result file. (without ".")
     * @return The fileextension
     */
    public String getExtension()
    {
        String ext;
        if(!ipath.equals(""))
        {
            int tmp = ipath.lastIndexOf(".");
            ext = ipath.substring(tmp+1);
        }
        else
        {
            if(!filename.contains("."))
            {
                return "";
            }
            int tmp = filename.lastIndexOf(".");
            ext = filename.substring(tmp+1);
        }
        return ext.toLowerCase();
    }






}
