package de.derb3rt.karoominimal

import android.app.Activity
import android.os.Bundle

/** A small on-device preview and setup page, not a replacement for Karoo navigation. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
