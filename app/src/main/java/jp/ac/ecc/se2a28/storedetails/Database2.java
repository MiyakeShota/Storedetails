package jp.ac.ecc.se2a28.storedetails;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//データベースはコレクション>ドキュメント>フィールドが基本

public class Database2 {

    //Cloud Firestoreデータベースを表し、すべてのCloud Firestore操作のエントリポイント
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //CollectionReferenceとQueryはほとんど同じもので、<"複数のドキュメントへの参照">
    private CollectionReference colRef;
    //DocumentReferenceは一つのドキュメントへの参照
    private DocumentReference docRef;
    ////コレクションに対してクエリを作成します。
    //例:CollectionReference citiesRef = db.collection("cities");
    //Query query = citiesRef.whereEqualTo("state", "CA");
    Query query;
    //使っているクラス名
    private static final String TAG = "Database2";
    //返り値を格納する変数
    private String str = "まだ値は入っていない";
    //Task<DocumentSnapshot>
    //Task<QuerySnapshot>の方は複数のドキュメントのデータを持つ
    private Task<DocumentSnapshot> task_temp;

//    private Task<DocumentSnapshot> top10_task;
//    Task<QuerySnapshot> top10_task;

    //rank用のカウント変数
    private int rankcount;
    //rankの写真用のカウント変数
    private int rankphotocount = -1;

    private int rankcount2;

    private int counter;

    private final Handler handler = new Handler();

    private final static Object lock = new Object();

    //コレクションを指定
    Database2(String colPath) {
        colRef = db.collection(colPath);
    }

    //単一のドキュメントを作成するというよりは単一のフィールドを作成するメソッドになってしまっている

    //使う型のテストをは完了
    //refrence型はよくわからない

    //現在時刻のセット
    //"timestamp", FieldValue.serverTimestamp()

    //ドキュメント名取得
    //document.geiId();

    //自動生成された ID を持つドキュメントを作成する方のメソッド
    //単一のフィールドを作成または上書きするメソッド
    //SetOptions.merge()によって新しいデータを既存のドキュメントに結合する
    //リストは完全に上書きになるので注意
    public void set(String fieldId, Object fieldVal) {
        //フィールドのidと値を結合
        Map<String, Object> hashMap = new HashMap();
        hashMap.put(fieldId, fieldVal);

        //値をデータベースにセット
        colRef.document().set(hashMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshotが正常に書き込まれました！");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ドキュメントの書き込みエラー", e);
            }
        });
    }

    //ドキュメント名を指定して作成する方のメソッド
    //単一のフィールドを作成または上書きするメソッド
    //SetOptions.merge()によって新しいデータを既存のドキュメントに結合する
    //リストは完全に上書きになるので注意
    public void set(String docPath, String fieldId, Object fieldVal) {
        //ドキュメント名を指定 存在しない場合は新規で生成される
        DocumentReference docRef = colRef.document(docPath);

        //フィールドのidと値を結合
        Map<String, Object> hashMap = new HashMap();
        hashMap.put(fieldId, fieldVal);

        //値をデータベースにセット
        docRef.set(hashMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshotが正常に書き込まれました！");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "ドキュメントの書き込みエラー", e);
            }
        });
    }

    //set()と同じでフィールドが存在しなかったら生成する
    //必要あるかは不明
