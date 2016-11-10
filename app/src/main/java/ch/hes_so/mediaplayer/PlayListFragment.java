package ch.hes_so.mediaplayer;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PlayListFragment extends Fragment {
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play_list, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);


        //--------------------------------------
        // check if the permission is granted
        //--------------------------------------

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init_phone_music_grid();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void init_phone_music_grid() {
        // Init songs list (Instanciate Adapter & Manager)
        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    /**************************************************************************
     * Check if the permission has been granted, if not terminate the activity
     **************************************************************************/

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.init_phone_music_grid();
                } else {
                    new AlertDialog.Builder(getActivity()).setTitle("Permission Error").setMessage("Permission to access your media not granted!").setNeutralButton("Close", null).show();
                    getActivity().finish();
                }

                return;
            }
        }
    }
}
