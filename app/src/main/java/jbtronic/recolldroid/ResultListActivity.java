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

package jbtronic.recolldroid;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import jbtronics.recolldroid.api.Query;
import jbtronics.recolldroid.api.QueryOptions;
import jbtronics.recolldroid.api.Result;



/**
 * An activity representing a list of Results. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ResultDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ResultListActivity extends AppCompatActivity
    implements Query.onQueryError{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        final TextView text = (TextView) findViewById(R.id.txt_result_text);
        text.setVisibility(View.VISIBLE);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.result_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        handleIntent(getIntent());

        if (findViewById(R.id.result_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView,Query q) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        final ResultItemRecyclerViewAdapter r = new ResultItemRecyclerViewAdapter(q);

        //Called if RecyclerView is on bottom
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                QueryContentProvider.query.setOnCompleteHandler(new Query.onQueryComplete() {
                    @Override
                    public void querycompleted(Query sender, int page) {
                        r.notifyDataSetChanged();
                    }
                });
                QueryContentProvider.query.loadNextPage();
            }
        });

        recyclerView.setAdapter(r);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    //Func for Handling search intents
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            final ProgressBar progressBar = (ProgressBar)  findViewById(R.id.progress_bar);
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.result_list);
            final TextView text = (TextView) findViewById(R.id.txt_result_text);


            text.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String addr = sharedPref.getString("pref_key_server_addr", "");
            Boolean auth = sharedPref.getBoolean("pref_key_auth_enable",false);
            QueryOptions options = new QueryOptions();
            if(auth)
            {
                String user = sharedPref.getString("pref_key_auth_user", "");
                String pass = sharedPref.getString("pref_key_auth_pass","");
                QueryContentProvider.query = new Query(addr,this,query,user,pass,options);
            }
            else
            {
                QueryContentProvider.query = new Query(addr,this,query,options);
            }

            QueryContentProvider.query.setOnCompleteHandler(new Query.onQueryComplete() {
                @Override
                public void querycompleted(Query sender, int page) {
                    if(sender.getResults().size() == 0) //If got no results
                    {
                        text.setText("No Results!");
                        text.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        setupRecyclerView(recyclerView, sender);
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        Log.d("ResultListActivity", "Showing complete!");
                    }
                }
            });
            QueryContentProvider.query.setOnErrorHandler(this);
            QueryContentProvider.query.makeQuery();

        }
    }

    @Override
    public void onBackPressed() {
        SearchView searchView = (SearchView) findViewById(R.id.action_search);
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void queryerror(VolleyError error, Exception e) {
        final ProgressBar progressBar = (ProgressBar)  findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        if(error!=null) {
            Toast t = Toast.makeText(this, "Network Error" + error.getMessage() , Toast.LENGTH_SHORT);
            t.show();
        }

        if(e!=null)
        {
            Toast t = Toast.makeText(this, "Network Error" + e.getMessage() , Toast.LENGTH_SHORT);
            t.show();
        }
    }


    public class ResultItemRecyclerViewAdapter
            extends RecyclerView.Adapter<ResultItemRecyclerViewAdapter.ViewHolder> {

        private Query query;
        private int lastPosition;

        public ResultItemRecyclerViewAdapter(Query query) {
            this.query = query;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.result_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Result r = query.getResult(position);
            holder.mResult = r;
            holder.mTitle.setText(r.getHeader());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            String path = sharedPref.getString("pref_key_path_file", "");

            //holder.mIcon.setImageResource(r.getIconRes());
            holder.mTitle.setCompoundDrawablesWithIntrinsicBounds( r.getIconRes(), 0, 0, 0);
            holder.mFolder.setText(r.getFolder(path));
            holder.mPreview.setText(Html.fromHtml(r.getFormattedSnippet())); //Other methods are API 24 or higher.

            if(r.getIpath().equals(""))
            {
                holder.mIpath.setVisibility(View.GONE);
            }
            else
            {
                holder.mIpath.setText(r.getIpath());
            }

            setAnimation(holder.container, position);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putInt(ResultDetailFragment.ARG_ITEM_ID, position);
                        ResultDetailFragment fragment = new ResultDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.result_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ResultDetailActivity.class);
                        intent.putExtra(ResultDetailFragment.ARG_ITEM_ID, position);

                        context.startActivity(intent);
                    }
                }
            });
        }

        private void setAnimation(View viewToAnimate, int position)
        {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition)
            {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
                viewToAnimate.startAnimation(animation);
            }
            lastPosition = position;
        }

        @Override
        public int getItemCount() {
            return query.getResults().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mView;
            public TextView mTitle;
            public TextView mPreview;
            public TextView mFolder;
            public TextView mIpath;
            public View container;
            public Result mResult;
            public Button btn_download;
            public Button btn_preview;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitle = (TextView) view.findViewById(R.id.result_list_title);
                mFolder = (TextView) view.findViewById(R.id.result_list_folder);
                mPreview = (TextView) view.findViewById(R.id.result_list_preview);
                mIpath = (TextView) view.findViewById(R.id.result_list_ipath);
                container =  view.findViewById(R.id.cv);
                mResult = null;
                btn_preview = (Button) view.findViewById(R.id.result_btn_preview);
                btn_download = (Button) view.findViewById(R.id.result_btn_download);
                setBtnHandlers();
            }

            void setBtnHandlers()
            {
                btn_preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = QueryContentProvider.query.getPreviewURL(mResult);

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ResultListActivity.this);
                        Boolean external = sharedPref.getBoolean("pref_key_use_external_preview", false);

                        if(!external) {
                            showWebViewDialog(url);
                        }
                        else
                        {
                            openBrowser(url);
                        }
                    }
                });

                btn_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = QueryContentProvider.query.getDownloadURL(mResult);
                        downloadURL(url);
                    }
                });
            }

            public void downloadURL(String url)
            {
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                dm.enqueue(request);
            }

            public void openBrowser(String url)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

            public void showWebViewDialog(String url)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(ResultListActivity.this);
                alert.setTitle("Preview");

                WebView wv = new WebView(ResultListActivity.this);
                wv.loadUrl(url);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }


            @Override
            public String toString() {
                return super.toString() + " '" + mTitle.getText() + "'";
            }
        }
    }

    public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
        //public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

        private int previousTotal = 0; // The total number of items in the dataset after the last load
        private boolean loading = true; // True if we are still waiting for the last set of data to load.
        private int visibleThreshold = 10; // The minimum amount of items to have below your current scroll position before loading more.
        int firstVisibleItem, visibleItemCount, totalItemCount;

        private int current_page = 1;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if(firstVisibleItem >= 1)    //Show ScrollUp Fab only after some items
            {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setVisibility(View.VISIBLE);
            }
            else
            {
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setVisibility(View.GONE);
            }

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached

                // Do something
                current_page++;

                onLoadMore(current_page);

                loading = true;
            }
        }

        public abstract void onLoadMore(int current_page);
    }
}
