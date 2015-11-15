package com.confucius.sample.institutedashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class InstituteDashboard extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private InstituteTask mAuthTask = null;
    private ProgressBar mProgressBar;
    private View mStampListBody;
    private DATAInstitution InstitutionData;

    private TextView lblInstituteName;
    private ImageView imgLogo;
    private ImageView imgBannerURL;
    private WebView wvAbout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InstituteDashboard newInstance(int sectionNumber) {
        InstituteDashboard fragment = new InstituteDashboard();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public InstituteDashboard() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mProgressBar = ((ProgressBar)rootView.findViewById(R.id.login_progress));
        mStampListBody = (rootView.findViewById(R.id.dvContent));

        lblInstituteName = (TextView)rootView.findViewById(R.id.lblInstituteName);
        imgLogo = (ImageView)rootView.findViewById(R.id.imgInstituteLogo);
        imgBannerURL = (ImageView)rootView.findViewById(R.id.imgBannerURL);
        wvAbout = (WebView)rootView.findViewById(R.id.wvAbout);

        GetInstituteProfile();
        
        
        return rootView;
    }

    public void GetInstituteProfile() {

        if (mAuthTask != null) {
            return;
        }

        mAuthTask = new InstituteTask(getActivity().getString(R.string.tempInstituteId));
        mAuthTask.execute((Void) null);
    }

    public class InstituteTask extends AsyncTask<Void, Void, Boolean> {

        private final String InstitutionId;

        InstituteTask(String institutionId) {
            InstitutionId = institutionId;
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
                comm.setAPIURL(getActivity().getString(R.string.ConfuciusSecurityAPIURL));
                jsonResponse = comm.GetAPI("/api/Institution/?id=" + InstitutionId
                        + "&appId=" + getActivity().getString(R.string.ApplicationId));

                if (jsonResponse != null) {

                    String strSuccess = jsonResponse.getString("Success");

                    if (strSuccess == "true") {

                        JSONObject responseData = jsonResponse.getJSONObject("ResponseData");

                        if (responseData != null) {
                            InstitutionData = new DATAInstitution();

                            //DO SOMETHING
                            JSONObject courses = responseData.getJSONObject("Institution");

                            InstitutionData.InstitutionName = courses.getString("InstitutionName");
                            InstitutionData.BannerText = courses.getString("Description");
                            InstitutionData.LogoURL = courses.getString("LogoURL");
                            InstitutionData.BannerURL = courses.getString("BannerURL");
                            InstitutionData.AboutHTMLContent  = courses.getString("AboutHTMLContent");
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

            if (success && InstitutionData != null) {

                lblInstituteName.setText(InstitutionData.InstitutionName);
                wvAbout.loadData("<div style='padding:5px'><p>" + InstitutionData.BannerText + "</p><hr/>" + InstitutionData.AboutHTMLContent + "</div>", "text/html", "utf-8");

                if (InstitutionData.LogoURL != null && InstitutionData.LogoURL.equals(getString(R.string.DEFAULT_blank_institute)))
                    InstitutionData.LogoURL = getString(R.string.DEFAULT_image_domain) + InstitutionData.LogoURL;
                Common.getImageLoader(null).displayImage(InstitutionData.LogoURL, imgLogo);
                Common.getImageLoader(null).displayImage(InstitutionData.BannerURL, imgBannerURL);

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity().getBaseContext());
                dialog.setTitle("Message Alert");
                dialog.setMessage("Failed tor retrieve Stamp List!");
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