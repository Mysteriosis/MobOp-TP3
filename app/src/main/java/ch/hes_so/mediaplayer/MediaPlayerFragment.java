package ch.hes_so.mediaplayer;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MediaPlayerFragment extends Fragment {
    private static Integer currentSongIndex = null;
    private Runnable updateSongTime = null;
    private Handler myHandler = new Handler();

    private ImageView album_art;
    private TextView song_title;
    private TextView song_album;
    private TextView song_artist;

    private ProgressBar songProgressBar;
    private TextView current_time;
    private TextView total_time;

    private ImageView playButton;
    private ImageView stopButton;
    private ImageView nextButton;
    private ImageView prevButton;

    protected Boolean stopped = false;
    protected Boolean paused = false;

    public MediaPlayer mediaPlayer;
    public SongListManager songListManager;

    public static MediaPlayerFragment newInstance(int index) {
        currentSongIndex = index;
        return new MediaPlayerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_media_player, container, false);

        // initFields
        this.album_art = (ImageView) v.findViewById(R.id.album_art);
        this.song_title = (TextView) v.findViewById(R.id.song_title);
        this.song_album = (TextView) v.findViewById(R.id.song_album);
        this.song_artist = (TextView) v.findViewById(R.id.song_artist);
        this.current_time = (TextView) v.findViewById(R.id.currentTime);
        this.total_time = (TextView) v.findViewById(R.id.totalTime);

        // initButtons
        this.playButton = (ImageButton) v.findViewById(R.id.playImageButton);
        this.stopButton = (ImageButton) v.findViewById(R.id.stopImageButton);
        this.nextButton = (ImageButton) v.findViewById(R.id.nextImageButton);
        this.prevButton = (ImageButton) v.findViewById(R.id.prevImageButton);
        this.songProgressBar = (ProgressBar) v.findViewById(R.id.songProgressBar);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            if (currentSongIndex < songListManager.getPlayListLength() - 1) {
                currentSongIndex++;
            } else {
                Toast toast = Toast.makeText(getActivity(), "End of list!", Toast.LENGTH_SHORT);
                toast.show();
            }

            playSong(currentSongIndex);
            }
        });

        songListManager = new SongListManager(getActivity());
        setListenerToButton();

        playSong(currentSongIndex);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

    }

    @Override
    public void onStop() {
        super.onStop();
        this.mediaPlayer.reset();
        this.mediaPlayer.release();
        this.myHandler.removeCallbacks(this.updateSongTime);

        PlayListFragment frag = new PlayListFragment();
        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getTextTime(Integer milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes) -
                TimeUnit.HOURS.toSeconds(hours);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setSongInfo(Integer index) {
        Bitmap img = this.songListManager.getArtwork(index);

        if(img != null)
            this.album_art.setImageBitmap(img);

        this.song_artist.setText(this.songListManager.getArtist(index));
        this.song_album.setText(this.songListManager.getAlbum(index));
        this.song_title.setText(this.songListManager.getTitle(index));
        this.total_time.setText(getTextTime(this.mediaPlayer.getDuration()));
    }

    public void playSong(int songIndex) {
        try {
            this.mediaPlayer.reset();
            this.mediaPlayer.setDataSource(this.songListManager.getSongFileName(songIndex));
            this.mediaPlayer.prepare();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.mediaPlayer.start();

        setSongInfo(songIndex);

        // ProgressBar + Current Time
        if(this.updateSongTime != null)
            this.myHandler.removeCallbacks(this.updateSongTime);

        this.updateSongTime = new Runnable() {
            public void run() {
                Integer startTime = mediaPlayer.getCurrentPosition();
                current_time.setText(getTextTime(startTime));
                songProgressBar.setMax(mediaPlayer.getDuration());
                songProgressBar.setProgress(startTime);
                myHandler.postDelayed(this, 500);
            }
        };
        myHandler.postDelayed(updateSongTime,500);
        playButton.setImageResource(R.drawable.ic_pause_black_24dp);
    }

    private void setListenerToButton() {

        /******************************
         * START button initialisation
         *******************************/
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewParam) {
                if (stopped) {
                    playSong(currentSongIndex);
                    stopped = false;
                    paused = false;
                    playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                }
                else if(paused) {
                    mediaPlayer.start();
                    paused = false;
                }
                else {
                    mediaPlayer.pause();
                    paused = true;
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            }
        });

        /******************************
         * STOP button initialisation
         *******************************/
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewParam) {
                mediaPlayer.stop();
                current_time.setText("00:00:00");

                if (!stopped) {
                    stopped = true;
                    paused = false;
                    playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                }
            }
        });

        /******************************
         * PREVIOUS button initialisation
         *******************************/
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewParam) {
                if(currentSongIndex > 0) {
                    currentSongIndex--;
                    playSong(currentSongIndex);
                }
            }
        });

        /******************************
         * NEXT button initialisation
         *******************************/
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewParam) {
                if(currentSongIndex < songListManager.getPlayListLength()) {
                    currentSongIndex++;
                    playSong(currentSongIndex);
                }
                else {
                    Toast toast = Toast.makeText(getActivity(), "End of list!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
}
