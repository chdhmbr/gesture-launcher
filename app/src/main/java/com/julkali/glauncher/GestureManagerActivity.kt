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

    // Data class for spinner entries
    data class GestureEntry(val id: String, val appName: String, val gesture: String) {
        override fun toString(): String = appName
    }

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
            GestureEntry(it.id, it.appName, it.gesture)
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
                val fragment = supportFragmentManager.findFragmentById(R.id.gestureViewerFragment)
                if (fragment is GestureViewerFragment) {
                    fragment.display(selected.appName, selected.gesture)
                }
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
        supportFragmentManager.findFragmentById(R.id.gestureViewerFragment)?.let { fragment ->
            supportFragmentManager.beginTransaction().hide(fragment).commit()
        }
        binding.txtNoGestures.visibility = View.VISIBLE
    }

    private fun hideNoGestureMessage() {
        binding.savedAppEntries.visibility = View.VISIBLE
        binding.deleteLaunchEntryButton.visibility = View.VISIBLE
        supportFragmentManager.findFragmentById(R.id.gestureViewerFragment)?.let { fragment ->
            supportFragmentManager.beginTransaction().show(fragment).commit()
        }
        binding.txtNoGestures.visibility = View.GONE
    }

    fun deleteAppLaunchEntry(view: View) {
        val lock = displayedId ?: return
        dbHandler.deleteAppLaunchEntryById(lock)
        loadSavedApps()
    }
}
