package jp.ac.ecc.se2a28.storedetails;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class Storage2 {
    //Cloud Storageのインスタンス生成
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    //アプリからストレージ参照を作成します
    private final StorageReference storageRef = storage.getReference();

    //フォルダまでの参照
    private StorageReference folderRef;

    //
    Bitmap bmp;

//    public Storage2(String folderPath) {
//        //子参照を作成する
//        //folderRefが「folderPath」を指すようになりました
//        folderRef = storageRef.child(folderPath);
//    }

    public String set(String folderPath , String upimgPath) {
        //アップロードする画像のパスを指定する
        //“/strage/emulated/0/” は内部ストレージを意味しています
        //data/data/app_name/
        ///storage/emulated/0/Download/150837603608136697178_(2215).jpg
        ///storage/self/primary/Download/150837603608136697178_(2215).jpg
        //Download以下はselfと同じ名前で対応する
        Uri file = Uri.fromFile(new File(upimgPath));

        //????????? userid+ファイル名にすべき
        final StorageReference imgRef = storageRef.child(folderPath + "/" + file.getLastPathSegment());
        //putFile()がおそらく非同期処理
        UploadTask uploadTask = imgRef.putFile(file);

        //ダウンロード URL を取得
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imgRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Database2 db2 = new Database2("test");
                    db2.set("test", "test", downloadUri.toString());
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

        return folderPath + file.getLastPathSegment();
    }



    //storageの階層パスから取得するメソッド
    //今回は使わない
//    public void get(String path, ImageView _imageView) {
//        final ImageView imageView = _imageView;
//
//        StorageReference imgRef = storageRef.child(path);
//
//        //https://firebase.google.com/docs/storage/android/upload-files?hl=ja
//        //アプリがクラッシュする可能性がある
//        final long ONE_MEGABYTE = 1024 * 1024;
//        imgRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                //Android SDKの”Bitmapクラス”と”画像フォーマットのbitmap”は別物
//                // 「img_1」のデータが返されます。必要に応じてこれを使用してください
//                //byte配列(jpeg,png等) → Bitmap
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                imageView.setImageBitmap(bitmap);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                //エラーを処理する
//            }
//        });
//    }

    //storageの階層を指定してuriを取得するメソッド
    //https://akira-watson.com/android/httpurlconnection-get.html
    //https://codeday.me/jp/qa/20190408/578931.html
    public void get(final ImageView imageView) {
        storageRef.child("test/GettyImages-522585140.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                new AsyncTask<Void, Void, Void>() {
                    // 非同期処理
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            InputStream in = new URL(uri.toString()).openStream();
                            bmp = BitmapFactory.decodeStream(in);
                        } catch (Exception e) {
                            // log error
                        }
                        return null;
                    }

                    // 非同期処理が終了後、結果をメインスレッドに返す
                    @Override
                    protected void onPostExecute(Void result) {
                        if (bmp != null)
                            imageView.setImageBitmap(bmp);
                    }

                }.execute();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }
}
