package com.example.mutualfunds;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

public class FragmentActivity extends androidx.fragment.app.Fragment {

    private final String tableName;

    public FragmentActivity(String table) {
        tableName = table;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        int total = new Database(getContext()).getTotal(tableName);
        if (total != 0) {
            TextView textView = view.findViewById(R.id.fragment_textView);
            textView.setText("Total: " + total);
        }

        ListView listView = view.findViewById(R.id.listView);
        setupListView(tableName, listView);

        return view;
    }

    private void setupListView(String tableName, ListView listView) {
        Database db = new Database(getContext());
        List<stockNode> nodes = db.getTopMost(tableName, 20);
        if (nodes == null) {
            return;
        }
        ListViewAdapter adapter = new ListViewAdapter(getContext(), nodes);
        listView.setAdapter(adapter);
    }
}

class ListViewAdapter extends ArrayAdapter<stockNode> {
    private final List<stockNode> nodes;

    public ListViewAdapter(Context context, List<stockNode> stocks) {
        super(context, R.layout.fragment_layout, stocks);
        nodes = stocks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = Objects.requireNonNull(inflater).inflate(R.layout.list_node, null, true);
        TextView stockName = rowView.findViewById(R.id.stockName);
        TextView freq = rowView.findViewById(R.id.frequency);
        stockName.setText(nodes.get(position).shareName);
        freq.setText(nodes.get(position).frequency + "");
        return rowView;

    }
}