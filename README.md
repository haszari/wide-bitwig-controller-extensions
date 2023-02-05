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

A single controller extension for the [OG Korg nanoKontrol](http://i.korg.com/uploads/Support/nanoKONTROL_OM_E2_633664627400740000.pdf).

- Faders are [bound](./src/main/java/com/cartoonbeats/NanoKontrolExtension.java#L83) to channel faders.
- Knobs are [bound](./main/src/main/java/com/cartoonbeats/NanoKontrolExtension.java#L77) to the first [remote control](https://www.bitwig.com/userguide/latest/midi_controllers/#the_remote_controls_pane) in the first page of the first device.
