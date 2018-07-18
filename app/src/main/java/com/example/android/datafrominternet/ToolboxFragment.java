package com.example.android.datafrominternet;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class ToolboxFragment extends Fragment {


    public ToolboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_toolbox, container, false);

        LinearLayout serialRead = (LinearLayout) rootView.findViewById(R.id.serialRead);
        LinearLayout cloudSearch = (LinearLayout) rootView.findViewById(R.id.cloudSearch);
        LinearLayout qrReader =  (LinearLayout) rootView.findViewById(R.id.qrReader);
        LinearLayout seeWebPlot = (LinearLayout) rootView.findViewById(R.id.seeWebPlot);
        LinearLayout seeCodePlot = (LinearLayout) rootView.findViewById(R.id.seeCodePlot);

        serialRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(getActivity(), SerialReadActivity.class);

                startActivity(Intent);
            }
        });

        cloudSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), CloudSearchActivity.class);

                startActivity(Intent);
            }
        });

        qrReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), QRReader.class);

                startActivity(Intent);
            }
        });

        seeWebPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), SeeWebPlotActivity.class);

                startActivity(Intent);
            }
        });

        seeCodePlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), SeeCodePlotActivity.class);

                startActivity(Intent);
            }
        });

        return rootView;
    }

}
