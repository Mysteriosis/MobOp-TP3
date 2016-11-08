package ch.hes_so.mediaplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PlayerMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        PlayListFragment frag = new PlayListFragment();
        getFragmentManager().beginTransaction().add(android.R.id.content, frag).commit();
    }
}
