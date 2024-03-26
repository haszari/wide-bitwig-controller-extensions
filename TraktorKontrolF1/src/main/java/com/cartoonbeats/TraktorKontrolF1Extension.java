package com.cartoonbeats;

import static java.lang.String.format;

import java.util.function.Supplier;
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
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MidiOut;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;
import com.bitwig.extension.controller.api.SceneBank;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

// Map from named colours to MIDI message and hue/HSB value.
// From Native Instruments Controller Editor manual.
final class F1ColourInfo {
   public int hue;
   public float saturation;
   public float brightness;
   public int midiMessageBase;

   public F1ColourInfo(int hue, int midiMessageBase) {
      this.hue = hue;
      this.saturation = 1.0f;
      this.brightness = 0.5f;
      this.midiMessageBase = midiMessageBase;
   }

   public F1ColourInfo(int hue, float saturation, float brightness, int midiMessageBase) {
      this.hue = hue;
      this.saturation = saturation;
      this.brightness = brightness;
      this.midiMessageBase = midiMessageBase;
   }

   public int getDim() {
      return midiMessageBase;
   }

   public int getBright() {
      return midiMessageBase + 2;
   }

   public static F1ColourInfo getClosest(Color color, ControllerHost host) {
      F1ColourInfo closest = F1ColourInfo.BLACK;
      int r = color.getRed255();
      int g = color.getGreen255();
      int b = color.getBlue255();

      float[] hsb = java.awt.Color.RGBtoHSB(r,g,b, null);
      int hue = (int)Math.round(hsb[0] * 360);
      int sat = (int)Math.round(hsb[1] * 100);
      int bright = (int)Math.round(hsb[1] * 100);

      host.println(String.format("Clip colour is (r%03d g%03d b%03d) aka (h%03d s%03d b%03d)", r,g,b, hue,sat, bright));

      if ( sat < 10 && bright < 10 ) {
         host.println(String.format("Black %d %d", sat, bright));
         return F1ColourInfo.BLACK;
      }

      int hueDelta = 999;
      for (F1ColourInfo colour : ALL_COLOURS) {
         int hued = (int)Math.abs(hue - colour.hue);
         if ( hued < hueDelta ) {
            hueDelta = hued;
            closest = colour;
         }
      }
      return closest;
   }

   public static final F1ColourInfo BLACK = new F1ColourInfo(0, 0.0f, 0.0f, 0);
   public static final F1ColourInfo WHITE  = new F1ColourInfo(0, 1.0f, 1.0f, 68);

   public static final F1ColourInfo RED = new F1ColourInfo(0, 4);
   public static final F1ColourInfo ORANGE = new F1ColourInfo(15, 8);
   public static final F1ColourInfo LIGHTORANGE = new F1ColourInfo(35, 12);
   public static final F1ColourInfo WARMYELLOW = new F1ColourInfo(50, 16);

   public static final F1ColourInfo YELLOW = new F1ColourInfo(60, 20);
   public static final F1ColourInfo LIME = new F1ColourInfo(86, 24);
   public static final F1ColourInfo GREEN = new F1ColourInfo(120, 28);
   public static final F1ColourInfo MINT = new F1ColourInfo(159, 32);

   public static final F1ColourInfo CYAN = new F1ColourInfo(180, 36);
   public static final F1ColourInfo TURQUOISE = new F1ColourInfo(192,40);
   public static final F1ColourInfo BLUE = new F1ColourInfo(229, 44);
   public static final F1ColourInfo PLUM = new F1ColourInfo(248, 48);

   public static final F1ColourInfo VIOLET = new F1ColourInfo(269, 52);
   public static final F1ColourInfo PURPLE = new F1ColourInfo(294, 56);
   public static final F1ColourInfo MAGENTA = new F1ColourInfo(300, 60);
   public static final F1ColourInfo FUCHSIA = new F1ColourInfo(328, 64);

   // All colourful colours, no black/white. Used for matching by hue.
   public static final F1ColourInfo ALL_COLOURS[] = {
      F1ColourInfo.RED,
      F1ColourInfo.ORANGE,
      F1ColourInfo.LIGHTORANGE,
      F1ColourInfo.WARMYELLOW,

      F1ColourInfo.YELLOW,
      F1ColourInfo.LIME,
      F1ColourInfo.GREEN,
      F1ColourInfo.MINT,

      F1ColourInfo.CYAN,
      F1ColourInfo.TURQUOISE,
      F1ColourInfo.BLUE,
      F1ColourInfo.PLUM,

      F1ColourInfo.VIOLET,
      F1ColourInfo.PURPLE,
      F1ColourInfo.MAGENTA,
      F1ColourInfo.FUCHSIA,
   };

};

