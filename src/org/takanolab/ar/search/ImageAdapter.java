package org.takanolab.ar.search;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import jp.androidgroup.nyartoolkit.R;

class BindData {
  String text;
  int imageResourceId;

  public BindData(String text, int id) {
    this.text = text;
    this.imageResourceId = id;
  }
}

class ViewHolder {
  TextView textView;
  ImageView imageView;
}

public class ImageAdapter extends ArrayAdapter<BindData> {
  private LayoutInflater inflater;

  public ImageAdapter(Context context, List<BindData> objects) {
    super(context, 0, objects);
    this.inflater = (LayoutInflater) context
      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.result_list_item,
        parent, false);
      holder = new ViewHolder();
      holder.textView = (TextView) convertView
        .findViewById(R.id.textView1);
      holder.imageView = (ImageView) convertView
        .findViewById(R.id.imageView1);
      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    BindData data = getItem(position);
    holder.textView.setText(data.text);
    holder.imageView.setImageResource(data.imageResourceId);
    return convertView;
  }
}