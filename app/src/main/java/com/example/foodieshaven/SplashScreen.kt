package com.example.foodieshaven

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val fadeDuration = 2000L // Fade duration in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the ActionBar (header)
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash_screen)

        // Find the ImageView by its ID
        val logo: ImageView = findViewById(R.id.logo)

        // Load the animation (fade-in and scale)
        val fadeInScale: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale)

        // Start the animation on the logo
        logo.startAnimation(fadeInScale)

        // Play the splash screen sound with fade-in
        mediaPlayer = MediaPlayer.create(this, R.raw.splash_sound)
        mediaPlayer?.setVolume(0f, 0f) // Start with muted volume
        mediaPlayer?.start()

        // Start fade-in for the sound
        fadeInSound()

        // Delay for 2 seconds and then move to the main activity
        Handler().postDelayed({
            // Start fade-out for the sound
            fadeOutSound()

            // Transition to MainActivity after fade-out
            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                // Apply a fade-out transition between activities
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                // Stop and release the media player
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null

                // Close SplashActivity
                finish()
            }, fadeDuration)
        }, 2000) // Show splash screen for 2 seconds
    }

    private fun fadeInSound() {
        val steps = 20
        val stepDelay = fadeDuration / steps
        for (i in 1..steps) {
            val volume = i / steps.toFloat()
            Handler().postDelayed({
                mediaPlayer?.setVolume(volume, volume)
            }, i * stepDelay)
        }
    }

    private fun fadeOutSound() {
        val steps = 20
        val stepDelay = fadeDuration / steps
        for (i in 1..steps) {
            val volume = 1f - (i / steps.toFloat())
            Handler().postDelayed({
                mediaPlayer?.setVolume(volume, volume)
            }, i * stepDelay)
        }
    }
}
