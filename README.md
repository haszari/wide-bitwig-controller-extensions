# wide-bitwig-controller-extensions
[Bitwig](https://www.bitwig.com/) controller scripts for live performance on multiple tracks at once.

## Wide? WTF
Most controller extensions provide deep control of all devices a single focused channel.

This repo takes a different approach. The goal is to maximise the
control of multiple channels at once.

Each channel has one or more macro parameters mapped automatically (depending
on the layout of the controller), so you can tweak synth params across many
instruments.

I'm betting that's more fun!

## Status
Early days.

#### [OG Korg nanoKontrol](http://i.korg.com/uploads/Support/nanoKONTROL_OM_E2_633664627400740000.pdf).
- Faders are [bound](./src/main/java/com/cartoonbeats/NanoKontrolExtension.java#L83) to channel faders.
- Knobs are [bound](./main/src/main/java/com/cartoonbeats/NanoKontrolExtension.java#L77) to the first [remote control](https://www.bitwig.com/userguide/latest/midi_controllers/#the_remote_controls_pane) in the first page of the first device.

#### Native Instruments Kontrol F1
- Knob is bound to channel remote control 1.
- Fader is bound to channel remote control 2.
- Grid buttons for launching clips (with playing feedback light).
- Stop button for stopping.
- Navigate tracks & clip pages:
  - `SYNC`/`QUANT` left/right
  - `CAPTURE`/`TYPE` up/down

#### Novation LaunchControl XL
- Fader is channel fader.
- Knobs are first three remote controls.
- Channel buttons are remote controls 3 & 4 (top right). These are momentary "stab" buttons; hold down to set remote to max. Can be used for custom sends or to enable an effect (etc).