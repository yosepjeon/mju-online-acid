package gui;

import java.io.*;
import javax.sound.sampled.*;

public class Sound
{
   public void BackGroundMusic()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/bgm.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.loop(-1);
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Correct()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/correct.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Wrong()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/wrong.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Miss()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/miss.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Hidden()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/hidden.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Gold_or_Diamond()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/gold_or_diamond.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Item()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/item.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void Level_up()
   {
      try
      {
         AudioInputStream ais = AudioSystem.getAudioInputStream(new File("asset/sound/level_up.wav"));
         Clip c = AudioSystem.getClip();
         c.open(ais);
         c.start();
      } catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}