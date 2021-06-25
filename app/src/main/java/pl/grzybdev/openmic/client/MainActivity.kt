package pl.grzybdev.openmic.client

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import pl.grzybdev.openmic.client.network.Packet
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
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

            outToServer.write(Packet.getHelloPacket());
            outToServer.flush();

            val connectPacket: String = inFromServer.readLine();
            val data = connectPacket.split(0.toChar());

            val recorded: AudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));

            recorded.startRecording();

            Thread {
                val bufSize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                val buf: ByteArray = ByteArray(bufSize);
                val addr = InetAddress.getByName(ip.text.toString());
                val sock = DatagramSocket();

                while (true) {
                    recorded.read(buf, 0, bufSize);
                    val packet = DatagramPacket(buf, bufSize, addr, Integer.parseInt(data[1]));
                    sock.send(packet);
                }
            }.start();
        }.start();
    }
}