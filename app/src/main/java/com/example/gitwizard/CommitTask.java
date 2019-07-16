package com.example.gitwizard;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.gitwizard.model.Commit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CommitTask extends AsyncTask<String, Void, String> {

    private String user;
    private String repo;
    private View view;
    private RequestQueue queue;
    private Activity activity;


    public CommitTask(String user, String repo, View view, RequestQueue queue, Activity activity) {
        this.user = user;
        this.repo = repo;
        this.view = view;
        this.queue = queue;
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... strings) {
        String url = activity.getString(R.string.BASE_URL) + "repos/" + user + "/" + repo + "/commits";
        Log.i("INFO", "Connecting to : " + url);

        //Volley request to Git hub API
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Commit>>() {
                    }.getType();
                    List<Commit> commits = gson.fromJson(response, listType);
                    Log.i("INFO", commits.size() + " Commits Found for this repo...");
                    LinearLayout scrollView = activity.findViewById(R.id.scrollview_linear_layout);
                    scrollView.removeAllViews();

                    LayoutInflater vi = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for (Commit commit : commits) {
                        View commitView = vi.inflate(R.layout.commit_layout, null);

                        // set committer
                        TextView committerTextView = commitView.findViewById(R.id.label_committer);
                        committerTextView.setText(commit.getCommit().getAuthor().getName());

                        // set hash
                        TextView hashTextView = commitView.findViewById(R.id.label_commit_hash);
                        hashTextView.setText(commit.getSha());

                        //set Message
                        TextView messageTextView = commitView.findViewById(R.id.label_commit_message);
                        messageTextView.setText(commit.getCommit().getMessage());

                        scrollView.addView(commitView);
                    }
                } catch (Exception e) {
                    Log.e("EXCEPTION", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                if (responseBody.contains("Not Found")) {
                    Snackbar.make(view, "Repo does not exist...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "Error getting commits...", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                Log.e("NETERRROR", responseBody);
            }
        });
        queue.add(stringRequest);


        return null;
    }
}
