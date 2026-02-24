package com.example.personalfinancialmanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animate logo
        ImageView logo = findViewById(R.id.splashLogo);
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim);
        logo.startAnimation(logoAnim);

        // Delay navigation to Login until animation completes
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DURATION); // This MUST match your animation duration
    }
}
