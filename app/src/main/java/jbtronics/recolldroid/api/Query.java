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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Created by janhb on 18.08.2016.
 */
public class Query {
    private SortType sort;
    private String after;
    private Boolean ascending;
    private String query;
    private Integer page;
    private String dir;
    private String before;
    private ArrayList<Result> results;

    public Query(SortType sort,String after,Boolean ascending,String query,Integer page,String dir,String before,ArrayList<Result> results)
    {
        this.sort = sort;
        this.after = after;
        this.ascending = ascending;
        this.query = query;
        if(page>=0)
        {
            throw new InputMismatchException("page number must be > 0!");
        }
        else
        {
            this.page = page;
        }
        this.dir = dir;
        this.before = before;
        this.results = results;
    }

    public Query(JSONObject query, JSONArray results)
    {
        try {
            String sort_str = query.getString("sort");
            after = query.getString("after");
            Integer ascending_int = query.getInt("ascending");
            this.query = query.getString("query");
            page = query.getInt("page");
            dir = query.getString("dir");
            before = query.getString("before");

            this.results = new ArrayList<Result>();

            for(int i=0; i < results.length(); i++){
                JSONObject obj = results.getJSONObject(i);
                this.results.add(new Result(obj));
            }

        } catch (JSONException e) {
            Log.w("Query","Could not parse JSON:",e);
        }

    }

    public ArrayList<Result> getResults() {
        return results;
    }

    public void addResult(Result result)
    {
        results.add(result);
    }

    public String getAfter() {
        return after;
    }

    public boolean getAscending()
    {
        return ascending;
    }

    public String getQuery()
    {
        return query;
    }

    public Integer getPage()
    {
        return page;
    }

    public String getDir()
    {
        return dir;
    }

    public String getBefore()
    {
        return before;
    }

    public void setAfter(String after) {
        this.after = after;
    }


    public URL getPreview(Result result)
    {
        Integer i = results.indexOf(result);
        return null;
    }

}
