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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jbtronics.recolldroid.api.Result;

/**
 * A fragment representing a single Result detail screen.
 * This fragment is either contained in a {@link ResultListActivity}
 * in two-pane mode (on tablets) or a {@link ResultDetailActivity}
 * on handsets.
 */
public class ResultDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Result mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ResultDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = QueryContentProvider.query.getResult(getArguments().getInt(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab_result);
            //fab.setImageResource(mItem.getIconRes());
            //fab.setBackgroundColor(getResources().getColor(R.color.colorDetailFab));
            if (appBarLayout != null) {
                //appBarLayout.setTitle(mItem.getFilename());
                appBarLayout.setTitle(mItem.getLabel());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.result_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            //((TextView) rootView.findViewById(R.id.txt_detail_size)).setText(mItem.getSize().toString());
            //((TextView) rootView.findViewById(R.id.txt_detail_author)).setText(mItem.getAuthor());
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String path = sharedPref.getString("pref_key_path_file", "");
            ((TextView) rootView.findViewById(R.id.txt_result_title)).setText(mItem.getTitle());
            ((TextView) rootView.findViewById(R.id.txt_result_filename)).setText(mItem.getFilename());
            ((TextView) rootView.findViewById(R.id.txt_result_author)).setText(mItem.getAuthor());
            ((TextView) rootView.findViewById(R.id.txt_result_abstract)).setText(mItem.getAbstract());
            ((TextView) rootView.findViewById(R.id.txt_result_ipath)).setText(mItem.getIpath());
            ((TextView) rootView.findViewById(R.id.txt_result_folder)).setText(mItem.getFolder(path));
            ((TextView) rootView.findViewById(R.id.txt_result_size)).setText(mItem.getSize().toString());
            ((TextView) rootView.findViewById(R.id.txt_result_lastmod)).setText(mItem.getMtime().toString());

        }

        return rootView;
    }
}
