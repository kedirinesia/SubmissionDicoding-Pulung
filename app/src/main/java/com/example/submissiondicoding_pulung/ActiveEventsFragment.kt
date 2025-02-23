package com.example.submissiondicoding_pulung

import EventActiveAdapter
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

class ActiveEventsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyMessage: TextView
    private lateinit var adapter: EventActiveAdapter
    private val eventList = mutableListOf<EventActive>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_active_events, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = EventActiveAdapter(eventList)
        recyclerView.adapter = adapter


        FetchEventsTask().execute()

        return view
    }

    private inner class FetchEventsTask : AsyncTask<Void, Void, List<EventActive>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            tvEmptyMessage.visibility = View.GONE
        }

        override fun doInBackground(vararg params: Void?): List<EventActive> {
            val response = APIClient.api("events?active=1", null, "GET")

            return response?.let {
                parseEventList(it)
            } ?: emptyList()
        }

        override fun onPostExecute(result: List<EventActive>) {
            progressBar.visibility = View.GONE
            recyclerView.visibility = if (result.isNotEmpty()) View.VISIBLE else View.GONE
            tvEmptyMessage.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE

            if (result.isNotEmpty()) {
                eventList.clear()
                eventList.addAll(result)
                adapter.notifyDataSetChanged()
                Log.d("API_SUCCESS", "Events Loaded: ${result.size}")
            } else {
                Log.w("API_SUCCESS", "No events found!")
            }
        }
    }

    private fun parseEventList(response: String): List<EventActive> {
        val eventList = mutableListOf<EventActive>()
        try {
            val jsonResponse = response.substringAfter(": ").trim()
            val jsonObject = JSONObject(jsonResponse)
            if (!jsonObject.getBoolean("error")) {
                val eventsArray: JSONArray = jsonObject.getJSONArray("listEvents")

                for (i in 0 until eventsArray.length()) {
                    val eventObj = eventsArray.getJSONObject(i)
                    val event = EventActive(
                        id = eventObj.getInt("id"),
                        name = eventObj.getString("name"),
                        summary = eventObj.getString("summary"),
                        imageLogo = eventObj.getString("imageLogo")
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
