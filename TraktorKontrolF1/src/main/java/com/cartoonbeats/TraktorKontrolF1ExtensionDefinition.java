package com.cartoonbeats;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class TraktorKontrolF1ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("C584449E-EF3D-40E4-B8D9-AA5DE67A68B4");

   public TraktorKontrolF1ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "KontrolF1";
   }

   @Override
   public String getAuthor()
   {
      return "haszari";
   }

   @Override
   public String getVersion()
   {
      return "0.1";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }

   @Override
   public String getHardwareVendor()
   {
      return "Cartoon Beats Reality";
   }

   @Override
   public String getHardwareModel()
   {
      return "KontrolF1";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 18;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 1;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 1;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
      // NI allows for 4 decks, on different virtual ports.
      // However, I don't think Bitwig allows multiple options for autodetect ports.
      // So, we'll just autodetect the first F1, others will need to be added manually.
      // Note these port names are tested/discovered on macOS, might be different on other platforms.
      list.add(
         new String[]{"Traktor Kontrol F1 - 1 Input"},
         new String[]{"Traktor Kontrol F1 - 1 Output"}
      );
   }

   @Override
   public TraktorKontrolF1Extension createInstance(final ControllerHost host)
   {
      return new TraktorKontrolF1Extension(this, host);
   }
}
