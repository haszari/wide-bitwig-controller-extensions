package com.cartoonbeats;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class LaunchControlXLExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("A08DEF7C-64A1-4977-BD24-EEBA5F9DF2FC");

   public LaunchControlXLExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "LaunchControlXL";
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
      return "LaunchControlXL";
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
      if (platformType == PlatformType.WINDOWS)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Traktor Kontrol F1 - 1 Input"}, new String[]{"Traktor Kontrol F1 - 1 Output"});
      }
      else if (platformType == PlatformType.MAC)
      {
         list.add(new String[]{"Launch Control XL"}, new String[]{"Launch Control XL"});
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
      }
   }

   @Override
   public LaunchControlXLExtension createInstance(final ControllerHost host)
   {
      return new LaunchControlXLExtension(this, host);
   }
}
