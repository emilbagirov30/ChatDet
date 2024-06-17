package com.emil.chatdet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>  {
    private LinkedList<Object> messages;
    private ArrayList<String> timesList;
    private ArrayList<Boolean> isPausedList = new ArrayList<>();
    private ArrayList<Boolean> isChecked = new ArrayList<>();
    private ArrayList<Integer> imageButtons = new ArrayList<>();
    private ArrayList<Integer> stripSoundValues = new ArrayList<>();
    private ArrayList<MediaPlayer> mediaPlayers = new ArrayList<>();

    private ArrayList<Integer> stripSoundMax = new ArrayList<>();
    private ArrayList<Handler> handlers = new ArrayList<>();
    ArrayList<String> listUsernames;
    ArrayList<String> listDetectorResult;
    String username;
    private ConcurrentHashMap<String, Object> userDataMap;
    Context context;
Handler handler = new Handler();
    DialogProfile profileDialog;
    public ChatAdapter (Context context, LinkedList<Object> messages, ArrayList<String> listUsernames,
                        String username, ConcurrentHashMap<String, Object> userDataMap,
                        ArrayList<String> timesList,ArrayList<String> listDetectorResult){
        this.messages = messages;
        this.listUsernames = listUsernames;
        this.username = username;
        this.userDataMap = userDataMap;
        this.context = context;
        this.timesList = timesList;
        this.listDetectorResult = listDetectorResult;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_chat_item, parent, false));
    }

    @SuppressLint({"NotifyDataSetChanged", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Object currentMessage = messages.get(position);
        isPausedList.add(false);
        imageButtons.add(1);
        handlers.add(null);
        stripSoundValues.add(null);
        mediaPlayers.add(null);
        stripSoundMax.add(null);
        isChecked.add(false);
        holder.time.setText(timesList.get(position));
        if (currentMessage instanceof String){
            holder.message.setText(currentMessage.toString());
            holder.message.setVisibility(View.VISIBLE);
            holder.picture.setVisibility(View.GONE);
            holder.stripSound.setVisibility(View.GONE);
            holder.play.setVisibility(View.GONE);
        } else if (currentMessage instanceof File) {
            if (((File) currentMessage).getAbsolutePath().endsWith(".3gp")){
                holder.message.setVisibility(View.GONE);
            holder.picture.setVisibility(View.GONE);
            holder.stripSound.setVisibility(View.VISIBLE);
            holder.play.setVisibility(View.VISIBLE);

            if (mediaPlayers.get(position) == null) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setLooping(false);
                mediaPlayers.set(position, mediaPlayer);
            }


            if (stripSoundValues.get(position) == null) stripSoundValues.set(position, 0);
            if (stripSoundMax.get(position) == null) stripSoundMax.set(position, 0);
            if (handlers.get(position) == null) {
                Handler handler = new Handler();
                handlers.set(position, handler);
            }

            if (imageButtons.get(position) == 1) {
                holder.play.setBackgroundResource(R.drawable.sound_play);
            } else {
                holder.play.setBackgroundResource(R.drawable.sound_pause);
            }


            mediaPlayers.get(position).setOnCompletionListener(mp -> {
                imageButtons.set(position, 1);
                isPausedList.set(position, false);
                mediaPlayers.get(position).seekTo(0);
                mediaPlayers.get(position).reset();
                holder.stripSound.setProgress(0);
                stripSoundValues.set(position, 0);
                notifyItemChanged(position);
            });

            holder.stripSound.setProgress(stripSoundValues.get(position));
            holder.stripSound.setMax(stripSoundMax.get(position));

            mediaPlayers.get(position).setOnPreparedListener(mp -> {
                holder.stripSound.setMax(mediaPlayers.get(position).getDuration());
                stripSoundMax.set(position, mediaPlayers.get(position).getDuration());
                handlers.get(position).post(new Runnable() {
                    @Override
                    public void run() {
                        holder.stripSound.setProgress(mediaPlayers.get(position).getCurrentPosition());
                        stripSoundValues.set(position, mediaPlayers.get(position).getCurrentPosition());
                        handlers.get(position).postDelayed(this, 0);

                    }
                });
            });

            holder.play.setOnClickListener(v -> {
                try {
                    if (mediaPlayers.get(position).isPlaying()) {
                        isPausedList.set(position, true);
                        mediaPlayers.get(position).pause();
                        holder.play.setBackgroundResource(R.drawable.sound_play);
                        imageButtons.set(position, 1);
                        holder.stripSound.setProgress(mediaPlayers.get(position).getCurrentPosition());
                        stripSoundValues.set(position, mediaPlayers.get(position).getCurrentPosition());

                    } else {

                        if (!isPausedList.get(position)) {
                            holder.stripSound.setProgress(0);
                            stripSoundValues.set(position, 0);
                            mediaPlayers.get(position).reset();
                            mediaPlayers.get(position).setDataSource(((File) currentMessage).getAbsolutePath());
                            mediaPlayers.get(position).prepare();
                            mediaPlayers.get(position).start();
                        } else {
                            holder.stripSound.setProgress(mediaPlayers.get(position).getCurrentPosition());
                            stripSoundValues.set(position, mediaPlayers.get(position).getCurrentPosition());
                            mediaPlayers.get(position).seekTo(mediaPlayers.get(position).getCurrentPosition());
                            mediaPlayers.get(position).start();
                        }
                        holder.play.setBackgroundResource(R.drawable.sound_pause);
                        imageButtons.set(position, 0);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        }else{
                holder.picture.setVisibility(View.VISIBLE);
                holder.stripSound.setVisibility(View.GONE);
                holder.play.setVisibility(View.GONE);
                holder.message.setVisibility(View.GONE);
                Glide.with(context).load((File) currentMessage).into(holder.picture);
               // holder.picture.setImageBitmap((Bitmap) currentMessage);

                holder.picture.setOnClickListener(v -> {
                    DialogBigPicture dialog = new DialogBigPicture((File) currentMessage);
                    dialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "Picture");
                });
            }
        }

        if (isChecked.get(position)){
            holder.result.setVisibility(View.VISIBLE);
            if (listDetectorResult.get(position).equals("1"))
                holder.result.setBackgroundResource(R.drawable.marking_true);
            else holder.result.setBackgroundResource(R.drawable.marking_false);
holder.detector.setVisibility(View.GONE);
        }else{
            holder.result.setVisibility(View.GONE);
            holder.detector.setVisibility(View.VISIBLE);
        }


holder.detector.setOnClickListener(v -> {
     DialogDetector dialog = new DialogDetector(context);
     dialog.show();
     handler.postDelayed(() -> {
         if (listDetectorResult.get(position).equals("1")) {
             dialog.result.setText( Html.fromHtml("<font color='#25A542'>Правда</font>"));
             Glide.with(context)
                     .load(R.drawable.det_true)
                     .into(dialog.loading);
             MediaPlayer.create(context, R.raw.true_det).start();
             new Handler().postDelayed(() -> {
                 dialog.dismiss();
                 holder.result.setVisibility(View.VISIBLE);
                 holder.detector.setVisibility(View.GONE);
                 holder.result.setBackgroundResource(R.drawable.marking_true);
             },1500);
         }else{
             dialog.result.setText( Html.fromHtml("<font color='#C11313'>Ложь</font>"));
             Glide.with(context)
                     .load(R.drawable.det_false)
                     .into(dialog.loading);
             MediaPlayer.create(context, R.raw.false_det).start();
             new Handler().postDelayed(() -> {
                 dialog.dismiss();
                 holder.result.setVisibility(View.VISIBLE);
                 holder.detector.setVisibility(View.GONE);
                 holder.result.setBackgroundResource(R.drawable.marking_false);
             },1500);
         }
       isChecked.set(position,true);
     },1500);

});



        if (listUsernames.get(position).equals(username)){
            holder.mainLayout.setLayoutDirection(LinearLayout.LAYOUT_DIRECTION_RTL);
            holder.messageLayout.setBackgroundResource(R.drawable.my_message);
            CardView.LayoutParams params = (CardView.LayoutParams)   holder.card.getLayoutParams();
            params.gravity = Gravity.RIGHT;
            holder.card.setLayoutParams(params);

        }else{
            holder.mainLayout.setLayoutDirection(LinearLayout.LAYOUT_DIRECTION_LTR);
            holder.messageLayout.setBackgroundResource(R.drawable.stranger_message);
            CardView.LayoutParams params = (CardView.LayoutParams)   holder.card.getLayoutParams();
            params.gravity = Gravity.LEFT;
            holder.card.setLayoutParams(params);
        }


        Object avatar = userDataMap.get(listUsernames.get(position));
        if (avatar instanceof String) {
            holder.avatar.setImageResource(R.drawable.default_avatar);
            holder.avatar.setOnClickListener(v -> {
                profileDialog = new DialogProfile(context);
               ImageView avatarDialog = profileDialog.findViewById(R.id.iv_avatar);
               avatarDialog.setImageResource(R.drawable.default_avatar);
               TextView usernameDialog = profileDialog.findViewById(R.id.tv_username);
               usernameDialog.setText(listUsernames.get(position));
                profileDialog.show();
            });
        }
        else if (avatar instanceof File) {
            Glide.with(context)
                    .load((File)avatar)
                    .into( holder.avatar);
            holder.avatar.setOnClickListener(v -> {
                profileDialog = new DialogProfile(context);
                ImageView avatarDialog = profileDialog.findViewById(R.id.iv_avatar);
                Glide.with(context)
                        .load((File)avatar)
                        .into(avatarDialog);
                TextView usernameDialog = profileDialog.findViewById(R.id.tv_username);
                usernameDialog.setText(listUsernames.get(position));
                profileDialog.show();
            });
        }
        holder.username.setText(listUsernames.get(position));
    }



    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        ImageView detector;
        LinearLayout mainLayout,messageLayout;
        TextView message;
        ImageView picture,play,result;

    ProgressBar stripSound;
        TextView username,time;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cw_main);
            message = itemView.findViewById(R.id.tv_message);
            username = itemView.findViewById(R.id.tv_username);
            avatar =  itemView.findViewById(R.id.iv_user_avatar);
            detector = itemView.findViewById(R.id.iv_detector);
            picture =  itemView.findViewById(R.id.iv_picture);
           result =  itemView.findViewById(R.id.iv_result);
           play =  itemView.findViewById(R.id.iv_play);
           stripSound =  itemView.findViewById(R.id.pb_sound);
            mainLayout = itemView.findViewById(R.id.ll_main);
            messageLayout = itemView.findViewById(R.id.ll_message);
            time = itemView.findViewById(R.id.tv_time);
        }
    }


}
