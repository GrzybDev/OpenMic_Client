package pl.grzybdev.openmic.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

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

        val ip: EditText = findViewById(R.id.ipAddress);
        val port: EditText = findViewById(R.id.port);

        val thread = Thread {
            val socket: Socket = Socket(ip.text.toString(), Integer.parseInt(port.text.toString()));
            val outToServer: DataOutputStream = DataOutputStream(socket.getOutputStream());
            val inFromServer: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()));
        }.start();
    }
}