package com.confucius.sample.institutedashboard;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by imbisibol on 11/13/2015.
 */
public class InstructorListAdapter extends ArrayAdapter<DATAInstructor> {
    Context context;
    int layoutResourceId;
    ArrayList<DATAInstructor> data = new ArrayList<DATAInstructor>();

    public InstructorListAdapter(Context context, int layoutResourceId,
                             ArrayList<DATAInstructor> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public DATAInstructor getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        InstructorListItem holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new InstructorListItem();
            holder.Id  = (TextView) row.findViewById(R.id.lblInstructorId);
            holder.InstructorName = (TextView) row.findViewById(R.id.lblInstructorName);
            holder.JobDescription = (TextView) row.findViewById(R.id.lblJobDescription);
            holder.Department = (TextView) row.findViewById(R.id.lblDepartment);
            holder.AvatarURL = (ImageView) row.findViewById(R.id.imgAvatarURL);

            row.setTag(holder);

        } else {
            holder = (InstructorListItem) row.getTag();
        }

        DATAInstructor item = data.get(position);
        holder.Id.setText(item.Id);
        String instructorName = "";
        if(item.FirstName == null)
            item.FirstName = "";
        if(item.LastName == null)
            item.LastName = "";
        holder.InstructorName.setText((item.FirstName + " " + item.LastName).trim());
        holder.JobDescription.setText(String.valueOf(item.JobDescription));
        holder.Department.setText(String.valueOf(item.Department));

        if (item.AvatarURL != null && item.AvatarURL != "") {
            if(item.AvatarURL.equals(getContext().getString(R.string.DEFAULT_user_avatar)))
                item.AvatarURL = getContext().getString(R.string.DEFAULT_image_domain) + item.AvatarURL;

            Common.getImageLoader(null).displayImage(item.AvatarURL, holder.AvatarURL);
        }

        return row;

    }

    static class InstructorListItem
    {
        public TextView Id;
        public TextView InstructorName;
        public TextView JobDescription;
        public TextView Department;

        public ImageView AvatarURL;
    }
}
