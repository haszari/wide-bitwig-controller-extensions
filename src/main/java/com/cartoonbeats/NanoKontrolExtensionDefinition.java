package com.cartoonbeats;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class NanoKontrolExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("D0028103-1D22-4051-8E08-9041D665C19C");

   public NanoKontrolExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "nanoKontrol";
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
      return "Cartoon Beats";
   }

   @Override
   public String getHardwareModel()
   {
      return "nanoKontrol";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 16;
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
         // list.add(new String[]{"nanoKontrol SLIDER/KNOB"}, new String[]{"Output Port 0"});
      }
      else if (platformType == PlatformType.MAC)
      {
         list.add(new String[]{"nanoKontrol SLIDER/KNOB"}, new String[]{"nanoKontrol CTRL"});
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
      }
   }

   @Override
   public NanoKontrolExtension createInstance(final ControllerHost host)
   {
      return new NanoKontrolExtension(this, host);
   }
}
