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
public class InstituteFaculty extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public String CourseId;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private InstructorTask mAuthTask = null;
    private ProgressBar mProgressBar;
    private View mStampListBody;
    private InstructorListAdapter instructorAdapter;
    public GridView gridview;

    public ArrayList<DATAInstructor> instructorData = new ArrayList<DATAInstructor>();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InstituteFaculty newInstance(int sectionNumber) {
        InstituteFaculty fragment = new InstituteFaculty();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }

    public InstituteFaculty() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.institute_faculty, container, false);

        mProgressBar = ((ProgressBar)rootView.findViewById(R.id.login_progress));
        mStampListBody = (rootView.findViewById(R.id.dvStampListBody));
        gridview = (GridView)rootView.findViewById(R.id.gvDataList);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Toast.makeText(getActivity().getBaseContext(), instructorData.get(position).Id, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity().getBaseContext(), FacultyMain.class);
                intent.putExtra(getString(R.string.INTENT_FacultyId), instructorData.get(position).Id);
                startActivity(intent);
            }
        });

        GetInstructors();

        return rootView;
    }

    public void GetInstructors() {

        if (mAuthTask != null) {
            return;
        }

        instructorData = new ArrayList<>();
        mAuthTask = new InstructorTask(getActivity().getString(R.string.tempInstituteId), getActivity().getString(R.string.tempInstituteName), CourseId);
        mAuthTask.execute((Void) null);
    }


    public class InstructorTask extends AsyncTask<Void, Void, Boolean> {

        private final String CourseId;
        private final String InstitutionId;
        private final String InstitutionName;

        InstructorTask(String institutionId, String institutionName, String courseId) {
            InstitutionId = institutionId;
            InstitutionName = institutionName;
            CourseId = courseId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            JSONObject jsonResponse = null;

            try {

                JSONObject jsonParam = new JSONObject();

                Common comm = new Common();
                comm.setAPIURL(getActivity().getString(R.string.ConfuciusSecurityAPIURL));
                jsonResponse = comm.GetAPI("/api/ProfileView/?name=&institutionId=" + InstitutionId
                        + "&pageNo=1&pageSize=100000&appId=" + getActivity().getString(R.string.ApplicationId));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject responseData = jsonResponse.getJSONObject("ResponseData");

                        if(responseData != null) {

                            //DO SOMETHING
                            JSONArray courses = responseData.getJSONArray("UserProfileViews");

                            //CHECK COURSE MEMBERSHIP
                            ArrayList<String> memberIds = new ArrayList<>();
                            JSONObject courseMembershipJSON = new JSONObject();
                            if(CourseId != null) {
                                Common comm2 = new Common();
                                comm2.setAPIURL(getActivity().getString(R.string.ConfuciusLearningAPIURL));
                                courseMembershipJSON = comm2.GetAPI("/api/CourseMember/?userId=&courseId=" + CourseId +
                                "&institutionId=" + InstitutionId + "&getAll=false&authToken=" + getActivity().getString(R.string.ApplicationId));

                                JSONArray courseMembers =  courseMembershipJSON.getJSONObject("ResponseData").getJSONArray("CourseMembers");
                                for(int ctr=0;ctr<courseMembers.length();ctr++) {
                                    memberIds.add(courseMembers.getJSONObject(ctr).getString("UserId"));
                                }
                            }

                            for(int ctr=0;ctr<courses.length();ctr++) {
                                DATAInstructor newData = new DATAInstructor();

                                if(courses.getJSONObject(ctr).getString("InstructorProfileDateCreated") != null) {
                                    newData.Id = courses.getJSONObject(ctr).getString("UserId");

                                    if(memberIds != null && memberIds.size() > 0 && !memberIds.contains(newData.Id))
                                        continue;

                                    newData.FirstName = courses.getJSONObject(ctr).getString("FirstName");
                                    newData.LastName = courses.getJSONObject(ctr).getString("LastName");
                                    newData.JobDescription = courses.getJSONObject(ctr).getString("InstructorJobDescription");
                                    newData.Department = courses.getJSONObject(ctr).getString("InstructorDepartment");
                                    newData.AvatarURL = courses.getJSONObject(ctr).getString("AvatarURL");

                                    instructorData.add(newData);
                                }
                            }
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

            if (success) {

                instructorAdapter = new InstructorListAdapter(getActivity(), R.layout.faculty_listrow, instructorData);
                gridview.setAdapter(instructorAdapter);

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity().getBaseContext());
                dialog.setTitle("Message Alert");
                dialog.setMessage("Failed tor retrieve Instructors List!");
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