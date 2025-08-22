package com.julkali.glauncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.databinding.ActivityGestureManagerBinding
import com.julkali.glauncher.fragments.GestureViewerFragment
import com.julkali.glauncher.io.database.GestureDBHandler

class GestureManagerActivity : FragmentActivity() {

    private lateinit var dbHandler: GestureDBHandler
    private lateinit var binding: ActivityGestureManagerBinding

    private var displayedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestureManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHandler = GestureDBHandler(this)
        loadSavedApps()

        // Wire up delete button
        binding.deleteLaunchEntryButton.setOnClickListener { deleteAppLaunchEntry(it) }
    }

    fun onNewButtonClicked(view: View) {
        val intent = Intent(this, SaveGestureActivity::class.java)
        startActivity(intent)
    }

    private fun loadSavedApps() {
        val savedAppEntries = dbHandler.readSavedGestures()
        if (savedAppEntries.isEmpty()) {
            displayNoGesturesMessage()
            return
        }
        hideNoGestureMessage()

        val entries = savedAppEntries.map {
            object {
                val id = it.id
                val appName = it.appName
                val gesture = it.gesture

                override fun toString(): String {
                    return appName
                }
            }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entries)
        binding.savedAppEntries.adapter = adapter
        binding.savedAppEntries.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "Please select something!", Toast.LENGTH_SHORT).show()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = entries[position]
                val fragment = supportFragmentManager.findFragmentById(R.id.gestureViewerFragment) as GestureViewerFragment
                fragment.display(
                    selected.appName,
                    selected.gesture
                )
                displayedId = selected.id
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedApps()
    }

    private fun displayNoGesturesMessage() {
        binding.savedAppEntries.visibility = View.GONE
        binding.deleteLaunchEntryButton.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .hide(supportFragmentManager.findFragmentById(R.id.gestureViewerFragment)!!)
            .commit()
        binding.txtNoGestures.visibility = View.VISIBLE
    }

    private fun hideNoGestureMessage() {
        binding.savedAppEntries.visibility = View.VISIBLE
        binding.deleteLaunchEntryButton.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .show(supportFragmentManager.findFragmentById(R.id.gestureViewerFragment)!!)
            .commit()
        binding.txtNoGestures.visibility = View.GONE
    }

    fun deleteAppLaunchEntry(view: View) {
        val lock = displayedId ?: return
        dbHandler.deleteAppLaunchEntryById(lock)
        loadSavedApps()
    }
}
