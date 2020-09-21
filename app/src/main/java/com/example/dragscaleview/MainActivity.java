package com.example.dragscaleview;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.dragscaleview.view.DragRectView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout drawLayout;
    private DragRectView dragRectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawLayout = findViewById(R.id.draw_view);
        dragRectView = findViewById(R.id.drag_rect_view);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 200);
        lp.setMargins(50, 50, 0, 0);
        dragRectView.setLayoutParams(lp);
        dragRectView.setClickable(true);
        dragRectView.setSelected(true);
        dragRectView.setMyTouchListener(new DragRectView.OnMyTouchListener() {
            @Override
            public void onClick() {
                dragRectView.setSelected(true);
            }
        });

        drawLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dragRectView.setSelected(false);
            }
        });


//        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DragRectView dragRectView = new DragRectView(MainActivity.this);
//                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 200);
//                lp.setMargins(50, 50, 0, 0);
//                dragRectView.setLayoutParams(lp);
//                dragRectView.setClickable(true);
//                dragRectView.setSelected(true);
//                drawLayout.addView(dragRectView);
//
//            }
//        });


    }
}