package com.confucius.sample.institutedashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import org.w3c.dom.Text;

/**
 * Created by imbisibol on 11/13/2015.
 */
public class CourseDetails extends Fragment {

    private ProgressBar mProgressBar;
    private View mStampListBody;
    private CourseTask mAuthTask = null;
    private DATACourse CourseData;

    private ImageView imgLogo;
    private TextView lblCourseName;
    private TextView lblSubjectArea;
    private WebView wvAbout;


    public static CourseDetails newInstance(int sectionNumber) {
        CourseDetails fragment = new CourseDetails();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.course_details, container, false);

        imgLogo = (ImageView)rootView.findViewById(R.id.imgLogoURL);
        lblCourseName = (TextView)rootView.findViewById(R.id.lblCourseName);
        lblSubjectArea =  (TextView)rootView.findViewById(R.id.lblSubjectArea);
        wvAbout = (WebView)rootView.findViewById(R.id.wvAbout);
        mProgressBar = ((ProgressBar)rootView.findViewById(R.id.login_progress));
        mStampListBody = (rootView.findViewById(R.id.dvStampListBody));

        Intent intent = getActivity().getIntent();
        String courseId = intent.getStringExtra(getString(R.string.INTENT_CourseId));

        GetCourseDetails(courseId);

        return rootView;
    }

    private void GetCourseDetails(String id)
    {
        if (mAuthTask != null) {
            return;
        }

        CourseData = new DATACourse();
        mAuthTask = new CourseTask(id);
        mAuthTask.execute((Void) null);
    }

    public class CourseTask extends AsyncTask<Void, Void, Boolean> {

        private final String CourseId;

        CourseTask(String courseId) {
            CourseId = courseId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            JSONObject jsonResponse = null;

            try {

                JSONObject jsonParam = new JSONObject();

                Common comm = new Common();
                comm.setAPIURL(getString(R.string.ConfuciusLearningAPIURL));
                jsonResponse = comm.GetAPI("/api/Course/?accessCode=&id=" + CourseId
                        + "&authToken=" + getString(R.string.ApplicationId));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject responseData = jsonResponse.getJSONObject("ResponseData");

                        if (responseData != null) {
                            CourseData = new DATACourse();

                            //DO SOMETHING
                            JSONObject course = responseData.getJSONObject("Course");
                            CourseData.Id = course.getString("Id");
                            CourseData.Description = course.getString("Description");
                            CourseData.CourseName = course.getString("CourseName");
                            CourseData.CourseImageURL = course.getString("CourseImageURL");
                            CourseData.SubjectArea = course.getString("SubjectArea");
                            CourseData.OverviewHTMLContent = course.getString("OverviewHTMLContent");
                            CourseData.SyllabusHTMLContent = course.getString("SyllabusHTMLContent");
                        }
                    }

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

            if (success && CourseData != null) {

                String instructorName = "";

                Common.getImageLoader(null).displayImage(CourseData.CourseImageURL, imgLogo);
                lblCourseName.setText(CourseData.CourseName);
                lblSubjectArea.setText(CourseData.SubjectArea);
                wvAbout.loadData("<div style='padding: 20px; padding-top:5px;'>" + CourseData.OverviewHTMLContent + "<h3>Course Syllabus</h3><hr/>" + CourseData.SyllabusHTMLContent + "</div>", "text/html", "utf-8");

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity().getBaseContext());
                dialog.setTitle("Message Alert");
                dialog.setMessage("Failed tor retrieve Course Detail!");
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

