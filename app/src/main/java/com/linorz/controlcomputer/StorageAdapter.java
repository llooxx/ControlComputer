package com.linorz.controlcomputer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.linorz.controlcomputer.tools.StaticMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by linorz on 2017/7/15.
 */

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.StorageItem> {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> mapList;
    private List<CircleProgressBar> cpb_list = new ArrayList<>();

    public StorageAdapter(Context context, List<Map<String, Object>> mapList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
    }

    public void remove(int position) {
        mapList.remove(position);
        notifyItemRemoved(position);
    }

    public void add(String text, String path, int position) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", text);
        map.put("path", path);
        map.put("progress", 0);
        mapList.add(position, map);
        notifyItemInserted(position);
    }

    public int add(String name, String path) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("path", path);
        map.put("progress", 0);
        mapList.add(map);
        return mapList.size() - 1;
    }

    @Override
    public StorageItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StorageItem(inflater.inflate(R.layout.storage_item, parent, false));
    }

    @Override
    public void onBindViewHolder(StorageItem item, int position) {
        item.name.setText((String) mapList.get(position).get("name"));
        item.path = (String) mapList.get(position).get("path");
        int k = (int) mapList.get(position).get("progress");
        if (k != -1) {
            item.cpb.setProgress(k);
            item.cpb.setProgressTextFormatPattern(k + "％");
        } else {
            item.cpb.setProgress(0);
            item.cpb.setProgressTextFormatPattern("失败");
        }
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public CircleProgressBar getCPB(int index) {
        try {
            return cpb_list.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public class StorageItem extends RecyclerView.ViewHolder {
        TextView name;
        CircleProgressBar cpb;
        String path;

        public StorageItem(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.item_name);
            cpb = (CircleProgressBar) view.findViewById(R.id.item_progress);
            cpb_list.add(cpb);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StaticMethod.openFile(context, new File(path), name.getText().toString());
                }
            });
        }
    }
}