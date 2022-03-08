package com.sunnyweather.android.ui.place

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.R
import kotlinx.android.synthetic.main.fragment_place.*

class PlaceFragment : Fragment() {
    val viewMode by lazy { ViewModelProvider(this).get(PlaceViewMode::class.java) }

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // requireActivity() returns the host activity
        requireActivity().lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event.targetState == Lifecycle.State.CREATED) {
                    // perform logic
                    val layoutManager = LinearLayoutManager(activity)
                    recyclerView.layoutManager = layoutManager
                    adapter = PlaceAdapter(this@PlaceFragment, viewMode.placeList)
                    recyclerView.adapter = adapter
                    searchPlaceEdit.addTextChangedListener {
                        val content = it.toString()
                        if (content.isNotEmpty()) {
                            viewMode.searchPlaces(content)
                        } else {
                            recyclerView.visibility = View.GONE
                            bgImageView.visibility = View.VISIBLE
                            viewMode.placeList.clear()
                            adapter.notifyDataSetChanged()
                        }
                    }

                    viewMode.placeLiveData.observe(this@PlaceFragment) {
                        val places = it.getOrNull()
                        if (places != null) {
                            recyclerView.visibility = View.VISIBLE
                            bgImageView.visibility = View.GONE
                            viewMode.placeList.clear()
                            viewMode.placeList.addAll(places)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(context, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                            it.exceptionOrNull()?.printStackTrace()
                        }
                    }

                    // remove observer
                    lifecycle.removeObserver(this)
                }
            }
        })
    }
}
