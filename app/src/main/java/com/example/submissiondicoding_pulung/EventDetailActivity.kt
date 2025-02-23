package com.example.submissiondicoding_pulung

import EventDetail
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class EventDetailActivity : AppCompatActivity() {
    private lateinit var imageLogo: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventOwner: TextView
    private lateinit var eventTime: TextView
    private lateinit var eventQuota: TextView
    private lateinit var eventDesc: TextView
    private lateinit var btnOpenLink: Button
    private var EventId: Int = 0
    private var eventLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        imageLogo = findViewById(R.id.imageLogo)
        eventName = findViewById(R.id.eventName)
        eventOwner = findViewById(R.id.eventOwner)
        eventTime = findViewById(R.id.eventTime)
        eventQuota = findViewById(R.id.eventQuota)
        eventDesc = findViewById(R.id.eventDesc)
        btnOpenLink = findViewById(R.id.btnOpenLink)


        EventId = intent.getIntExtra("eventId", 0)
        Log.d("EventDetailActivity", "Event ID: $EventId")


        if (EventId != 0) {
            fetchEventDetail(EventId)
        } else {
            Log.e("EventDetailActivity", "Event ID tidak valid!")
        }

        btnOpenLink.setOnClickListener {
            eventLink?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
            }
        }
    }

    private fun fetchEventDetail(eventId: Int) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = APIClient.api("events/$eventId", null, "GET")
                Log.d("API_RESPONSE", "Raw Response: $response")

                if (response.isNullOrBlank()) {
                    Log.e("API_ERROR", "Response kosong!")
                    return@launch
                }

                val event = parseEventDetail(response)
                Log.d("apimodel", "$event")

                withContext(Dispatchers.Main) {
                    if (event != null) {
                        updateUI(event)
                    } else {
                        Log.e("API_ERROR", "Event tidak ditemukan!")
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fetch event error: ${e.message}")
            }
        }
    }


    private fun updateUI(event: EventDetail) {
        eventName.text = event.name
        eventOwner.setText("Penyelenggara: ${event.ownerName}")
        eventTime.text = "Waktu: ${event.beginTime}"
        eventQuota.text = "Sisa Kuota: ${event.quota - event.registrants}"
        eventDesc.text = event.description
        eventLink = event.link


        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadBitmap(event.imageLogo)
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    imageLogo.setImageBitmap(bitmap)
                } else {
                    Log.e("IMAGE_ERROR", "Gagal memuat gambar!")
                }
            }
        }
    }

    private fun parseEventDetail(response: String): EventDetail? {
        return try {
            val jsonResponse = JSONObject(response)

            if (!jsonResponse.has("event")) {
                Log.e("API_ERROR", "Key 'event' tidak ditemukan di JSON!")
                return null
            }

            val eventObj = jsonResponse.getJSONObject("event")

            val event = EventDetail(
                id = eventObj.getInt("id"),
                name = eventObj.getString("name"),
                summary = eventObj.optString("summary", ""),
                description = htmlToPlainText(eventObj.optString("description", "")),
                imageLogo = eventObj.optString("imageLogo", ""),
                mediaCover = eventObj.optString("mediaCover", ""),
                category = eventObj.optString("category", ""),
                ownerName = eventObj.optString("ownerName", ""),
                cityName = eventObj.optString("cityName", ""),
                quota = eventObj.optInt("quota", 0),
                registrants = eventObj.optInt("registrants", 0),
                beginTime = eventObj.optString("beginTime", ""),
                endTime = eventObj.optString("endTime", ""),
                link = eventObj.optString("link", "")
            )

            Log.d("API_PARSE", "Event berhasil diparsing: $event")

            event
        } catch (e: Exception) {
            Log.e("API_ERROR", "JSON Parsing Error: ${e.message}")
            null
        }


    catch (e: Exception) {
            Log.e("API_ERROR", "JSON Parsing Error: ${e.message}")
            null
        }
    }



    private fun htmlToPlainText(html: String): String {
        val spanned: Spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        return spanned.toString().trim()
    }

    private fun loadBitmap(url: String?): Bitmap? {
        return try {
            if (!url.isNullOrBlank()) {
                BitmapFactory.decodeStream(URL(url).openStream())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("IMAGE_ERROR", "Error loading image: ${e.message}")
            null
        }
    }
}
