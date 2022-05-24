import java.util.*;
import java.awt.*;
import java.io.*;
import javax.swing.filechooser.*;
import javax.imageio.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import javax.sound.sampled.*;

public class MusicBox extends JPanel implements Runnable, ActionListener, AdjustmentListener
{
	int columnChecked = 0;
	int tempo = 200;
	JFrame frame;
	JToggleButton[][] noteButtons;
	JButton toggleMusicButton, clearButton;
	JLabel tempoLabel;
	JScrollBar tempoBar;
	boolean playingMusic = false;
	Thread timing;
	Color defaultButtonColor;
	Clip[] clip = new Clip[37];
	String[] notes = {"C3" ,"B3" ,"ASharp3" ,"A3" ,"GSharp3" ,"G3" ,"FSharp3" ,"F3" ,"E3" ,"DSharp3" ,"D3" ,"CSharp3",
						"C2" ,"B2" ,"ASharp2" ,"A2" ,"GSharp2" ,"G2" ,"FSharp2" ,"F2" ,"E2" ,"DSharp2" ,"D2" ,"CSharp2",
						"C1", "B1" ,"ASharp1" ,"A1" ,"GSharp1" ,"G1" ,"FSharp1" ,"F1" ,"E1" ,"DSharp1" ,"D1" ,"CSharp1",
						"C0"};
	String[] instrumentNames = {"Bell", "Glockenspiel", "Marimba", "Oboe", "Oh_Ah", "Piano"};
	JMenuItem[] instrumentItems;
	JMenuItem saveItem, loadItem;
	JMenuBar menuBar;
	JFileChooser fileChooser;
	JScrollPane scrollBars;

	public MusicBox()
	{
		frame = new JFrame("Music Box");
		frame.add(this);

		//Adding the note buttons
		noteButtons = new JToggleButton[37][50];
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(37,50));
		for(int i=0; i<noteButtons.length; i++)
		{
			for(int j=0; j<noteButtons[i].length; j++)
			{
				noteButtons[i][j] = new JToggleButton();
				noteButtons[i][j].setPreferredSize(new Dimension(30,30));
				noteButtons[i][j].setMargin(new Insets(0,0,0,0));
				noteButtons[i][j].setText(notes[i].replace("Sharp", "#"));
				buttonPanel.add(noteButtons[i][j]);
			}
		}
		scrollBars = new JScrollPane(buttonPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		defaultButtonColor = noteButtons[0][0].getBackground();
		frame.add(scrollBars, BorderLayout.CENTER);


		//Loading instruments
		loadTunes("Bell");

		//Creating the MenuBar
		menuBar = new JMenuBar();
		JPanel menuPanel = new JPanel(new GridLayout(1,2));
		toggleMusicButton = new JButton("Play");
		toggleMusicButton.addActionListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		menuPanel.add(toggleMusicButton);
		menuPanel.add(clearButton);

		JMenu instrumentMenu = new JMenu("Instruments");
		instrumentItems = new JMenuItem[instrumentNames.length];
		for(int i=0; i<instrumentNames.length; i++)
		{
			JMenuItem item = new JMenuItem(instrumentNames[i]);
			item.putClientProperty("name", instrumentNames[i]);
			item.addActionListener(this);
			instrumentMenu.add(item);
		}

		//Adding loading/saving
		String directory = System.getProperty("user.dir");
		fileChooser = new JFileChooser(directory);
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		loadItem = new JMenuItem("Load");
		loadItem.addActionListener(this);
		loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		fileMenu.add(saveItem);
		fileMenu.add(loadItem);

		menuBar.add(fileMenu);
		menuBar.add(instrumentMenu);
		menuBar.add(menuPanel);
		frame.setJMenuBar(menuBar);

		//Adding tempo bar
		JPanel tempoPanel = new JPanel();
		tempoLabel = new JLabel("Tempo: "+tempo);
		tempoBar = new JScrollBar(JScrollBar.HORIZONTAL, 200, 0, 50, 350);
		tempoBar.addAdjustmentListener(this);
		tempo = 400-tempoBar.getValue();
		tempoPanel.setLayout(new BorderLayout());
		tempoPanel.add(tempoLabel, BorderLayout.WEST);
		tempoPanel.add(tempoBar, BorderLayout.CENTER);
		frame.add(tempoPanel, BorderLayout.SOUTH);

		frame.setSize(1500, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		//Timing
		timing = new Thread(this);
		timing.start();

		frame.setVisible(true);
	}

	public void run()
	{
		long baseTempo = 60;
		while(true)
		{
			try{
				if(playingMusic)
				{
					for(int i=0; i<37; i++)
					{
						noteButtons[i][columnChecked].setBackground(Color.RED);
						if(noteButtons[i][columnChecked].isSelected())
						{
							clip[i].start();
						}
					}
					Thread.sleep(tempo);

					for(int i=0; i<37; i++)
					{
						noteButtons[i][columnChecked].setBackground(defaultButtonColor);
						if(noteButtons[i][columnChecked].isSelected())
						{
							clip[i].stop();
							clip[i].setFramePosition(0);
						}
					}

					columnChecked++;
					if(columnChecked == noteButtons[0].length)
					{
						columnChecked = 0;
					}
				}
				else
				{
					Thread.sleep(0);
				}
			}catch(InterruptedException e){}
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if(e.getSource() == tempoBar)
		{
			tempo = 400-tempoBar.getValue();
			tempoLabel.setText("Tempo: "+tempoBar.getValue());
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == toggleMusicButton)
		{
			playingMusic = playingMusic? false:true;

			if(playingMusic)
				toggleMusicButton.setText("Stop");
			else
				toggleMusicButton.setText("Play");
		}
		else if(e.getSource() == clearButton)
		{
			for(int i=0; i<noteButtons.length; i++)
			{
				for(int j=0; j<noteButtons[i].length; j++)
				{
					noteButtons[i][j].setSelected(false);
				}
			}
			reset();
		}
		else if(e.getSource() == saveItem)
		{
			saveSong();
			reset();
		}
		else if(e.getSource() == loadItem)
		{
			reset();
			loadSong();
		}
		else
		{
			String name = (String) ((JMenuItem)e.getSource()).getClientProperty("name");
			loadTunes(name);
			reset();
		}
	}

	public void saveSong()
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", ".txt");
		fileChooser.setFileFilter(filter);
		if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			try{
				String absPath = file.getAbsolutePath();
				System.out.println(absPath.substring(absPath.length()-3));
				if(absPath.substring(absPath.length()-3).equals(".txt"))
				{
					absPath = absPath.substring(0, absPath.length()-3);
				}
				String currSong = "";
				String[] noteNames = {"c ","b ","a-","a ","g-","g ","f-","f ","e ","d-","d ","c-","c ","b ","a-","a ","g-","g ", "f-","f " ,"e ","d-","d ","c-","c ","b ","a-","a ","g-","g ","f-","f ","e ","d-","d ","c-","c "};
				for(int i=0; i<noteButtons.length; i++)
				{
					if(i==0)
						currSong += "" + tempo + " " + noteButtons[i].length + "\n";
					currSong += noteNames[i] + " ";

					for(int j=0; j<noteButtons[i].length; j++)
					{
						if(noteButtons[i][j].isSelected())
							currSong += "x";
						else
							currSong += "-";
					}
					currSong += "\n";
				}

				BufferedWriter outputStream = new BufferedWriter(new FileWriter(absPath+".txt"));
				outputStream.write(currSong);
				outputStream.close();

			}catch(IOException e){}

		}
	}

