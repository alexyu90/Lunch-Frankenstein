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
public class DemoFragment extends Fragment {


    public DemoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_demo, container, false);

        LinearLayout authentication = (LinearLayout) rootView.findViewById(R.id.authentication);
        LinearLayout Track = (LinearLayout) rootView.findViewById(R.id.track);

        authentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), Authentication1Activity.class);

                startActivity(Intent);
            }
        });

        Track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent (getActivity(), TrackActivity.class);

                startActivity(Intent);
            }
        });

        return rootView;
    }
}
