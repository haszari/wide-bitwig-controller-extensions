package com.cartoonbeats;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class MidiFighterTwisterExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("0D4C94F1-D66F-456A-825F-9841171755B8");

   public MidiFighterTwisterExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "MidiFighterTwister";
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
      return "Midi Fighter Twister";
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
         list.add(new String[]{"Midi Fighter Twister"}, new String[]{"Midi Fighter Twister"});
      }
      else if (platformType == PlatformType.MAC)
      {
         list.add(new String[]{"Midi Fighter Twister"}, new String[]{"Midi Fighter Twister"});
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         list.add(new String[]{"Midi Fighter Twister"}, new String[]{"Midi Fighter Twister"});
      }
   }

   @Override
   public MidiFighterTwisterExtension createInstance(final ControllerHost host)
   {
      return new MidiFighterTwisterExtension(this, host);
   }
}
