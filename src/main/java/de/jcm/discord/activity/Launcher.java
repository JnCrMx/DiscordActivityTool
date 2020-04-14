package de.jcm.discord.activity;

import de.jcm.discordgamesdk.Core;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Launcher
{
	private static File downloadDiscordLibrary() throws IOException
	{
		String name = "discord_game_sdk";
		String suffix;
		if(System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			suffix = ".dll";
		}
		else
		{
			suffix = ".so";
		}

		String zipPath = "lib/x86_64/"+name+suffix;

		URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/latest/discord_game_sdk.zip");
		ZipInputStream zin = new ZipInputStream(downloadUrl.openStream());

		ZipEntry entry;
		while((entry = zin.getNextEntry())!=null)
		{
			if(entry.getName().equals(zipPath))
			{
				File tempDir = new File(System.getProperty("java.io.tmpdir"), "java-"+name+System.nanoTime());
				if(!tempDir.mkdir())
					throw new IOException("Cannot create temporary directory");
				tempDir.deleteOnExit();

				File temp = new File(tempDir, name+suffix);
				temp.deleteOnExit();

				FileOutputStream fout = new FileOutputStream(temp);
				zin.transferTo(fout);
				fout.close();

				zin.close();

				return temp;
			}
			zin.closeEntry();
		}
		zin.close();
		return null;
	}

	public static void main(String[] args)
	{
		try
		{
			File discordLibrary = downloadDiscordLibrary();
			if(discordLibrary==null)
			{
				JOptionPane.showMessageDialog(null, "Cannot download Discord SDK.",
				                              "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			Core.init(discordLibrary);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error downloading Discord SDK.",
			                              "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		DiscordActivityTool discordActivityTool = new DiscordActivityTool();

		JFrame frame = new JFrame();
		frame.setContentPane(discordActivityTool.$$$getRootComponent$$$());
		frame.pack();
		frame.setTitle("Discord Activity Tool");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				discordActivityTool.shutdownCore();
			}
		});
	}
}
