package com.perpetual.corn;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFrag extends Fragment implements View.OnClickListener{

    boolean isConnected;
    Button btnConnection;

    public ConnectionFrag() {
        // Required empty public constructor
        isConnected = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection, container, false);
        btnConnection = (Button) view.findViewById(R.id.btn_conn);
        btnConnection.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {


        if(isConnected){
            //disconnect

        }else{
            //connect

        }

        isConnected = !isConnected;
        btnConnection.setText((isConnected)?"Disconnect":"Connect");

    }
}
