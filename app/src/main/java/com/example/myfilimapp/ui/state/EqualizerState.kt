package com.example.myfilimapp.ui.state

import com.example.myfilimapp.model.request.EqPreset

data class EqualizerState(
    val gains       : List<Float>  = List(5) { 0f },  // per-band gains (-12..+12 dB)
    val bassDb      : Float        = 0f,               // bass knob  (-12..+12 dB)
    val trebleDb    : Float        = 0f,               // treble knob(-12..+12 dB)
    val activePreset: EqPreset?    = EqPreset.FLAT,
    val isEnabled   : Boolean      = true
)
