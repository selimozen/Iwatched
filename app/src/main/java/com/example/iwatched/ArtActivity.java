package com.example.iwatched;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import com.example.iwatched.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ArtActivity<için> extends AppCompatActivity {
    private ActivityArtBinding binding;
    //Geleriye gitmek için
    ActivityResultLauncher<Intent>activityResultLauncher;
    //İzni istemek için
    ActivityResultLauncher<String>permissionLauncher;
    Bitmap selectedImage;
    //Birden fazla yerde kullanacağımız için, SQL database'imizi burada tanımlıyoruz.
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        //Database'imizi açıyoruz.
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            binding.artName.setText("");
            binding.artType.setText("");
            binding.duration.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.select);


        }else{
            int artId = intent.getIntExtra("artId", 1);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[]{String.valueOf(artId)});
                int artNameix = cursor.getColumnIndex("artname");
                int typeNameix = cursor.getColumnIndex("typename");
                int durationix = cursor.getColumnIndex("duration");
                int imageix = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.artName.setText(cursor.getString(artNameix));
                    binding.artType.setText(cursor.getString(typeNameix));
                    binding.duration.setText(cursor.getString(durationix));

                    byte[] bytes = cursor.getBlob(imageix);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save(View view){
        String name = binding.artName.getText().toString();
        String type = binding.artType.getText().toString();
        String durationS = binding.duration.getText().toString();
        int duration = Integer.parseInt(durationS);

        Bitmap smallImage = makeSmallerImage(selectedImage, 200);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 75, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{

            //Tablomuzu açıyoruz.
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, typename VARCHAR, duration INT, image BLOB)");
            //Binding (bağlama) işlemlerini yapıyoruz.
            String sqlS = "INSERT INTO arts (artname, typename, duration, image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlS);

            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, type);
            sqLiteStatement.bindString(3, durationS);
            sqLiteStatement.bindBlob(4, byteArray);

            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        //Bundan önce açtığımız tüm aktiviteleri kapatmak için aşağıdaki kodu kullanıyoruz.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        //Galeriden aldığımız fotoğrafı tekrar boyutlandıracağımız bir fonskiyon yazıyoruz.
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if(bitmapRatio > 1){
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }else{
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
        return image.createScaledBitmap(image, width, height,true);
    }

    public void selectImage(View view){
        //Burada uzun bir kod yazıyoruz. Bu kod bloğu, izin verildimi verilmedi mi bunu kontrol ediyor.
        // Yani uygulamaya birdaha girildiğinde daha önceden izin verildimi gibisinden?
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view, "Permission Needed!", Snackbar.LENGTH_INDEFINITE).setAction("Give The Fooking Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();
            } else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else{
            //Bu kod'un anlamı, ben URI'a gitcem, resmi seçip kullancam. Ama sadece intenti yazdık. İşimiz yeni başlıyor:)
            //1. İzni nasıl isteyeceğiz, 2. galeriye giidp izni isteyip ne yapacağız.
            Intent intenttogallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intenttogallery);
        }
    }
    private void registerLauncher(){
        //Galeriye gitmek için gerekli launcher'ı yazıyoruz.
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        Uri imageData = intentFromResult.getData();
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                //Uri'ını aldığımız fotoğrafı bitmape çeviyoruz. Kaynağı resme çeviyoruz.
                                ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);

                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        //Yukarıda kullandığımız launcher kodlarını burada kaydetmemiz lazım.
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //izin aldıktan sonra galeriye gidip fotoğrafı seçecek kodu yazıyoruz.
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else{
                    Toast.makeText(ArtActivity.this,"Permission Needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}