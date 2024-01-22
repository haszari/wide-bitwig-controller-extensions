package com.cartoonbeats;

import static java.lang.String.format;

import java.util.stream.IntStream;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ClipLauncherSlot;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareLightVisualState;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.OnOffHardwareLight;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

public class TraktorKontrolF1Extension extends ControllerExtension
{
   protected TraktorKontrolF1Extension(final TraktorKontrolF1ExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();

      MidiIn midiIn = host.getMidiInPort(0);
      MidiOut midiOut = host.getMidiOutPort(0);

      hardwareSurface = host.createHardwareSurface();
      hardwareSurface.setPhysicalSize(100.0, 100.0);

      // Get a reference to the tracks we want to control.
      final int numTracks = 4;
      final int numSends = 0;
      final int numScenes = 4;
      TrackBank tracks = host.createMainTrackBank(
         numTracks,
         numSends,
         numScenes
      );
      // Show the highlight rect in session view so we know which clips we're pointing at.
      tracks.setShouldShowClipLauncherFeedback(true);

      // Kontrol F1 is on channel 13 (aka 12 zero-based)
      final int kontrolF1MidiChannel = 12;

      // declare constants for the different hardware controls
      final int knobCCCh1 = 2; // CC2 - CC5
      final int faderCCCh1 = 6; // CC6 - CC9
      final int gridNoteTopLeft = 36; // Ch1 36…39, Ch2 40…43, Ch3 44…47, Ch4 48…51

      // Loop over first 8 channels, assigning knob to first macro param and fader to level fader.
      IntStream.range(0,numTracks).forEach(channelIndex -> {
         Track track = tracks.getItemAt(channelIndex);

         // Get the first page of remote controls (aka macros) for the track.
         // Future: get a named "Perform" page if available.
         final int maxParams = 8; // we'll have access to full page of 8 but only access knobs 1 and 5 (0/4)
         CursorRemoteControlsPage remoteControlsPage =  track.createCursorRemoteControlsPage(maxParams);

         int paramIndex = 0;
         // Assign the knob to the first macro control.
         AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("KNOB__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, knobCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign the fader to the second macro control.
         paramIndex = 1;
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("FADER__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, faderCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign 4x4 grid buttons to clip launcher slots.
         final int channelGridNoteStart = channelIndex * numTracks;
         IntStream.range(0,numScenes).forEach(rowIndex -> {
            int slotIndex = channelGridNoteStart + rowIndex;
            int midiNote = gridNoteTopLeft + slotIndex;

            HardwareButton clipButton = hardwareSurface.createHardwareButton(format("CLIP_BUTTON_%d_%d", channelIndex, rowIndex));
            clipButton.pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(kontrolF1MidiChannel, midiNote));
            clipButton.setBounds(10 + channelIndex * 20, 10 + rowIndex * 20, 10, 10);

            ClipLauncherSlot sessionClip = track.clipLauncherSlotBank().getItemAt(rowIndex);
            sessionClip.isPlaying().markInterested();
            clipButton.pressedAction().setBinding(sessionClip.launchAction());

            OnOffHardwareLight light = hardwareSurface.createOnOffHardwareLight(format("CLIP_LED_%d_%d", channelIndex, rowIndex));
            // light.setBounds(0 + channelIndex * 20, 10, 10, 10);
            light.setOnColor(Color.whiteColor());
            light.setOffColor(Color.blackColor());
            light.setStateToVisualStateFunction(
               isOn -> isOn ? HardwareLightVisualState.createForColor(Color.whiteColor(), Color.blackColor())
               : HardwareLightVisualState.createForColor(Color.blackColor(), Color.blackColor()));
            light.isOn().setValueSupplier(sessionClip.isPlaying());
            clipButton.setBackgroundLight(light);
            light.isOn().onUpdateHardware(value -> {
               // Send note on/off to light up the button.
               midiOut.sendMidi(0x90 + kontrolF1MidiChannel, midiNote, value ? 127 : 0);
            });
         });

         // Map stop button row to stop whatever clip is playing in that channel.
         final int ch1StopButtonCC = 37;
         HardwareButton quantButton = hardwareSurface.createHardwareButton(format("STOP_BUTTON_%d", channelIndex));
         quantButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, ch1StopButtonCC + channelIndex, 127));
         quantButton.pressedAction().setBinding(track.stopAction());

      });

      // QUANT & CAPTURE buttons nav tracks left/right (page size 4).
      // This allows us to set left/right "deck".
      final int quantButtonCC = 13;
      HardwareButton quantButton = hardwareSurface.createHardwareButton(format("QUANT_BUTTON"));
      quantButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, quantButtonCC, 127));
      quantButton.pressedAction().setBinding(tracks.scrollPageBackwardsAction());
      final int captureButtonCC = 14;
      HardwareButton captureButton = hardwareSurface.createHardwareButton(format("CAPT_BUTTON"));
      captureButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, captureButtonCC, 127));
      captureButton.pressedAction().setBinding(tracks.scrollPageForwardsAction());
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
      hardwareSurface.updateHardware();
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
