package com.confucius.sample.institutedashboard;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by imbisibol on 11/13/2015.
 */
public class CourseListAdapter extends ArrayAdapter<DATACourse> {
    Context context;
    int layoutResourceId;
    ArrayList<DATACourse> data = new ArrayList<DATACourse>();

    public CourseListAdapter(Context context, int layoutResourceId,
                           ArrayList<DATACourse> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public DATACourse getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CourseListItem holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new CourseListItem();
            holder.Id  = (TextView) row.findViewById(R.id.lblCourseId);
            holder.CourseName = (TextView) row.findViewById(R.id.lblCourseName);
            holder.SubjectArea = (TextView) row.findViewById(R.id.lblSubjectArea);
            holder.CourseImageURL = (ImageView) row.findViewById(R.id.imgCourseImage);

            row.setTag(holder);

        } else {
            holder = (CourseListItem) row.getTag();
        }

        DATACourse item = data.get(position);
        holder.Id.setText(item.Id);
        holder.CourseName.setText(item.CourseName);
        holder.SubjectArea.setText(String.valueOf(item.SubjectArea));

        if (item.CourseImageURL != null && item.CourseImageURL != "")
            Common.getImageLoader(null).displayImage(item.CourseImageURL, holder.CourseImageURL);

        return row;

    }

    static class CourseListItem
    {
        public TextView Id;
        public TextView CourseName;
        public TextView SubjectArea;

        public ImageView CourseImageURL;
    }
}
