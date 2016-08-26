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

import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by janhb on 18.08.2016.
 */
public class Query {

    public interface onQueryComplete{ void querycompleted(Query sender, int page); }
    public interface onQueryError{ void queryerror(VolleyError error, Exception e);}


    private Integer page;
    private Integer lastpage;
    private ArrayList<Result> results;
    private Context context;
    private RequestQueue queue;
    private String baseurl;
    private String auth_pass;
    private String auth_user;
    private String term;
    private QueryOptions options;
    private onQueryError mOnError;
    private onQueryComplete mOnComplete;
    private Boolean complete=false;
    private Boolean legacy_json=false;
    private Integer pagesize=50;


    public Query(String url, Context context, String term, String user, String pass)
    {
        queue = Volley.newRequestQueue(context);
        this.context = context;
        baseurl = url;
        if(baseurl.charAt(baseurl.length()-1)!='/')
        {
            baseurl = baseurl + '/';
        }
        auth_pass = pass;
        auth_user = user;
        this.term = term;
        options = new QueryOptions();
        page = 1;
        lastpage = 0;
        results = new ArrayList<>();
    }

    public Query(String url, Context context,String term, String user, String pass, QueryOptions options) {
        queue = Volley.newRequestQueue(context);
        this.context = context;
        baseurl = url;
        if (baseurl.charAt(baseurl.length() - 1) != '/') {
            baseurl = baseurl + '/';
        }
        auth_pass = pass;
        auth_user = user;
        this.term = term;
        this.options = options;
        page = 1;
        lastpage = 0;
        results = new ArrayList<>();
    }

    public Query(String url, Context context, String term, QueryOptions options) {
        queue = Volley.newRequestQueue(context);
        this.context = context;
        baseurl = url;
        if (baseurl.charAt(baseurl.length() - 1) != '/') {
            baseurl = baseurl + '/';
        }
        auth_pass = "";
        auth_user = "";
        this.term = term;
        this.options = options;
        page = 1;
        lastpage = 0;
        results = new ArrayList<>();
    }

    public void setPageSize(Integer size)
    {
        if(size>0) {
            pagesize = size;
        }
        else if(size==0){
            legacy_json = true;
        }
    }

    public void useLegacyJson(Boolean value)
    {
        legacy_json = value;
    }

    public void makeQuery()
    {
        String url = baseurl + buildQueryParams();
        final ResultsRequest resultsRequest = new ResultsRequest(url, auth_user, auth_pass, new Response.Listener<ArrayList<Result>>() {
            @Override
            public void onResponse(ArrayList<Result> response) {
                if(page > lastpage)
                {
                    lastpage = page;
                    for(Result k : response)
                    {
                        results.add(k);
                    }
                    complete = true;
                    mOnComplete.querycompleted(Query.this, page);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Connection", "Error Downloading Json:" + error.getMessage());
                if (mOnError != null) {
                    mOnError.queryerror(error, null);
                }
            }
        });

        // Add the request to the RequestQueue.
        Log.d("Connection", "Download begin!");
        queue.add(resultsRequest);
    }

    private String buildQueryParams()
    {
        String params;
        if(legacy_json) {
            params = "json";
        }
        else
        {
            params = "pagedjson";
        }
        try {
            params += "?query=" + URLEncoder.encode(term.trim(), "UTF-8");
            if(!legacy_json) {
                params += "&page=" + page.toString();
                params += "&items=" + pagesize.toString();
            }
            params += "&sort=" + options.getSort().toString();
            params += "&ascending" + options.getAscendingString();
            params += "&dir" + URLEncoder.encode(options.getDir().trim(), "UTF-8");
            params += "&before" +  options.getBeforeString();
            params += "&after" + options.getAfterString();
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("Connection","Encoding not supported!",e);
            if(mOnError != null) {
                mOnError.queryerror(null, e);
            }
            return "";
        }
        return params;
    }

    public void loadNextPage()
    {
        page++;
        makeQuery();
    }

    public void setOnCompleteHandler(onQueryComplete handler)
    {
        mOnComplete = handler;
    }

    public void setOnErrorHandler(onQueryError handler)
    {
        mOnError = handler;
    }

    public String getDownloadURL(Integer position)
    {
        String params = "download/";
        params += position.toString();
        try {
            params += "?query=" + URLEncoder.encode(term.trim(), "UTF-8");
            params += "&sort=" + options.getSort().toString();
            params += "&ascending" + options.getAscendingString();
            params += "&dir" + URLEncoder.encode(options.getDir().trim(), "UTF-8");
            params += "&before" +  options.getBeforeString();
            params += "&after" + options.getAfterString();
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("Connection","Encoding not supported!",e);
            if(mOnError != null) {
                mOnError.queryerror(null, e);
            }
            return "";
        }
        return baseurl + params;
    }

    public String getDownloadURL(Result r)
    {
        Integer i = results.indexOf(r);
        return getDownloadURL(i);
    }

    public String getPreviewURL(Integer position)
    {
        String params = "preview/";
        params += position.toString();
        try {
            params += "?query=" + URLEncoder.encode(term.trim(), "UTF-8");
            params += "&sort=" + options.getSort().toString();
            params += "&ascending" + options.getAscendingString();
            params += "&dir" + URLEncoder.encode(options.getDir().trim(), "UTF-8");
            params += "&before" +  options.getBeforeString();
            params += "&after" + options.getAfterString();
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("Connection","Encoding not supported!",e);
            if(mOnError != null) {
                mOnError.queryerror(null, e);
            }
            return "";
        }
        return baseurl + params;
    }

    public String getPreviewURL(Result r)
    {
        Integer i = results.indexOf(r);
        return getPreviewURL(i);
    }


    public ArrayList<Result> getResults() {
        return results;
    }

    public Result getResult(int index)
    {
        return results.get(index);
    }


    public void addResult(Result result)
    {
        results.add(result);
    }

    public String getTerm()
    {
        return term;
    }

    public QueryOptions getOptions()
    {
        return options;
    }


    public Integer getPage()
    {
        return page;
    }


}
