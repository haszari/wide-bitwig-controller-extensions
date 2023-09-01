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

public class LaunchControlXLExtension extends ControllerExtension
{
   protected LaunchControlXLExtension(final LaunchControlXLExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();

      MidiIn midiIn = host.getMidiInPort(0);

      hardwareSurface = host.createHardwareSurface();
      hardwareSurface.setPhysicalSize(100.0, 100.0);

      // Get a reference to the tracks we want to control.
      final int numTracks = 8;
      final int numSends = 0;
      final int numScenes = 1;
      TrackBank tracks = host.createMainTrackBank(
         numTracks,
         numSends,
         numScenes
      );

      // Kontrol F1 is on channel 9 in Protokol (aka 8 zero-based)
      final int launchControlMidiChannel = 8;

      // Declare constants for the different hardware controls.
      final int sendACCCh1 = 13; // CC13-20
      final int sendBCCCh1 = 29; // CC29-36
      final int panCCCh1 = 49; // CC49-56
      final int faderCCCh1 = 77; // CC77-84

      // Map 8 channels of control.
      IntStream.range(0,8).forEach(channelIndex -> {
         Track track = tracks.getItemAt(channelIndex);

         // Get the first page of remote controls (aka macros) for the track.
         // Future: get a named "Perform" page if available.
         final int maxParams = 8; // we'll have access to full page of 8 but only access knobs 1 and 5 (0/4)
         CursorRemoteControlsPage remoteControlsPage =  track.createCursorRemoteControlsPage(maxParams);

         int paramIndex = 0;
         // Assign Send A knob to the first macro control.
         AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("SENDA__ch%d", channelIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(launchControlMidiChannel, sendACCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign Send B knob to the second macro control.
         paramIndex++;
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("SENDB__ch%d", channelIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(launchControlMidiChannel, sendBCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign pan knob to the second macro control.
         paramIndex++;
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("PAN__ch%d", channelIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(launchControlMidiChannel, panCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign the fader to the second macro control.
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("FADER__ch%d", channelIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(launchControlMidiChannel, faderCCCh1 + channelIndex));
         knob.setBinding(track.volume());
      });

      // TODO: navigate up/down tracks (1 or bankwise)
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
