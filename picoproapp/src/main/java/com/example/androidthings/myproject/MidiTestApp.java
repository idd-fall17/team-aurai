package com.example.androidthings.myproject;

import edu.berkeley.idd.utils.SerialMidi;

/**
 * Demo of the SerialMidi class
 * Created by bjoern on 9/12/17.
 */

public class MidiTestApp extends SimplePicoPro {
    SerialMidi serialMidi;
    int channel = 0;
    int velocity = 127; //0..127
    int timbre_value = 0;
    final int timbre_controller = 0x47;
    @Override
    public void setup() {
        uartInit(UART6,115200);
        serialMidi = new SerialMidi(UART6);
    }

    @Override
    public void loop() {

        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

        serialMidi.midi_note_on(channel,SerialMidi.MIDI_C4,velocity);
        delay(200);
        serialMidi.midi_note_off(channel,SerialMidi.MIDI_C4,127);
        delay(200);
        serialMidi.midi_note_on(channel,SerialMidi.MIDI_E4,127);
        delay(200);
        serialMidi.midi_note_off(channel,SerialMidi.MIDI_E4,127);
        delay(200);
        serialMidi.midi_note_on(channel,SerialMidi.MIDI_G4,127);
        delay(200);
        serialMidi.midi_note_off(channel,SerialMidi.MIDI_G4,127);
        delay(200);

        //
        timbre_value+=5;
        if(timbre_value>=127)
            timbre_value=0;
        serialMidi.midi_controller_change(channel,timbre_controller,timbre_value);

    }
}
