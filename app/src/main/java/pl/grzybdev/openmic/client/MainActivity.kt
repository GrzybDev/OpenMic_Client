package pl.grzybdev.openmic.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var button: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button = findViewById(R.id.submitBtn);
        button.setOnClickListener { connectToPC(); }
    }

    fun connectToPC() {
        button.isEnabled = false;
        button.text = getString(R.string.btn_connecting);
    }
}