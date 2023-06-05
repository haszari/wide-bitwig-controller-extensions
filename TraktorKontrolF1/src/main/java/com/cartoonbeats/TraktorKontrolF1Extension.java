package com.cartoonbeats;

import static java.lang.String.format;

import java.util.stream.IntStream;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.Channel;
import com.bitwig.extension.controller.api.ClipLauncherSlotBank;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.CursorTrack;
import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.SceneBank;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;

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

      // TODO: Perform your driver initialization here.
      MidiIn midiIn = host.getMidiInPort(0);

      hardwareSurface = host.createHardwareSurface();
      hardwareSurface.setPhysicalSize(100.0, 100.0);

      // Get a reference to the tracks we want to control.
      final int numTracks = 4;
      final int numSends = 0;
      final int numScenes = 1;
      TrackBank tracks = host.createMainTrackBank(
         numTracks,
         numSends,
         numScenes
      );

      // Kontrol F1 is on channel 13 (aka 12 zero-based)
      final int kontrolF1MidiChannel = 12;

      // declare constants for the different hardware controls
      final int knobCCCh1 = 2; // CC2 - CC5
      final int faderCCCh1 = 6; // CC6 - CC9

      // Loop over first 8 channels, assigning knob to first macro param and fader to level fader.
      IntStream.range(0,4).forEach(channelIndex -> {
         Channel currentChannel = tracks.getItemAt(channelIndex);
         final int numDevices = 1;
         DeviceBank channelDevices = currentChannel.createDeviceBank(numDevices);
         // Get the first device (i.e. the main instrument plugin).
         // Future: handle midi or other "prefix" devices before instrument,
         // e.g. get first instrument plugin, or allow user to select device somehow.
         Device device = channelDevices.getDevice(0);

         // Get the first page of macro controls.
         // Future: get a named "Perform" page if available.
         final int maxParams = 8; // we'll have access to full page of 8 but only access knobs 1 and 5 (0/4)
         // final String customName = "KontrolF1PerformPage";
         // final String filter = "perf"; // Attempted to get access to `Perform` page but it didn't work
         CursorRemoteControlsPage remoteControlsPage = device.createCursorRemoteControlsPage(maxParams);

         int paramIndex = 0;
         // Assign the knob to the first macro control.
         AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("KNOB__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, knobCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign the knob to the fifth macro control (first in bottom row).
         paramIndex = 4;
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("FADER__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, faderCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());


      });

      // Highlight which scene will be triggered by `SYNC` button.
      SceneBank scenes = tracks.sceneBank();
      scenes.setIndication(true);

      // Highlight which tracks are focused for performance params (and in future - clip launcher).
      Track track = tracks.getItemAt(0);
      ClipLauncherSlotBank clipLauncherSlotBank = track.clipLauncherSlotBank();
      clipLauncherSlotBank.setIndication(true);

      // sync button triggers scene!
      final int syncButtonCC = 12;
      HardwareButton syncButton = hardwareSurface.createHardwareButton(format("SYNC_BUTTON"));
      syncButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, syncButtonCC, 127));
      syncButton.pressedAction().setBinding(scenes.getScene(0).launchAction());

      // CAPTURE & TYPE buttons nav scene up/down.
      final int captureButtonCC = 14;
      HardwareButton captureButton = hardwareSurface.createHardwareButton(format("CAPT_BUTTON"));
      captureButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, captureButtonCC, 127));
      captureButton.pressedAction().setBinding(scenes.scrollBackwardsAction());
      final int typeButtonCC = 16;
      HardwareButton typeButton = hardwareSurface.createHardwareButton(format("TYPE_BUTTON"));
      typeButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, typeButtonCC, 127));
      typeButton.pressedAction().setBinding(scenes.scrollForwardsAction());

      // SIZE & BROWSE buttons nav tracks left/right (page size 4).
      final int sizeButtonCC = 17;
      HardwareButton sizeButton = hardwareSurface.createHardwareButton(format("SIZE_BUTTON"));
      sizeButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, sizeButtonCC, 127));
      sizeButton.pressedAction().setBinding(tracks.scrollPageBackwardsAction());
      final int browseButtonCC = 18;
      HardwareButton browseButton = hardwareSurface.createHardwareButton(format("BROWSE_BUTTON"));
      browseButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, browseButtonCC, 127));
      browseButton.pressedAction().setBinding(tracks.scrollPageForwardsAction());

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
