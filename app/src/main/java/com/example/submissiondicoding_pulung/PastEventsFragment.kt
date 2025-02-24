package com.example.submissiondicoding_pulung

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class PastEventsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyTextView: TextView
    private lateinit var adapter: EventPastAdapter
    private val eventList = mutableListOf<EventPast>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_past_events, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.recyclerView1)
        emptyTextView = view.findViewById(R.id.emptyTextView) // Tambahan untuk teks jika data kosong

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = EventPastAdapter(eventList)
        recyclerView.adapter = adapter

        FetchEventsTask().execute()

        return view
    }

    private inner class FetchEventsTask : AsyncTask<Void, Void, List<EventPast>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.GONE
        }

        override fun doInBackground(vararg params: Void?): List<EventPast> {
            val response = APIClient.api("events?active=2", null, "GET")
            return response?.let { parseEventList(it) } ?: emptyList()
        }

        override fun onPostExecute(result: List<EventPast>) {
            progressBar.visibility = View.GONE

            if (result.isNotEmpty()) {
                eventList.clear()
                eventList.addAll(result)
                adapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                emptyTextView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                emptyTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun parseEventList(response: String): List<EventPast> {
        val eventList = mutableListOf<EventPast>()
        try {
            val jsonResponse = if (response.startsWith("HTTP")) {
                response.substringAfter(": ").trim()
            } else {
                response
            }

            val jsonObject = JSONObject(jsonResponse)
            if (!jsonObject.getBoolean("error")) {
                val eventsArray: JSONArray = jsonObject.getJSONArray("listEvents")

                for (i in 0 until eventsArray.length()) {
                    val eventObj = eventsArray.getJSONObject(i)
                    val event = EventPast(
                        id = eventObj.getInt("id"),
                        name = eventObj.getString("name"),
                        imageLogo = eventObj.optString("imageLogo", ""),
                        beginTime = eventObj.optString("beginTime", "Tidak ada data"),
                        description = eventObj.optString("description", "Tidak ada deskripsi"),
                        link = eventObj.optString("link", "#"),
                        ownerName = eventObj.optString("ownerName", "Tidak diketahui"),
                        quota = eventObj.optInt("quota", 0),
                        registrant = eventObj.optInt("registrant", 0)
                    )
                    eventList.add(event)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("API_ERROR", "JSON Parsing Error: ${e.message}")
        }
        return eventList
    }
}
