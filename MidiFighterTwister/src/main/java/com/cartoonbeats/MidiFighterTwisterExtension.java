package com.cartoonbeats;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.TrackBank;

public class MidiFighterTwisterExtension extends ControllerExtension
{
   protected MidiFighterTwisterExtension(final MidiFighterTwisterExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();

      // TODO: Perform your driver initialization here.
      MidiIn midiIn = host.getMidiInPort(0);

      hardwareSurface = host.createHardwareSurface();
      // hardwareSurface.setPhysicalSize(100.0, 100.0);

      // Get a reference to the tracks we want to control – the first two effect tracks (aux returns).
      final int numTracks = 2;
      final int numSends = 0;
      final int numScenes = 0;
      TrackBank tracks = host.createEffectTrackBank(
         numTracks,
         numSends,
         numScenes
      );

      // MIDI Fighter is on channel 1 (aka 0 zero-based)
      final int kontrolF1MidiChannel = 0;


   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      // getHost().showPopupNotification("Hello World Exited");
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg)
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data)
   {
   }

   // private Transport mTransport;
   private HardwareSurface hardwareSurface;
}
