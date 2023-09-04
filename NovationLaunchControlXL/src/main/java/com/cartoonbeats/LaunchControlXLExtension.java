package com.cartoonbeats;

import static java.lang.String.format;

import java.util.stream.IntStream;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.HardwareButton;
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
      final int numSends = 2;
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
      final int[] trackFocusButtonNote = { 41, 42, 43, 44, 57, 58, 59, 60};
      final int[] trackControlButtonNote = { 73, 74, 75, 76, 89, 90, 91, 92 };

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

         // Assign buttons to momentary sends (e.g. stab to reverb/stab to delay).
         // knob = hardwareSurface.createAbsoluteHardwareKnob(format("trackFocus__ch%d", channelIndex));
         // knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(launchControlMidiChannel, trackFocusButtonCC[channelIndex]));
         // knob.setBinding(track.sendBank().getItemAt(0).value());
         // this->bindButtonToSendformat("trackControl__ch%d", channelIndex)
         bindButtonToSend(
            format("trackFocus__ch%d", channelIndex),
            channelIndex,
            track,
            0,
            launchControlMidiChannel,
            trackFocusButtonNote[channelIndex]
         );
         bindButtonToSend(
            format("trackFControl__ch%d", channelIndex),
            channelIndex,
            track,
            1,
            launchControlMidiChannel,
            trackControlButtonNote[channelIndex]
         );
      });

      // TODO: navigate up/down tracks (1 or bankwise)
      // TODO: allow selecting focus track – e.g. shift + track select (will need a shift mode)
   }

   private void bindButtonToSend(String hardwareName, int channelIndex, Track track, int sendIndex, int midiChannel, int noteNumber)
   {
      final ControllerHost host = getHost();
      MidiIn midiIn = host.getMidiInPort(0);

      HardwareButton button = hardwareSurface.createHardwareButton(hardwareName);
      button.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(midiChannel, noteNumber));
      button.releasedAction().setActionMatcher(midiIn.createNoteOffActionMatcher(midiChannel, noteNumber));
      button.pressedAction().setBinding(host.createAction(
         () -> {
            track.sendBank().getItemAt(sendIndex).value().set(1.0);
         },
         () -> format("setSend_%d_Max", sendIndex))
      );
      button.releasedAction().setBinding(host.createAction(
         () -> {
            track.sendBank().getItemAt(sendIndex).value().set(0.0);
         },
         () -> format("setSend_%d_Min", sendIndex))
      );
   }

   @Override
   public void exit()
   {
   }

   @Override
   public void flush()
   {
      // TODO Show send state on track buttons.
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg)
   {
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data)
   {
   }

   // private Transport mTransport;
   private HardwareSurface hardwareSurface;
}
