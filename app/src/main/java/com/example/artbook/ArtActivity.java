package com.example.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.artbook.databinding.ActivityArtBinding;
import com.example.artbook.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {



    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher; // GALERİYE GİTMEK İÇİN
    ActivityResultLauncher<String> permissionLauncher; // İZİN İSTEMEK İÇİN
    Bitmap selectedImage;
    SQLiteDatabase database;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding tanımladım

        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        registerLauncher();

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            // new art olduğunu anla
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.select);

        } else {
            int artId = intent.getIntExtra("artId", 1);
            binding.button.setVisibility(View.INVISIBLE); // BUTTONU GİZLE

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)}); // id i stringe dönüştür
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    // BURADA RESİMİ GÖSTERMEYE ÇALIŞIYORUZ TEXTTEN FARKLI OLDUĞU İÇİN

                    byte [] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);

                }
                cursor.close();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void save(View view) {

        String name = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte [] byteArray = outputStream.toByteArray();

        try {

            //VERİTABANI ISLEMLERİ


            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");

           String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES(?, ?, ?, ?)";
           SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
           sqLiteStatement.bindString(1,name);
           sqLiteStatement.bindString(2,artistName);
           sqLiteStatement.bindString(3,year);
           sqLiteStatement.bindBlob(4,byteArray);
           sqLiteStatement.execute();


        } catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        // ÖNCEDEN BİRİKMİŞ ACTİVİTY VARSA ONLARI KAPAT
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public Bitmap makeSmallerImage (Bitmap image, int maximumSize){

        // BURADA KULLANICI RESİMİ YATAY VEYA DİKEY GİRERSE OTOMATİK OLARAK KIRPIYOR ONA GÖRE AYARLIYOR

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio =  (float) width / (float) height;
        if (bitmapRatio > 1) {
            //landscape image
            width = maximumSize;
            height = (int) (width / bitmapRatio);

        } else {
            //portrait image
            height = maximumSize;
            width = (int) (height * bitmapRatio);

        }

        return image.createScaledBitmap(image, width,height,true);

    }



    public void selectImage(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { // GALERİDEN İZİN İSTE

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //BURADA İZİN İSTE
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            }else {

                //BURADA İZİN İSTE
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }


            } else {   //GALERİ

                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery); //burada çağırdık galeriye gidecek


            }
        }


        private void registerLauncher() {

        // bunu onCreate altında çağırmamız lazım ki işlev görsün

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    // KULLANICI GALERİDEN SEÇİNCE

                   Intent intentFromResult = result.getData();
                   if(intentFromResult != null) {
                      Uri imageData = intentFromResult.getData();
                      // binding.imageView.setImageURI(imageData);
                       try {
                           // HATAYI DENE BURADAN
                           if(Build.VERSION.SDK_INT >= 28){
                               // SDK VERSİYONU 28 VE ÜSTÜYSE KULLAN

                               ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                               selectedImage  = ImageDecoder.decodeBitmap(source);
                               binding.imageView.setImageBitmap(selectedImage);

                           } else {
                               // SDK VERSİYONU 28 ALTINDAYSA
                               selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                               binding.imageView.setImageBitmap(selectedImage);
                           }




                       } catch (Exception e) {
                           //HATAYI YAKALA BURADAN
                           e.printStackTrace();

                       }
                   }
                }

            }
        });




        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result) {
                    // burada izin verildi

                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery); // burada çağırdık galeriye gidecek

                } else {
                    // izin verilmedi

                    Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        }

    }

