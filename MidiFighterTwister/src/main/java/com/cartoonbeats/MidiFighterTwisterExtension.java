package com.cartoonbeats;

import static java.lang.String.format;

import java.util.stream.IntStream;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

public class MidiFighterTwisterExtension extends ControllerExtension
{

   // MIDI Fighter is on channel 1 (aka 0 zero-based)
   final int midiFighterMidiChannel = 0;

   // declare constants for the different hardware controls
   final int firstKnobCC = 0;
   final int row2Knob1CC = 8;

   // Keep a reference to the focused/active channel's remote controls so we can send out updates (feedback knob position).
   CursorRemoteControlsPage remoteControlsPage;

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
      hardwareSurface.setPhysicalSize(100.0, 100.0);

      initfocusedMainChannelControl(host, midiIn);
      initAuxChannelControl(host, midiIn);
   }

   // Attempt to control the active track's remote controls (aka macros) with the first 8 knobs.
   // Working, but I want to figure out showing current value (see flush()) before continuing with this.
   public void initfocusedMainChannelControl(ControllerHost host, MidiIn midiIn)
   {
      // Get a reference to the tracks we want to control.
      final int numSends = 0;
      final int numScenes = 1;
      Track track = host.createCursorTrack("CBRActiveTrack", "Active Track", numSends, numScenes, true);

      // Get the first page of remote controls (aka macros) for the track.
      // Future: get a named "Perform" page if available.
      final int maxParams = 8; // we'll have access to full page of 8 but only access knobs 1 and 5 (0/4)
      remoteControlsPage =  track.createCursorRemoteControlsPage(maxParams);

      IntStream.range(0,8).forEach(remoteControlIndex -> {
         // int paramIndex = 0;
         // Assign the knob to the first macro control.
         AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("ActiveChannel_knob_%d", remoteControlIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(midiFighterMidiChannel, row2Knob1CC + remoteControlIndex));
         remoteControlsPage.getParameter(remoteControlIndex).markInterested();
         knob.setBinding(remoteControlsPage.getParameter(remoteControlIndex).value());
      });

   }

   // Hard-code top 2 rows of knobs to 4 remote controls on aux 1 & 2 (effect tracks 1 & 2).
   public void initAuxChannelControl(ControllerHost host, MidiIn midiIn)
   {
      // Get a reference to the tracks we want to control – the first two effect tracks (aux returns).
      final int numTracks = 2;
      final int numSends = 2;
      final int numScenes = 1;
      TrackBank tracks = host.createEffectTrackBank(
         numTracks,
         numSends,
         numScenes
      );

      // Loop mapping top 2 rows to 4x aux performance controls.
      IntStream.range(0,2).forEach(auxChannelIndex -> {
         Track track = tracks.getItemAt(auxChannelIndex);

         // Get the first page of remote controls (aka macros) for the track.
         final int maxParams = 8;
         CursorRemoteControlsPage remoteControlsPage =  track.createCursorRemoteControlsPage(maxParams);

         // Map 4x knobs to track remote control 1-4.
         IntStream.range(0,4).forEach(knobIndex -> {
            AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("AUX_%d_KNOB_%d", auxChannelIndex, knobIndex));
            knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(midiFighterMidiChannel, (auxChannelIndex * 4) + (firstKnobCC + knobIndex)));
            knob.setBinding(remoteControlsPage.getParameter(knobIndex).value());
         });
      });

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
