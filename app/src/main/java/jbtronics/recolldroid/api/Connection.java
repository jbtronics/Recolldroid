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

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by janhb on 19.08.2016.
 */
public class Connection {

    private String baseurl;
    private Context context;
    private RequestQueue queue;
    private onQueryError mOnError;
    private onQueryComplete mOnComplete;
    private Query _query;
    private boolean _finished;

    private String auth_user="";
    private String auth_pass="";

    public interface onQueryComplete{ void querycompleted(Query q); }
    public interface onQueryError{ void queryerror(VolleyError error, Exception e);}

    public Connection(String url, Context context)
    {
        queue = Volley.newRequestQueue(context);
        this.context = context;
        baseurl = url;
        if(baseurl.charAt(baseurl.length()-1)!='/')
        {
            baseurl = baseurl + '/';
        }
        auth_pass = "";
        auth_user = "";
    }

    public Connection(String url, Context context, String user, String pass)
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
    }

    public void query(String term)
    {
        String query = "?query=";
        try
        {
            query  = query + URLEncoder.encode(term.trim(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("Connection","Encoding not supported!",e);
            if(mOnError != null) {
                mOnError.queryerror(null, e);
            }
            return;
        }

        String url = baseurl + "json" + query;
        downloadJson(url);

    }

    private void downloadJson(String url)
    {
        /*
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseQuery(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Connection","Error Downloading Json:" + error.getMessage());
                if(mOnError != null) {
                    mOnError.queryerror(error, null);
                }
            }
        });
        */

        QueryRequest queryRequest = new QueryRequest(url, auth_user,auth_pass, new Response.Listener<Query>() {
            @Override
            public void onResponse(Query response) {
                _query = response;
                _finished = true;
                if(mOnComplete!=null) {
                    mOnComplete.querycompleted(_query);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Connection","Error Downloading Json:" + error.getMessage());
                if(mOnError != null) {
                    mOnError.queryerror(error, null);
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(queryRequest);
    }

    public void setOnCompleteListener(onQueryComplete q)
    {
      mOnComplete = q;
    }

    public void setOnErrorListener(onQueryError q)
    {
        mOnError = q;
    }


    private void parseQuery(String json)
    {
        try {
            if (json != null) {
                JSONObject root = new JSONObject(json);
                JSONObject query = root.getJSONObject("query");
                JSONArray results = root.getJSONArray("results");
                _query = new Query(query,results);
                _finished = true;
                if(mOnComplete!=null) {
                    mOnComplete.querycompleted(_query);
                }
            }
        }
        catch (JSONException ex)
        {
            Log.w("Connection","Json Parse Exception: ",ex);
            if(mOnError != null) {
                mOnError.queryerror(null, ex);
            }
        }
    }


}
