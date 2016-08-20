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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

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


    public Result(JSONObject result) throws JSONException
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
        if(s.equals("") || s==null)
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
        //TODO: Implement Handling if mtime is empty!
        return mtime;
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

    public String getFolder()
    {
        return "TODO";
        //return ipath;
    }

    public String getFormattedSnippet()
    {
        snippet = snippet.replace("<span class=\"search-result-highlight\">","<b>");
        snippet = snippet.replace("</span>","</b>");
        return snippet;
    }




}
