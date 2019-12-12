package jp.ac.ecc.se2a28.storedetails;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //コンテキスト
    Context context = this;

    //Log.d用
    private static final String TAG = "FavoriteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.textView1);
        TextView textView1 = findViewById(R.id.textView2);
        TextView textView2 = findViewById(R.id.textView3);
        TextView textView3 = findViewById(R.id.textView4);
        TextView textView4 = findViewById(R.id.textView5);
        TextView textView5 = findViewById(R.id.textView10);
        TextView textView6 = findViewById(R.id.textView11);
        TextView textView7 = findViewById(R.id.textView9);

        Database2 db = new Database2("stores");
//        db.get("HnSRrPY3aSNRc37Cwcp8", "name",textView);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "dinner budget",textView1);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "lunch budget",textView2);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "住所",textView3);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "genre",textView4);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "lunch budget",textView5);
//        db.get("HnSRrPY3aSNRc37Cwcp8", "dinner budget",textView6);

        textView.setText("天丼あさひ");
        textView1.setText("～1000");
        textView2.setText("～1000");
        textView3.setText("丼");
        textView4.setText("大阪市北区茶屋町6-2,水野ビル1F");
        textView5.setText("～1000");
        textView6.setText("～1000");
        textView7.setText("[全日] 10:30～11:00 LO(22:30)");




        Button button2 = findViewById(R.id.button4);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Test1.class);
                startActivity(intent);
            }
        });

        Button button3 = findViewById(R.id.button5);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Test2.class);
                startActivity(intent);
            }
        });

//        Button button4 = findViewById(R.id.button);
//        button4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, MapsActivity.class);
//                startActivity(intent);
//            }
//        });

        ImageButton imagebutton = findViewById(R.id.imageButton);
        imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