final class ClipPadState extends InternalHardwareLightState {

   // We have a logical state, the colours are config or channel dependent.
   public static final int EMPTY = 0;
   public static final int CLIP = 1;
   public static final int TRIGGERED = 2;
   public static final int PLAYING = 3;
   public static final int STOPPING = 4;

   int state = EMPTY;
   Color clipColour = Color.blackColor();

   public ClipPadState(int state) {
      this.state = state;
   }

   public static ClipPadState empty() {
      return new ClipPadState(EMPTY);
   }

   public static ClipPadState clip() {
      return new ClipPadState(CLIP);
   }

   public static ClipPadState triggered() {
      return new ClipPadState(TRIGGERED);
   }
   public static ClipPadState stopping() {
      return new ClipPadState(STOPPING);
   }

   public static ClipPadState playing() {
      return new ClipPadState(PLAYING);
   }

   public int getState() {
      return state;
   }

   public void setColor(Color color) {
      clipColour = color;
   }

   public Color getColor() {
      return clipColour;
   }

   // I think this is also used and required by Bitwig.
   // Not sure how important it is to me.
   // I might get into trouble since my logical state is not the same as colour.
   @Override
   public boolean equals(final Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final ClipPadState other = (ClipPadState) obj;
      return state == other.state;
   }

   // This is purely used to display the light in the fake simulated hardware in
   // Bitwig.
   @Override
   public HardwareLightVisualState getVisualState() {
      // We'll just return a dummy value.
      return HardwareLightVisualState.createForColor(Color.blackColor());

   }

   public static void updateHardware(final ClipPadState state, final MidiOut midiOut, final int channel, final int note, final ControllerHost host) {
      int colourIndex = 0;

      F1ColourInfo clipPadColour = F1ColourInfo.getClosest(state.getColor(), host);

      switch (state.getState()) {
         case CLIP:
            colourIndex = clipPadColour.getDim();
            break;
         case PLAYING:
            colourIndex = clipPadColour.getBright();
            break;
         case TRIGGERED:
            colourIndex = F1ColourInfo.WHITE.getBright();
            break;
         case STOPPING:
            colourIndex = F1ColourInfo.WHITE.getDim();
            break;
         case EMPTY:
         default:
            colourIndex = F1ColourInfo.BLACK.getDim();
            break;
      }
      midiOut.sendMidi(0x90 + channel, note, colourIndex);
   }

}

// Maps a clip state to a hardware light state.
class ClipPadStateSupplier implements Supplier<ClipPadState> {

   private final ClipLauncherSlot sessionClip;

   public ClipPadStateSupplier(ClipLauncherSlot sessionClip) {
      this.sessionClip = sessionClip;
   }

   @Override
   public ClipPadState get() {
      ClipPadState state = ClipPadState.empty();

      // Triggered & stopping override playing state.
      if (sessionClip.isPlaybackQueued().get())
         state =  ClipPadState.triggered();
      else if (sessionClip.isStopQueued().get())
         state = ClipPadState.stopping();

      else if (sessionClip.isPlaying().get()) {
         state.setColor(sessionClip.color().get());
         state = ClipPadState.playing();
      }
      else if (sessionClip.hasContent().get()) {
         state = ClipPadState.clip();
         state.setColor(sessionClip.color().get());
      }


      return state;
   }

}

public class TraktorKontrolF1Extension extends ControllerExtension {
   protected TraktorKontrolF1Extension(final TraktorKontrolF1ExtensionDefinition definition,
         final ControllerHost host) {
      super(definition, host);
   }

   @Override
   public void init() {
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
            numScenes);
      // Show the highlight rect in session view so we know which clips we're pointing
      // at.
      tracks.setShouldShowClipLauncherFeedback(true);

      // Kontrol F1 is on channel 13 (aka 12 zero-based)
      final int kontrolF1MidiChannel = 12;

      // declare constants for the different hardware controls
      final int knobCCCh1 = 2; // CC2 - CC5
      final int faderCCCh1 = 6; // CC6 - CC9
      final int gridNoteTopLeft = 36; // Ch1 36…39, Ch2 40…43, Ch3 44…47, Ch4 48…51

