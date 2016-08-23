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

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by janhb on 21.08.2016.
 */
public class ResultsRequest extends Request<ArrayList<Result>> {

    private boolean need_auth = false;
    private final Listener<ArrayList<Result>> listener;
    private final Response.ErrorListener errorListener;
    private String user;
    private String pass;

    public ResultsRequest(String url, String user, String pass, Listener<ArrayList<Result>> listener, Response.ErrorListener errorListener)
    {
        super(Method.GET, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
        this.user = user;
        this.pass = pass;
        if(user.equals("") && pass.equals(""))
        {
            need_auth = false;
        }
        else
        {
            need_auth = true;
        }
    }

    public ResultsRequest(String url, Listener<ArrayList<Result>> listener, Response.ErrorListener errorListener)
    {
        super(Method.GET, url, errorListener);
        this.listener = listener;
        this.errorListener = errorListener;
        need_auth = false;
    }

    @Override
    protected Response<ArrayList<Result>> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            if (json != null) {
                JSONObject root = new JSONObject(json);
                JSONObject query = root.getJSONObject("query");
                JSONArray results = root.getJSONArray("results");
                ArrayList<Result> list = new ArrayList<Result>();

                for(int i=0; i < results.length();i++)
                {
                    Result tmp = new Result(results.getJSONObject(i));
                    list.add(tmp);
                }
                return Response.success(list, HttpHeaderParser.parseCacheHeaders(response));
            }
            else
            {
                return  Response.error(new ParseError(response));
            }

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e)
        {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(ArrayList<Result> response)
    {
        listener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(need_auth) {
            HashMap<String, String> params = new HashMap<String, String>();
            String creds = String.format("%s:%s", user, pass);
            String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
            params.put("Authorization", auth);
            return params;
        }
        else
        {
           return super.getHeaders();
        }

    }

}
