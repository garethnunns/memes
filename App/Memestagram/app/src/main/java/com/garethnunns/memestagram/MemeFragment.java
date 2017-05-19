package com.garethnunns.memestagram;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

public class MemeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_MEME = "arg_meme";
    private int meme;

    private boolean updating = false;
    private boolean firstUpdate = true;
    private boolean end = false; // if they've reached the end

    public static final int MEME_LOADER = 7;
    private MemeAdapter adapter;

    private ShareActionProvider mShareActionProvider;

    public static MemeFragment newInstance(int meme) {
        MemeFragment fragment = new MemeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MEME, meme);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meme, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null) {
            meme = savedInstanceState.getInt(ARG_MEME);
            firstUpdate = false; // don't clear the cache if it's just a screen rotation
        }
        else {
            Bundle args = getArguments();
            meme = args.getInt(ARG_MEME);
        }

        // init the loader
        getLoaderManager().initLoader(MEME_LOADER, null,this);

        adapter = new MemeAdapter(getContext(), null, getActivity(), MEME_LOADER, MemeFragment.this, this);

        //bind the adapter to the listview
        ListView lv = (ListView) view.findViewById(R.id.meme_frag_meme);
        lv.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(ARG_MEME,meme);
        super.onSaveInstanceState(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final View progressView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    updating ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(updating ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getContext(),MemesContract.Tables.buildMemeUriWithID(meme),null,null,null,null);
        Log.i("loader", "onCreateLoader");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        //if(firstUpdate)
            //updateMeme(currentPage, getView());

        TextView found = (TextView) getView().findViewById(R.id.found);
        if((data == null) || (data.getCount()==0))
            found.setText(R.string.error_no_memes);
        else
            found.setText("");

        Log.i("Loader","onLoadFinished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        Log.i("loader","onLoaderReset");
    }

}