      // Loop over first 8 channels, assigning knob to first macro param and fader to
      // level fader.
      IntStream.range(0, numTracks).forEach(channelIndex -> {
         Track track = tracks.getItemAt(channelIndex);

         // Get the first page of remote controls (aka macros) for the track.
         // Future: get a named "Perform" page if available.
         final int maxParams = 8; // we'll have access to full page of 8 but only access knobs 1 and 5 (0/4)
         CursorRemoteControlsPage remoteControlsPage = track.createCursorRemoteControlsPage(maxParams);

         int paramIndex = 0;
         // Assign the knob to the first macro control.
         AbsoluteHardwareKnob knob = hardwareSurface
               .createAbsoluteHardwareKnob(format("KNOB__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(
               midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, knobCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign the fader to the second macro control.
         paramIndex = 1;
         knob = hardwareSurface.createAbsoluteHardwareKnob(format("FADER__ch%d_%d", channelIndex, paramIndex));
         knob.setAdjustValueMatcher(
               midiIn.createAbsoluteCCValueMatcher(kontrolF1MidiChannel, faderCCCh1 + channelIndex));
         knob.setBinding(remoteControlsPage.getParameter(paramIndex).value());

         // Assign 4x4 grid buttons to clip launcher slots.
         final int channelGridNoteStart = channelIndex * numTracks;
         IntStream.range(0, numScenes).forEach(rowIndex -> {
            int slotIndex = channelGridNoteStart + rowIndex;
            int midiNote = gridNoteTopLeft + slotIndex;

            HardwareButton clipButton = hardwareSurface
                  .createHardwareButton(format("CLIP_BUTTON_%d_%d", channelIndex, rowIndex));
            clipButton.pressedAction()
                  .setActionMatcher(midiIn.createNoteOnActionMatcher(kontrolF1MidiChannel, midiNote));
            clipButton.setBounds(10 + channelIndex * 20, 10 + rowIndex * 20, 10, 10);

            ClipLauncherSlot sessionClip = track.clipLauncherSlotBank().getItemAt(rowIndex);
            sessionClip.isPlaying().markInterested();
            sessionClip.hasContent().markInterested();
            sessionClip.isPlaybackQueued().markInterested();
            sessionClip.isStopQueued().markInterested();
            sessionClip.color().markInterested();
            clipButton.pressedAction().setBinding(sessionClip.launchAction());

            MultiStateHardwareLight light = hardwareSurface
                  .createMultiStateHardwareLight(format("CLIP_LED_%d_%d", channelIndex, rowIndex));
            clipButton.setBackgroundLight(light);
            light.state().setValueSupplier(new ClipPadStateSupplier(sessionClip));
            light.state().onUpdateHardware(state -> {
               ClipPadState.updateHardware((ClipPadState)state, midiOut, kontrolF1MidiChannel, midiNote, host);
            });
         });

         // Map stop button row to stop whatever clip is playing in that channel.
         final int ch1StopButtonCC = 37;
         HardwareButton quantButton = hardwareSurface.createHardwareButton(format("STOP_BUTTON_%d", channelIndex));
         quantButton.pressedAction().setActionMatcher(
               midiIn.createCCActionMatcher(kontrolF1MidiChannel, ch1StopButtonCC + channelIndex, 127));
         quantButton.pressedAction().setBinding(track.stopAction());

      });

      // Navigate up/down & left/right 4x4 grid pages.
      // SYNC QUANT left right
      // CAPTURE TYPE up down
      final int syncButtonCC = 12;
      HardwareButton syncButton = hardwareSurface.createHardwareButton(format("SYNC_BUTTON"));
      syncButton.pressedAction()
            .setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, syncButtonCC, 127));
      syncButton.pressedAction().setBinding(tracks.scrollPageBackwardsAction());
      final int captureButtonCC = 14;
      HardwareButton captureButton = hardwareSurface.createHardwareButton(format("CAPT_BUTTON"));
      captureButton.pressedAction()
            .setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, captureButtonCC, 127));
      captureButton.pressedAction().setBinding(tracks.scrollPageForwardsAction());

      SceneBank scenes = tracks.sceneBank();
      final int quantButtonCC = 13;
      HardwareButton quantButton = hardwareSurface.createHardwareButton(format("QUANT_BUTTON"));
      quantButton.pressedAction()
            .setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, quantButtonCC, 127));
      quantButton.pressedAction().setBinding(scenes.scrollPageBackwardsAction());
      final int revButtonCC = 15;
      HardwareButton revButton = hardwareSurface.createHardwareButton(format("REVERSE_BUTTON"));
      revButton.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(kontrolF1MidiChannel, revButtonCC, 127));
      revButton.pressedAction().setBinding(scenes.scrollPageForwardsAction());
   }

   @Override
   public void exit() {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer
      // running.
      // getHost().showPopupNotification("Hello World Exited");
   }

   @Override
   public void flush() {
      // TODO Send any updates you need here.
      hardwareSurface.updateHardware();
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg) {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data) {
   }

   // private Transport mTransport;
   private HardwareSurface hardwareSurface;
}
