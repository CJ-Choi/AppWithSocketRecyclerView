package com.example.socket_test_0730

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter

    private var socket: Socket? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(mutableListOf())
        recyclerView.adapter = adapter

        connectSocket()
    }

    private fun connectSocket() {
        try {
            socket = IO.socket("http://10.0.2.2:3000")
            socket?.connect()
            Log.d(TAG, "Socket connected")

            socket?.on("initial_list") { args ->
                Log.d(TAG, "Received initial_list event with data: ${args[0]}")
                if (args[0] is JSONArray) {
                    val jsonArray = args[0] as JSONArray
                    val items = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        items.add(jsonArray.getString(i))
                    }
                    runOnUiThread {
                        adapter.updateItems(items)
                        Log.d(TAG, "RecyclerView updated with initial list")
                    }
                }
            }

            socket?.on("item_added") { args ->
                val newItem = args[0] as String
                Log.d(TAG, "Received item_added event with data: $newItem")
                runOnUiThread {
                    adapter.addItem(newItem)
                    Log.d(TAG, "RecyclerView item added: $newItem")
                }
            }

            socket?.on("item_deleted") { args ->
                val index = (args[0] as Int)
                Log.d(TAG, "Received item_deleted event with index: $index")
                runOnUiThread {
                    adapter.removeItem(index)
                    Log.d(TAG, "RecyclerView item removed at index: $index")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Socket connection error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket?.disconnect()
        Log.d(TAG, "Socket disconnected")
    }
}