//    public void update(String docPath, String fieldId, Object fieldVal) {
//        //ドキュメント名を指定 存在しない場合は新規で生成される
//        DocumentReference docRef = colRef.document(docPath);
//
//        docRef.update(fieldId, fieldVal).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "DocumentSnapshotが正常に更新されました！");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.w(TAG, "ドキュメントの更新エラー", e);
//            }
//        });
//    }

    //フィールドの配列に要素を追加するメソッド
    //追加されるのはまだ存在しない要素のみ
    public void arrayUnion(String docPath, String fieldId, String fieldVal) {
        DocumentReference docRef = colRef.document(docPath);

        // 配列フィールドに新しい領域を原子的に追加します。
        docRef.update(fieldId, FieldValue.arrayUnion(fieldVal));
    }

    //フィールドの配列の指定した要素を削除するメソッド
    //指定した各要素のすべてのインスタンスを削除します
    //同じ値が複数ある場合、全部消える
    public void arrayRemove(String docPath, String fieldId, String fieldVal) {
        DocumentReference docRef = colRef.document(docPath);

        // 配列フィールドから領域を原子的に削除します。
        //原子性:一連の処理は、全体として実行されるか、実行されないか、どちらかであることが保証されることを指す。
        docRef.update(fieldId, FieldValue.arrayRemove(fieldVal));
    }

    //いいね数/フォロー数/フォロワー数用のメソッド
    //負の値入れるとその分マイナスされると思われる
    //毎回配列取り出して数えて値を入れたほうがいいのか？
    public void increment(String docPath, String fieldId, int val) {
        docRef = colRef.document(docPath);

        docRef.update(fieldId, FieldValue.increment(val));
    }

    //コレクション内のドキュメントを全て取得するメソッド
    //引数なしでも呼び出せてしまう
    public void getAll(final TextView... textView) {
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            textView[i++].setText(document.getId());
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            break;
                        }
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //いいね数/フォロー数/フォロワー数が多い順にn件取り出すメソッド
    //ASC・・・昇順
    //DESC・・・降順
    //number型じゃないとString型の数字が優先的に表示されるため注意
    public void ranking(String fieldId, int limit, final TextView... textView) {
        colRef.orderBy(fieldId, Query.Direction.DESCENDING).limit(limit).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            textView[i++].setText(document.getId());
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            break;
                        }
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //コレクション内から条件を満たす全ての<"ドキュメント">を取得するメソッド
    //whereLessThan:～より小さい
    //whereGreaterThan:～より大きい
    //whereArrayContains:配列値に基づいてフィルタ
    //fieldValの値が違うと反応しない
    public void getWhere(String fieldId, Object fieldVal, final TextView... textView) {
        colRef.whereEqualTo(fieldId, fieldVal)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                try {
                                    textView[i++].setText(document.getId());
                                } catch (ArrayIndexOutOfBoundsException aioobe) {
                                    break;
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //単体のフィールドを取り出すメソッド
    //単一のドキュメントの内容を取得すしてから単一のフィールドを取り出している
    public void get(final String docPath, final String fieldPath, final TextView textView) {
        //DocumentReferenceは一つのドキュメントへの参照
        DocumentReference docRef = colRef.document(docPath);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        textView.setText(document.get(fieldPath).toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //単体のフィールドを取り出すメソッド
    //Listからひとつを取り出すよう
    public void get(final String docPath, final String fieldPath, final int num, final TextView textView) {
        //DocumentReferenceは一つのドキュメントへの参照
        DocumentReference docRef = colRef.document(docPath);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Object object = document.get(fieldPath);
                        List<Object> list = (ArrayList<Object>)object;
                        textView.setText(list.get(num).toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //単体のフィールドを取り出すメソッド
    //Mapからひとつを取り出すよう
    //おそらく今回は使わない
    public void get(final String docPath, final String fieldPath, final String key, final TextView textView) {
        //DocumentReferenceは一つのドキュメントへの参照
        DocumentReference docRef = colRef.document(docPath);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Object object = document.get(fieldPath);
                        Map<String, Object> map = (HashMap<String, Object>)object;
                        textView.setText(map.get(key).toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //値を返り値で返すメソッド
    //ドキュメントとフィールドを指定
    public Object get(String docPath, String fieldPath) {
        //メインスレッド
        //返り値
        Object obj = null;

        docRef = colRef.document(docPath);

        //別タスクで非同期処理
        new Thread(new Runnable() {
            @Override
            public void run() {
                //別スレッド
                task_temp = docRef.get();
            }
        }).start();

        //メインスレッド
        //get()が終わるまで待つ
        synchronized (this) {
            try {
                while(task_temp == null) {
                    wait(1000);
                }
                //? なぜtask==nullだけではダメなのか
                while(!task_temp.isSuccessful()) {
                    wait(500);
                }
            } catch (InterruptedException ie) {
                //あるスレッドが待ち状態、休止状態、または占有されているとき、アクティビティーの前かその間のいずれかにそのスレッドで割り込みが発生した場合にスローされます
                Log.d(TAG, "待ち状態のスレッドに割り込み発生");
            }

            if (task_temp.isSuccessful()) {
                DocumentSnapshot document = task_temp.getResult();
                if (document.exists()) {
                    Log.d(TAG, "ドキュメントデータ " + document.getData());
                    obj = document.get(fieldPath);
                } else {
                    Log.d(TAG, "そのような文書はありません");
                }
            } else {
                Log.d(TAG, "taskの処理に失敗 ", task_temp.getException());
            }
            //2回目以降のためnullにしておく
            task_temp = null;
        }
        return obj;
    }

    //ドキュメントを削除するメソッド
    public void delete(String docPath) {
        docRef = colRef.document(docPath);

        docRef.delete();
    }

    //フィールドを削除するメソッド
    public void delete(String docPath, String fieldId) {
        docRef = colRef.document(docPath);

        // Remove the 'capital' field from the document
        Map<String, Object> updates = new HashMap<>();
        updates.put("capital", FieldValue.delete());

        docRef.update(updates);
    }

    //順番
    //1:写真          店名、テキスト
    //2:アイコン      性 名
//    public void top10(final Context context, final List<TextView> rankstorenamelist, final List<TextView> ranktextlist,
//                      final List<ImageView> rankimglist, final List<TextView> rankfirstnamelist, final List<TextView> ranklastnamelist, final List<ImageView> iconlist) {
//        //いいねが多い順で10件問い合わせる
//        colRef.orderBy("good", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (final QueryDocumentSnapshot document : task.getResult()) {
////                        Log.d(TAG, document.getId() + " => " + document.getData());
//
////                        Log.d(TAG, "店名:" + storename);
//                        rankstorenamelist.get(rankcount).setText(document.get("storename").toString());
//
//                        //テキスト
////                        Log.d(TAG, "テキスト:" + text);
//                        ranktextlist.get(rankcount).setText(document.get("text").toString());
//
//                        //写真
//                        List<String> photolist = (List<String>) document.get("photolist");
//                        for(int i = 0; i < 2; i++) {
//                            try {
//                                rankphotocount++;
//                                GlideApp.with(context).load(photolist.get(i)).into(rankimglist.get(rankphotocount));
//                            } catch (IndexOutOfBoundsException aioob) { }
//                        }
//
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                top10_task = db.collection("users").document(document.get("poster").toString()).get();
//                            }
//                        }).start();
//
//                        synchronized (this) {
//                            try {
//                                while (top10_task == null) {
//                                    Thread.sleep(100);
//                                    wait(200);
//                                }
//                                //? なぜtask==nullだけではダメなのか
//                                while (!top10_task.isSuccessful()) {
//                                    wait(200);
//                                }
//                            } catch (InterruptedException ie) {
//                                //あるスレッドが待ち状態、休止状態、または占有されているとき、アクティビティーの前かその間のいずれかにそのスレッドで割り込みが発生した場合にスローされます
//                                Log.d(TAG, "待ち状態のスレッドに割り込み発生");
//                            }
//
//                            DocumentSnapshot d = top10_task.getResult();
//                            if (d.exists()) {
////                                Log.d(TAG, "ドキュメントデータ " + d.getData());
//
//                                //性
////                                Log.d(TAG, "性: " + d.get("firstname"));
//                                rankfirstnamelist.get(rankcount).setText(d.get("firstname").toString());
//
//                                //名
////                                Log.d(TAG, "名: " + d.get("lastname"));
//                                ranklastnamelist.get(rankcount).setText(d.get("lastname").toString());
//
//                                //アイコン
////                                Log.d(TAG, "アイコン: " + d.get("icon"));
//                                GlideApp.with(context).load(d.get("icon")).circleCrop().into(iconlist.get(rankcount));
//                                rankcount++;
//                            } else {
//                                Log.d(TAG, "そのような文書はありません");
//                            }
//                            top10_task = null;
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "ドキュメントの取得エラー： ", task.getException());
//                }
//            }
//        });
//    }

//    final Context context, final List<TextView> rankstorenamelist, final List<TextView> ranktextlist,
//    final List<ImageView> rankimglist, final List<TextView> rankfirstnamelist, final List<TextView> ranklastnamelist, final List<ImageView> iconlist

//    public void top10() {
//        //いいねが多い順で10件問い合わせる
//        colRef.orderBy("good", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (final QueryDocumentSnapshot document : task.getResult()) {
//                        counter++;
//
////                        Log.d(TAG, document.getId() + " => " + document.getData());
//
//                        Log.d(TAG, "店名:" + document.get("storename").toString());
////                        rankstorenamelist.get(rankcount).setText(document.get("storename").toString());
//
//                        //テキスト
////                        Log.d(TAG, "テキスト:" + text);
////                        ranktextlist.get(rankcount).setText(document.get("text").toString());
//
//                        //写真
//                        List<String> photolist = (List<String>) document.get("photolist");
//                        for(int i = 0; i < 2; i++) {
//                            try {
//                                rankphotocount++;
////                                GlideApp.with(context).load(photolist.get(i)).into(rankimglist.get(rankphotocount));
//                            } catch (IndexOutOfBoundsException aioob) { }
//                        }
//
//                        db.collection("users").document(document.get("poster").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    DocumentSnapshot documentSnapshot = task.getResult();
//                                    if (documentSnapshot.exists()) {
//                                        Log.d(TAG, "性: " + documentSnapshot.get("lastname"));
//                                    }
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    Log.d(TAG, "ドキュメントの取得エラー： ", task.getException());
//                }
//            }
//        });
//    }

//    final Context context, final List<TextView> rankstorenamelist, final List<TextView> ranktextlist,
//                      final List<ImageView> rankimglist, final List<TextView> rankfirstnamelist, final List<TextView> ranklastnamelist, final List<ImageView> iconlist

//    public void top10() {
//        //いいねが多い順で10件問い合わせる
//        colRef.orderBy("good", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (final QueryDocumentSnapshot document : task.getResult()) {
////                        Log.d(TAG, document.getId() + " => " + document.getData());
//
////                        Log.d(TAG, "店名:" + storename);
////                        rankstorenamelist.get(rankcount).setText(document.get("storename").toString());
//
//                        //テキスト
////                        Log.d(TAG, "テキスト:" + text);
////                        ranktextlist.get(rankcount).setText(document.get("text").toString());
//
//                        //写真
//                        List<String> photolist = (List<String>) document.get("photolist");
//                        for(int i = 0; i < 2; i++) {
//                            try {
//                                rankphotocount++;
////                                GlideApp.with(context).load(photolist.get(i)).into(rankimglist.get(rankphotocount));
//                            } catch (IndexOutOfBoundsException aioob) { }
//                        }
//
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                top10_task = db.collection("users").document(document.get("poster").toString()).get();
//                            }
//                        }).start();
//
//                        try {
//                            while (top10_task == null) {
//                                Thread.sleep(100);
//                            }
//                            //? なぜtask==nullだけではダメなのか
//                            while (!top10_task.isSuccessful()) {
//                                Thread.sleep(100);
//                            }
//                        } catch (InterruptedException ie) {
//                            //あるスレッドが待ち状態、休止状態、または占有されているとき、アクティビティーの前かその間のいずれかにそのスレッドで割り込みが発生した場合にスローされます
//                            Log.d(TAG, "待ち状態のスレッドに割り込み発生");
//                        }
//
//                        DocumentSnapshot d = top10_task.getResult();
//                        if (d.exists()) {
////                                Log.d(TAG, "ドキュメントデータ " + d.getData());
//
//                            //性
////                                Log.d(TAG, "性: " + d.get("firstname"));
////                            rankfirstnamelist.get(rankcount).setText(d.get("firstname").toString());
//
//                            //名
//                                Log.d(TAG, "名: " + d.get("lastname"));
////                            ranklastnamelist.get(rankcount).setText(d.get("lastname").toString());
//
//                            //アイコン
////                                Log.d(TAG, "アイコン: " + d.get("icon"));
////                            GlideApp.with(context).load(d.get("icon")).circleCrop().into(iconlist.get(rankcount));
//                            rankcount++;
//                        } else {
//                            Log.d(TAG, "そのような文書はありません");
//                        }
//                        top10_task = null;
//                    }
//                } else {
//                    Log.d(TAG, "ドキュメントの取得エラー： ", task.getException());
//                }
//            }
//        });
//    }

    public void top10(final Context context, final List<TextView> rankstorenamelist, final List<TextView> ranktextlist,
                      final List<ImageView> rankimglist, final List<TextView> rankfirstnamelist, final List<TextView> ranklastnamelist, final List<ImageView> iconlist) {
        //いいねが多い順で10件問い合わせる
        colRef.orderBy("good", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d(TAG, document.getId() + " => " + document.getData());

//                        Log.d(TAG, "店名:" + storename);
                        rankstorenamelist.get(rankcount).setText(document.get("storename").toString());

                        //テキスト
//                        Log.d(TAG, "テキスト:" + text);
                        ranktextlist.get(rankcount).setText(document.get("text").toString());

                        //写真
                        List<String> photolist = (List<String>) document.get("photolist");
                        for(int i = 0; i < 2; i++) {
                            try {
                                rankphotocount++;
                                GlideApp.with(context).load(photolist.get(i)).into(rankimglist.get(rankphotocount));
                            } catch (IndexOutOfBoundsException aioob) { }
                        }

                        //??? マルチスレッド　並行処理　非同期処理
                        //??? synchronizedで出来ないか
                        //別スレッドに何番目の処理か分かるようにする変数
                        final int num = rankcount;

                        new Thread(new Runnable() {
                            Task<DocumentSnapshot> top10_task;
                            @Override
                            public void run() {

                                Log.d(TAG, String.valueOf(num));
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //順番バラバラになる
                                        top10_task = db.collection("users").document(document.get("poster").toString()).get();
                                    }
                                }).start();

                                try {
                                    while (top10_task == null) {
                                        Thread.sleep(100);
                                    }
                                    //? なぜtask==nullだけではダメなのか
                                    while (!top10_task.isSuccessful()) {
                                        Thread.sleep(100);
                                    }
                                } catch (InterruptedException ie) {
                                    //あるスレッドが待ち状態、休止状態、または占有されているとき、アクティビティーの前かその間のいずれかにそのスレッドで割り込みが発生した場合にスローされます
                                    Log.d(TAG, "待ち状態のスレッドに割り込み発生");
                                }

                                final DocumentSnapshot d = top10_task.getResult();
                                if (d.exists()) {
                                    // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, String.valueOf(num) + d.get("lastname").toString());
                                            rankfirstnamelist.get(num).setText(d.get("firstname").toString());
                                            ranklastnamelist.get(num).setText(d.get("lastname").toString());
                                            GlideApp.with(context).load(d.get("icon")).circleCrop().into(iconlist.get(num));
                                        }
                                    });
                                }
                            }
                        }).start();



//                        DocumentSnapshot d = top10_task.getResult();
//                        if (d.exists()) {
////                                Log.d(TAG, "ドキュメントデータ " + d.getData());
//
//                            //性
////                                Log.d(TAG, "性: " + d.get("firstname"));
////                            rankfirstnamelist.get(rankcount).setText(d.get("firstname").toString());
//
//                            //名
//                            Log.d(TAG, "名: " + d.get("lastname"));
////                            ranklastnamelist.get(rankcount).setText(d.get("lastname").toString());
//
//                            //アイコン
////                                Log.d(TAG, "アイコン: " + d.get("icon"));
////                            GlideApp.with(context).load(d.get("icon")).circleCrop().into(iconlist.get(rankcount));
                            rankcount++;
//                        } else {
//                            Log.d(TAG, "そのような文書はありません");
//                        }
//                        top10_task = null;
                    }
                } else {
                    Log.d(TAG, "ドキュメントの取得エラー： ", task.getException());
                }
            }
        });
    }
}
