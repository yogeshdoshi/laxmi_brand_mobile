package com.e.laxmibrand.admin;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/*
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
*/
import com.e.laxmibrand.ImageFilePath;
import com.e.laxmibrand.R;
import com.e.laxmibrand.admin.product.ImageListAdapter;
import com.e.laxmibrand.beans.BaseResponse;
import com.e.laxmibrand.user.UserLogin;
import com.e.laxmibrand.utils.PermissionUtils;
import com.e.laxmibrand.utils.Utility;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.sangcomz.fishbun.define.Define;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminMarketing extends AppCompatActivity {
          Button uploadImgBTN, saveBTN;
         public static ArrayList<Uri> imgList;
        ImageListAdapter imageListAdapter;
        RecyclerView imgRV;
    ImageView logoutBTN,backPressed;
        private ProgressDialog pDialog;
Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_marketing);

            uploadImgBTN = findViewById(R.id.uploadImgBTN);
            imgRV = findViewById(R.id.imgRV);
            saveBTN = findViewById(R.id.saveBTN);
        logoutBTN = findViewById(R.id.logoutBTN);
        backPressed = findViewById(R.id.backPressed);
        context =AdminMarketing.this;
        logoutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), UserLogin.class));

            }
        });

        backPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

            imgRV.setLayoutManager(new GridLayoutManager(AdminMarketing.this, 3, RecyclerView.VERTICAL, false));
            imgList = new ArrayList<>();
            imgList.clear();
            imageListAdapter = new ImageListAdapter(AdminMarketing.this, imgList, true);
            imgRV.setAdapter(imageListAdapter);

            uploadImgBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (PermissionUtils.requestPermission(AdminMarketing.this, 1, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Log.i("Permission 1 : ","GRANTED.....");
                        FishBun.with(AdminMarketing.this)
                                .setImageAdapter(new GlideAdapter())
                                .setMaxCount(5)
                                .setSelectedImages(imgList)
                                .setMinCount(1)
                                .startAlbum();
                        Log.i("Permission 2 : ","GRANTED.....22222");

                    }
             /*       Intent intent = new Intent(AdminMarketing.this, AlbumSelectActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 5);
                    startActivityForResult(intent, Constants.REQUEST_CODE);*/
                }
            });

            saveBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (imgList != null && imgList.size() > 0) {
                        if (Utility.isOnline(AdminMarketing.this)) {
                            saveMarketingAPI();

                        } else {
                            Utility.noInternetError(context);
                        }
                    } else {
                        Toast.makeText(AdminMarketing.this, "Please upload images.", Toast.LENGTH_SHORT).show();
                    }
//                Toast.makeText(getActivity(), "Images Saved successfully.", Toast.LENGTH_SHORT).show();
                }
            });

        }

    private void saveMarketingAPI() {
        final Dialog dialog = Utility.showProgress(context);
        ArrayList<MultipartBody.Part> images = prepareFilePart("image[]");
        Call<BaseResponse> addCategory = Utility.retroInterface().addAdvertisementImages(images);
        addCategory.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                Utility.dismissDialog(dialog);
                try {
                    if (response.code() == 200) {
                        if (response.body().getStatus().equalsIgnoreCase("success")) {
                            Toast.makeText(context, response.body().getResponseData().getMessage(), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(context, response.body().getResponseData().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "othtn200"+response.code(), Toast.LENGTH_SHORT).show();

//                        Toast.makeText(context, "Wrong Email or Password.", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    Toast.makeText(context,e.getMessage(),Toast.LENGTH_LONG).show();

                    Utility.somethingWentWrong(context);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Utility.dismissDialog(dialog);
                Toast.makeText(context,"exception here"+t.getMessage(),Toast.LENGTH_LONG).show();

                Utility.somethingWentWrong(context);
            }
        });


    }


    /*public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            //The array list has the image paths of the selected images
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
        }*/

        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == Define.ALBUM_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    ArrayList<Uri> list = data.getParcelableArrayListExtra(Define.INTENT_PATH);
                    imgList = list;
                    imageListAdapter = new ImageListAdapter(AdminMarketing.this, imgList, false);
                    imgRV.setAdapter(imageListAdapter);
                    imageListAdapter.notifyDataSetChanged();
                    if (imgList != null && imgList.size() > 0) {
                        saveBTN.setVisibility(View.VISIBLE);
                    } else {
                        saveBTN.setVisibility(View.GONE);
                    }
                    if (imgList != null && imgList.size() == 5) {
                        uploadImgBTN.setVisibility(View.GONE);
                    } else {
                        uploadImgBTN.setVisibility(View.VISIBLE);
                    }

                    // you can get an image path(ArrayList<Uri>) on 0.6.2 and later
                }
            }
        }



        public void updateProgress(int val, String title, String msg) {
            pDialog.setTitle(title);
            pDialog.setMessage(msg);
            pDialog.setProgress(val);
        }

        public void showProgress(String str) {
            try {
                pDialog.setCancelable(false);
                pDialog.setTitle("Please wait");
//            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            pDialog.setMax(100); // Progress Dialog Max Value
                pDialog.setMessage(str);
                if (pDialog.isShowing())
                    pDialog.dismiss();
                pDialog.show();
            } catch (Exception e) {

            }
        }

        public void hideProgress() {
            try {
                if (pDialog.isShowing())
                    pDialog.dismiss();
            } catch (Exception e) {

            }
        }

        private ArrayList<MultipartBody.Part> prepareFilePart(String partName) {

            ArrayList<MultipartBody.Part> imageFileArray = new ArrayList<>();

            if (imgList != null) {
                for (int i = 0; i < imgList.size(); i++) {

                    File imgFile = new File(ImageFilePath.getPath(AdminMarketing.this, imgList.get(i)));
                    try {
                        float fileSizeKB = Float.parseFloat(String.valueOf(imgFile.length() / 1024));

                        if (fileSizeKB > 500) {
                            int quality = (int) (50000 / fileSizeKB);
                            imgFile = new Compressor(AdminMarketing.this).setQuality(quality).compressToFile(imgFile);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);

                    // MultipartBody.Part is used to send also the actual file name
                    imageFileArray.add(MultipartBody.Part.createFormData(partName, imgFile.getName(), requestFile));

                }
            }

            return imageFileArray;
        }
    }

