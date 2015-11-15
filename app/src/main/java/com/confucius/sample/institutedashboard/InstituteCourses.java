package com.confucius.sample.institutedashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class InstituteCourses extends Fragment {

    public String InstructorId;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private CourseTask mAuthTask = null;
    private ProgressBar mProgressBar;
    private View mStampListBody;
    private CourseListAdapter courseAdapter;
    public GridView gridview;

    public ArrayList<DATACourse> courseData = new ArrayList<DATACourse>();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InstituteCourses newInstance(int sectionNumber) {
        InstituteCourses fragment = new InstituteCourses();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public InstituteCourses() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.institute_courses, container, false);

        mProgressBar = ((ProgressBar)rootView.findViewById(R.id.login_progress));
        mStampListBody = (rootView.findViewById(R.id.dvStampListBody));
        gridview = (GridView)rootView.findViewById(R.id.gvDataList);

        //GET DATA
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Toast.makeText(getActivity().getBaseContext(), courseData.get(position).Id, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity().getBaseContext(), CourseMain.class);
                intent.putExtra(getString(R.string.INTENT_CourseId), courseData.get(position).Id);
                startActivity(intent);
            }
        });

        GetCourses();

        return rootView;
    }


    public void GetCourses() {

        if (mAuthTask != null) {
            return;
        }

        courseData = new ArrayList<>();
        mAuthTask = new CourseTask(getActivity().getString(R.string.tempInstituteId), getActivity().getString(R.string.tempInstituteName), InstructorId);
        mAuthTask.execute((Void) null);
    }


    public class CourseTask extends AsyncTask<Void, Void, Boolean> {

            private final String InstitutionId;
            private final String InstitutionName;
            private final String InstructorId;

            CourseTask(String institutionId, String institutionName, String instructorId) {
                InstitutionId = institutionId;
                InstitutionName = institutionName;
                InstructorId = instructorId;
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
                comm.setAPIURL(getActivity().getString(R.string.ConfuciusLearningAPIURL));
                jsonResponse = comm.GetAPI("/api/Course/?institutionId=" + InstitutionId + "&institutionName=" + InstitutionName
                        + "&authToken=" + getActivity().getString(R.string.ApplicationId) + "&pageNo=1&pageSize=100000");

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject responseData = jsonResponse.getJSONObject("ResponseData");

                        if(responseData != null) {

                            //CHECK COURSE MEMBERSHIP
                            ArrayList<String> courseIds = new ArrayList<>();
                            JSONObject courseMembershipJSON = new JSONObject();
                            if(InstructorId != null) {
                                Common comm2 = new Common();
                                comm2.setAPIURL(getActivity().getString(R.string.ConfuciusLearningAPIURL));
                                courseMembershipJSON = comm2.GetAPI("/api/CourseMember/?userId=" + InstructorId + "&courseId=" +
                                        "&institutionId=" + InstitutionId + "&getAll=false&authToken=" + getActivity().getString(R.string.ApplicationId));

                                JSONArray courseMembers =  courseMembershipJSON.getJSONObject("ResponseData").getJSONArray("CourseMembers");
                                for(int ctr=0;ctr<courseMembers.length();ctr++) {
                                    courseIds.add(courseMembers.getJSONObject(ctr).getString("CourseId"));
                                }
                            }

                            //DO SOMETHING
                            JSONArray courses = responseData.getJSONArray("Courses");

                            for(int ctr=0;ctr<courses.length();ctr++) {
                                DATACourse newData = new DATACourse();

                                newData.Id = courses.getJSONObject(ctr).getString("Id");

                                if(courseIds != null && courseIds.size() > 0 && !courseIds.contains(newData.Id))
                                    continue;

                                newData.CourseName = courses.getJSONObject(ctr).getString("CourseName");
                                newData.CourseImageURL = courses.getJSONObject(ctr).getString("CourseImageURL");
                                newData.SubjectArea = courses.getJSONObject(ctr).getString("SubjectArea");

                                courseData.add(newData);
                            }
                        }


                        loginSuccess = true;

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

            if (success) {

                courseAdapter = new CourseListAdapter(getActivity(), R.layout.course_listrow, courseData);
                gridview.setAdapter(courseAdapter);

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity().getBaseContext());
                dialog.setTitle("Message Alert");
                dialog.setMessage("Failed tor retrieve Course List!");
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