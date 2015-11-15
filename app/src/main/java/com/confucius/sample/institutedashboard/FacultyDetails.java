package com.confucius.sample.institutedashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by imbisibol on 11/13/2015.
 */
public class FacultyDetails extends Fragment {

    private ProgressBar mProgressBar;
    private View mStampListBody;
    private InstructorTask mAuthTask = null;
    private DATAInstructor InstructorData;

    private ImageView imgAvatarURL;
    private TextView lblJobDescription;
    private TextView lblDepartment;
    private TextView lblInstructorName;
    private WebView wvAbout;


    public static FacultyDetails newInstance(int sectionNumber) {
        FacultyDetails fragment = new FacultyDetails();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.faculty_details, container, false);

        mProgressBar = ((ProgressBar)rootView.findViewById(R.id.login_progress));
        mStampListBody = (rootView.findViewById(R.id.dvStampListBody));

        imgAvatarURL = (ImageView)rootView.findViewById(R.id.imgAvatarURL);
        lblJobDescription = (TextView)rootView.findViewById(R.id.lblJobDescription);
        lblDepartment = (TextView)rootView.findViewById(R.id.lblDepartment);
        lblInstructorName = (TextView)rootView.findViewById(R.id.lblInstructorName);
        wvAbout = (WebView)rootView.findViewById(R.id.wvAbout);

        Intent intent = getActivity().getIntent();
        String instructorId = intent.getStringExtra(getString(R.string.INTENT_FacultyId));

        GetFacultyDetails(instructorId);

        return rootView;
    }

    private void GetFacultyDetails(String id)
    {
        if (mAuthTask != null) {
            return;
        }

        InstructorData = new DATAInstructor();
        mAuthTask = new InstructorTask(id);
        mAuthTask.execute((Void) null);
    }

    public class InstructorTask extends AsyncTask<Void, Void, Boolean> {

        private final String UserId;

        InstructorTask(String userId) {
            UserId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean loginSuccess = false;
            String userId = "";
            String userDisplay = "";
            JSONObject jsonResponse = null;

            try {

                JSONObject jsonParam = new JSONObject();

                Common comm = new Common();
                comm.setAPIURL(getString(R.string.ConfuciusSecurityAPIURL));
                jsonResponse = comm.GetAPI("/api/ProfileView/?userId=" + UserId
                        + "&appId=" + getString(R.string.ApplicationId));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject responseData = jsonResponse.getJSONObject("ResponseData");

                        if (responseData != null) {
                            InstructorData = new DATAInstructor();

                            //DO SOMETHING
                            JSONObject profile = responseData.getJSONObject("UserProfileView");
                            InstructorData.Id = profile.getString("UserId");
                            InstructorData.FirstName = profile.getString("FirstName");
                            InstructorData.LastName = profile.getString("LastName");
                            InstructorData.JobDescription = profile.getString("InstructorJobDescription");
                            InstructorData.Department = profile.getString("InstructorDepartment");
                            InstructorData.AvatarURL = profile.getString("AvatarURL");
                            InstructorData.AboutHTMLContent = profile.getString("InstructorAboutContent");

                        }
                    }


                    loginSuccess = true;

                }

            } catch (Exception ex) {

                String abc = ex.getMessage();

            }


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success && InstructorData != null) {

                String instructorName = "";
                if(InstructorData.FirstName == null)
                    InstructorData.FirstName = "";
                if(InstructorData.LastName == null)
                    InstructorData.LastName = "";
                lblInstructorName.setText((InstructorData.FirstName + " " + InstructorData.LastName).trim());
                lblJobDescription.setText(InstructorData.JobDescription);
                lblDepartment.setText(InstructorData.Department);
                if(InstructorData.AvatarURL.equals(getString(R.string.DEFAULT_user_avatar)))
                    InstructorData.AvatarURL = getString(R.string.DEFAULT_image_domain) + InstructorData.AvatarURL;
                Common.getImageLoader(null).displayImage(InstructorData.AvatarURL, imgAvatarURL);
                wvAbout.loadData("<div style='padding: 5px;'>" + InstructorData.AboutHTMLContent + "</div>", "text/html", "utf-8");

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity().getBaseContext());
                dialog.setTitle("Message Alert");
                dialog.setMessage("Failed tor retrieve Instructor Profile!");
                dialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);


        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
            mStampListBody.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mStampListBody.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mStampListBody.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
