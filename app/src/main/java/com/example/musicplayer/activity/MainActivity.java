package com.example.musicplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.Adapter.MusicPlayerAdapter;
import com.example.musicplayer.R;
import com.example.musicplayer.bean.MusicPlayerBean;
import com.example.musicplayer.fragment.AboutFragment;
import com.example.musicplayer.fragment.IntroductionFragment;
import com.example.musicplayer.fragment.MessageFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView nextIv, playIv, lastIv,menuIv;
    private TextView singerTv, songTv;
    private RecyclerView musicRv;

    //    数据源
    List<MusicPlayerBean> mDates = new ArrayList<>();
    //    适配器
    private MusicPlayerAdapter adapter;

    //     记录当前正在播放的歌曲的位置
    int currnetPlayPosition = -1;

    //     记录暂停音乐时进度条的位置
    int currnetPausePositionInSong = 0;

    MediaPlayer mediaPlayer;

    //    针对sd卡读取权限申请
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //     侧滑
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Fragment fragment_about, fragment_introduction, fragment_message;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int Notification_height;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mediaPlayer = new MediaPlayer();
//        设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        adapter = new MusicPlayerAdapter(this, mDates);
        musicRv.setLayoutManager(layoutManager);
        musicRv.setAdapter(adapter);

//        加载本地数据源
        loadMusicPlayerDate();
        verifyStoragePermissions(this);

//         设置adapter的监听事件
        setEventListener();
//        侧滑的方法
        setDrawer();
        //设置菜单fragment页面
        setFragment();
    }

    private void setEventListener() {
        /* 设置每一项的点击事件 */
        adapter.setOnItemClickListener(new MusicPlayerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currnetPlayPosition = position;
                MusicPlayerBean musicBean = mDates.get(position);
                playMusicBean(musicBean);
            }
        });
    }

    public void playMusicBean(MusicPlayerBean musicBean) {
        /* 根据传入的对象播放音乐 */
        //                 设置底部显示的歌手名和歌曲名称
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        stopMusic();
//                 重置多媒体播放器
        mediaPlayer.reset();
//                设置新的路径
        try {
            mediaPlayer.setDataSource(musicBean.getPach());
            playMusic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 点击播放按钮播放音乐，或者暂停从新播放
     * 播放音乐有两种情况：
     * 1.从暂停到播放
     * 2.从停止到播放
     * */
    private void playMusic() {
        /* 播放音乐的函数 */
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (currnetPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
//                从暂停位置开始播放
                mediaPlayer.seekTo(currnetPausePositionInSong);
                mediaPlayer.start();
            }
            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }
    private void pauseMusic() {
        /* 暂停音乐的函数 */
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currnetPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.icon_play1);
        }
    }
    private void stopMusic() {
        /* 停止音乐的函数 */
        if (mediaPlayer != null) {
            currnetPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play1);
        }
    }


    //申请权限
    public static void verifyStoragePermissions(Activity activity) {
//        Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
//             We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    // 加载数据源的方法
    private void loadMusicPlayerDate() {
        /* 加载本地存储当中的音乐MP3文件到集合中 （内容接收者）*/
//         1、获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
//         2、获取本地存储音乐的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//         3、开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
//         4、遍历cursor对象
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            if (duration > 30 * 1000) {
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String time = sdf.format(new Date(duration));
//          将一行当中得数据封装到对象当中
                MusicPlayerBean bean = new MusicPlayerBean(sid, song, singer, album, time, path);
                mDates.add(bean);
            }
        }
//      数据源发生变化，提示适配器更新
        adapter.notifyDataSetChanged();
    }

    // 初始化布局
    private void initView() {
        nextIv = findViewById(R.id.music_player_botton_iv_next);
        playIv = findViewById(R.id.music_player_botton_iv_play);
        lastIv = findViewById(R.id.music_player_botton_iv_last);
        singerTv = findViewById(R.id.music_player_botton_tv_singer);
        songTv = findViewById(R.id.music_player_botton_tv_song);
        musicRv = findViewById(R.id.music_player_rv);
        menuIv = findViewById(R.id.menu_icon);
        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);

        nextIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        menuIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);

        //获取状态栏高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        Notification_height = getResources().getDimensionPixelSize(resourceId);
        singerTv.setSelected(true);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.music_player_botton_iv_next:
                if (currnetPlayPosition == -1) {
//                    并没有选择要播放的音乐
                    Toast.makeText(MainActivity.this,"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currnetPlayPosition == mDates.size()-1) {
                    Toast.makeText(MainActivity.this,"已经是最后一首了，没有下一曲",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition + 1;
                MusicPlayerBean nextBean = mDates.get(currnetPlayPosition);
                playMusicBean(nextBean);
                break;
            case R.id.music_player_botton_iv_play:
                if (currnetPlayPosition == -1) {
//                    并没有选择要播放的音乐
                    Toast.makeText(MainActivity.this,"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
//                此时处于播放状态
                    pauseMusic();
                }else{
 //                此时没有播放音乐，点击开始播放音乐
                    playMusic();
                }
                break;
            case R.id.music_player_botton_iv_last:
                if (currnetPlayPosition == -1) {
//                    并没有选择要播放的音乐
                    Toast.makeText(MainActivity.this,"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currnetPlayPosition == 0) {
                    Toast.makeText(MainActivity.this,"已经是第一首了，没有上一曲",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition -1;
                MusicPlayerBean lastBean = mDates.get(currnetPlayPosition);
                playMusicBean(lastBean);
                break;
            case R.id.menu_icon:
                drawer.openDrawer(GravityCompat.START);
                break;
        }
    }


    private void setFragment() {
        fragmentManager = getSupportFragmentManager();
        fragment_message = new MessageFragment();
        fragment_introduction = new IntroductionFragment();
        fragment_about = new AboutFragment();
        transaction = fragmentManager.beginTransaction();
    }


    private void setDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.getChildAt(0).setVerticalScrollBarEnabled(false);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.item_0:
                        Log.d("ItemSelectedListener", "item0");
                        //fragmentManager.popBackStackImmediate(null, 1);
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_about).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_introduction).commit();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragment_message).commit();
//                        Toast.makeText(MainActivity.this, "!!!!", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_1:
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_message).commit();
                        Log.d("ItemSelectedListener", "item1");
                        Toast.makeText(MainActivity.this, "没有新消息嗷~", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_2:
                        //弹出功能介绍
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_introduction).commit();
                        Log.d("ItemSelectedListener", "item2");
//                        Toast.makeText(MainActivity.this, "2", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.item_3:
                        //弹出软件开发介绍
                        transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.fragment, fragment_about).commit();
                        Log.d("ItemSelectedListener", "item3");
//                        Toast.makeText(MainActivity.this, "3", Toast.LENGTH_SHORT).show();
                        break;
                }
                drawer.closeDrawer(GravityCompat.START);
                transaction.addToBackStack(null);
                return true;
            }
        });

        //设置滑动主activity跟随
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                //获取高度宽度
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                Display display = MainActivity.this.getWindowManager().getDefaultDisplay();
                display.getMetrics(metrics);
                //设置activity高度，注意要加上状态栏高度
                RelativeLayout relativeLayout = findViewById(R.id.main_activity);
                relativeLayout.layout(drawerView.getRight(), Notification_height, drawerView.getRight()+metrics.widthPixels, metrics.heightPixels);

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
