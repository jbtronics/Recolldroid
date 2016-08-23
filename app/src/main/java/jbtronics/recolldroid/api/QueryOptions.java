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

import java.util.Date;

/**
 * Created by janhb on 22.08.2016.
 */
public class QueryOptions {
    private String dir;
    private Date before;
    private SortType sort;
    private Date after;
    private Boolean ascending;

    public QueryOptions()
    {
        dir = "<all>";
        before = null;
        sort = SortType.RELEVANCYRATING;
        after = null;
        ascending = false;
    }

    public QueryOptions(SortType sort, Boolean ascending, String dir, Date before,Date after)
    {
        this.sort = sort;
        this.ascending = ascending;
        this.dir = dir;
        this.before = before;
        this.after = after;
    }

    public QueryOptions(SortType sort, Boolean ascending)
    {
        this.sort = sort;
        this.ascending = ascending;
        this.dir = "<all>";
        this.before = null;
        this.after = null;
    }

    public String getDir() {
        return dir;
    }

    public String getBeforeString() {
        return "";
    }

    public SortType getSort() {
        return sort;
    }

    public String getAscendingString()
    {
        if(ascending)
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }

    public String getAfterString() {
        return "";
    }

    public Boolean getAscending() {
        return ascending;
    }
}