	public void loadTunes(String instrument)
	{
		try {
			for(int x=0;x<notes.length;x++)
			{
				URL url = this.getClass().getClassLoader().getResource(instrument+"\\" + instrument+" - " + notes[x]+".wav");
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
				clip[x] = AudioSystem.getClip();
				clip[x].open(audioIn);
			}
		} catch (UnsupportedAudioFileException|IOException|LineUnavailableException e) {e.printStackTrace();}
	}

	public void loadSong()
	{
		if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				File file = fileChooser.getSelectedFile();
				BufferedReader input = new BufferedReader(new FileReader(file));
				String line = input.readLine();
				String[] tempoLength = line.split(" ");
				tempo = Integer.parseInt(tempoLength[0]);
				int colomnsToRead = Integer.parseInt(tempoLength[1]);
				char[][] song = new char[37][colomnsToRead];
				int row = 0;
				while((line = input.readLine()) != null)
				{
					for(int i=3; i<line.length(); i++)
					{
						song[row][i-3] = line.charAt(i);
					}
					row++;
				}
				setNotes(song);
				tempoBar.setValue(tempo);
			}catch(IOException e){}
		}
	}

	public void setNotes(char[][] song)
	{
		frame.remove(scrollBars);
		noteButtons = new JToggleButton[song.length][song[0].length];
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(song.length,song[0].length));

		for(int i=0; i<song.length; i++)
		{
			for(int j=0; j<song[i].length;j++)
			{
				noteButtons[i][j] = new JToggleButton();
				noteButtons[i][j].setPreferredSize(new Dimension(30,30));
				noteButtons[i][j].setMargin(new Insets(0,0,0,0));
				noteButtons[i][j].setText(notes[i].replace("Sharp", "#"));
				buttonPanel.add(noteButtons[i][j]);
			}
		}

		scrollBars = new JScrollPane(buttonPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frame.add(scrollBars, BorderLayout.CENTER);

		for(int i=0; i<noteButtons.length; i++)
		{
			for(int j=0; j<noteButtons[i].length; j++)
			{
				if(song[i][j] == 'x')
					noteButtons[i][j].setSelected(true);
			}
		}
		frame.revalidate();
	}

	public void reset()
	{
		columnChecked = 0;
		playingMusic = false;
		toggleMusicButton.setText("Play");
		for(int i=0; i<noteButtons.length;i++)
		{
			for(int j=0; j<noteButtons[i].length; j++)
			{
				noteButtons[i][j].setBackground(defaultButtonColor);
			}
		}
	}

	public static void main(String[] args)
	{
		MusicBox a = new MusicBox();
	}
}