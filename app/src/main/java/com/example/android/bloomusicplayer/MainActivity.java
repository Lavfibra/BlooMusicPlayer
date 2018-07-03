package com.example.android.bloomusicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.android.bloomusicplayer.model.SongList;
import com.vistrav.ask.Ask;


public class MainActivity extends AppCompatActivity {
    public SongList mSongsList;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Ask.on(this)
                    .id(1)
                    .forPermissions(Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withRationales("Permesso di scrittura sulla memoria esterna necessario",
                            "Per accedere alla tua musica")
                    .go();
        }
        mSongsList = new SongList();
        bottomNavigationView =
                findViewById(R.id.navigation);

        viewPager = findViewById(R.id.mainmenupager);
        viewPager.setAdapter(new MainMenuPagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position, true);
                if (position == 0)
                    bottomNavigationView.setSelectedItemId(R.id.navigation_songs);
                if (position == 1)
                    bottomNavigationView.setSelectedItemId(R.id.navigation_artists);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.navigation_songs:
                            viewPager.setCurrentItem(0, true);
                            break;
                        case R.id.navigation_artists:
                            viewPager.setCurrentItem(1, true);
                    }
                    return true;
                });

    }

    public class MainMenuPagerAdapter extends FragmentPagerAdapter {

        MainMenuPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return SongsFragment.newInstance(mSongsList);
            if (position == 1)
                return ArtistsFragment.newInstance(mSongsList);
            return new Fragment();
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
