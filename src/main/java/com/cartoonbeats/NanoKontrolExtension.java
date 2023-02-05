package com.cartoonbeats;

import static java.lang.String.format;

import java.util.stream.IntStream;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.Channel;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.Device;
import com.bitwig.extension.controller.api.DeviceBank;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;

public class NanoKontrolExtension extends ControllerExtension
{
   protected NanoKontrolExtension(final NanoKontrolExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();

      // Do I need this?
      // mTransport = host.createTransport();
      // host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi0(msg));
      // host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));

      // TODO: Perform your driver initialization here.
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

      // declare an enum of constants for the different buttons
      // final class NanoKontrol {
      final int knobCCScene1 = 14;
      final int faderCCScene1 = 2;
      // }

      // Loop over first 8 channels, assigning knob to first macro param and fader to level fader.
      IntStream.range(0, 8).forEach(channelIndex -> {
         Channel currentChannel = tracks.getItemAt(channelIndex);
         final int numDevices = 1;
         DeviceBank channelDevices = currentChannel.createDeviceBank(numDevices);
         // Get the first device (i.e. the main instrument plugin).
         // Future: handle midi or other "prefix" devices before instrument,
         // e.g. get first instrument plugin, or allow user to select device somehow.
         Device device = channelDevices.getDevice(0);
         // Get the first page of macro controls.
         // Future: get a named "Perform" page if available.
         CursorRemoteControlsPage remoteControlsPage = device.createCursorRemoteControlsPage(1);

         // Assign the knob to the first macro control.
         final int paramIndex = 0;
         AbsoluteHardwareKnob knob = hardwareSurface.createAbsoluteHardwareKnob(format("KNOB__ch%d_%d", channelIndex, paramIndex));
         final int midiChannel = 0;
         knob.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(midiChannel, knobCCScene1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign the fader to the channel volume.
         // TODO: if there is a `Level` param in macro page, bind that instead (so can have pre-fader fx).
         AbsoluteHardwareKnob fader = hardwareSurface.createAbsoluteHardwareKnob(format("FADER__ch%d", channelIndex));
         fader.setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(midiChannel, faderCCScene1 + channelIndex));
         fader.setBinding(currentChannel.volume().value());
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
      // // MMC Transport Controls:
      // if (data.equals("f07f7f0605f7"))
      //       mTransport.rewind();
      // else if (data.equals("f07f7f0604f7"))
      //       mTransport.fastForward();
      // else if (data.equals("f07f7f0601f7"))
      //       mTransport.stop();
      // else if (data.equals("f07f7f0602f7"))
      //       mTransport.play();
      // else if (data.equals("f07f7f0606f7"))
      //       mTransport.record();
   }

   private Transport mTransport;
   private HardwareSurface hardwareSurface;
}
