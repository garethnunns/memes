package com.garethnunns.memestagram;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment {
    public static final String ARG_MEME = "arg_meme";

    private Uri meme;

    private SharedPreferences login;

    private Bitmap bitmap;

    private boolean uploading = false;

    public static AddFragment newInstance(Uri meme) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEME,meme.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(!memestagram.loggedIn(getContext()))
            memestagram.logout(getContext(),getActivity());

        login = memestagram.getLogin(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null)
            meme = Uri.parse(savedInstanceState.getString(ARG_MEME));
        else {
            Bundle args = getArguments();
            meme = Uri.parse(args.getString(ARG_MEME));
        }

        ImageView addImage = (ImageView) view.findViewById(R.id.add_image);

        try {
            //Getting the Bitmap from Gallery
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), meme);
            //Setting the Bitmap to ImageView
            addImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
        }

        Button button = (Button) view.findViewById(R.id.add_post);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!uploading) upload();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ARG_MEME,meme.toString());
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    private void upload() {
        if(!memestagram.internetAvailable(getContext())) {
            Toast.makeText(getContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        uploading = true;

        final View view = getView();

        final View progress = view.findViewById(R.id.feed_progress);
        showProgress(progress);

        String url = getString(R.string.api)+"add";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
            new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse netResponse) {
                    String response = new String(netResponse.data);
                    try {
                        JSONObject jsonRes = new JSONObject(response);
                        Boolean success = jsonRes.getBoolean("success");
                        if(success) {
                            // get the meme and store it
                            JSONObject jsonMeme = jsonRes.getJSONObject("meme");
                            memestagram.insertMeme(getContext(), jsonMeme);

                            int idmeme = jsonMeme.getInt("idmeme");

                            Log.i("click", "Going to meme " + idmeme);
                            String fragTitle = "Meme " + idmeme;
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            Fragment frag = MemeFragment.newInstance(idmeme);
                            Fragment already = fm.findFragmentByTag(fragTitle);
                            if (already != null) frag = already;
                            fm.beginTransaction()
                                    .replace(R.id.container, frag, fragTitle)
                                    .addToBackStack(fragTitle)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .commit();
                        }
                        else
                            Toast.makeText(getContext(), jsonRes.getString("error"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        System.out.println(response);
                        Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                    }
                    uploading = false;
                    showProgress(progress);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(), getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                    uploading = false;
                    showProgress(progress);
                }
            }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                EditText tCaption = (EditText) view.findViewById(R.id.add_caption);

                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("key", login.getString("key",""));
                params.put("caption",tCaption.getText().toString().trim());
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("file", new DataPart("image.jpg", getStringImage(bitmap), "image/jpeg"));

                return params;
            }
        };
        Volley.newRequestQueue(getContext()).add(multipartRequest);
    }

    public byte[] getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return baos.toByteArray();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final View progressView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressView.setVisibility(uploading ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    uploading ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(uploading ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(uploading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_share).setVisible(false);
    }
}
