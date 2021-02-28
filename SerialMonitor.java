/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;

import cc.arduino.packages.BoardPort;
import processing.app.legacy.PApplet;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static processing.app.I18n.tr;

@SuppressWarnings("serial")
public class SerialMonitor extends AbstractTextMonitor {

  private Serial serial;
  private int serialRate;

  private static final int COMMAND_HISTORY_SIZE = 100;
  private final CommandHistory commandHistory =
      new CommandHistory(COMMAND_HISTORY_SIZE);

  public SerialMonitor(BoardPort port) {
    super(port);

    serialRate = PreferencesData.getInteger("serial.debug_rate");
    serialRates.setSelectedItem(serialRate + " " + tr("baud"));
    onSerialRateChange((ActionEvent event) -> {
      String wholeString = (String) serialRates.getSelectedItem();
      String rateString = wholeString.substring(0, wholeString.indexOf(' '));
      serialRate = Integer.parseInt(rateString);
      PreferencesData.set("serial.debug_rate", rateString);
      if (serial != null) {
        try {
          close();
          Thread.sleep(100); // Wait for serial port to properly close
          open();
        } catch (InterruptedException e) {
          // noop
        } catch (Exception e) {
          System.err.println(e);
        }
      }
    });

    onSendCommand((ActionEvent event) -> {
      String command = textField.getText();
      send(command);
      commandHistory.addCommand(command);
      textField.setText("");
    });

    onClearCommand((ActionEvent event) -> textArea.setText(""));

    // Add key listener to UP, DOWN, ESC keys for command history traversal.
    textField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {

          // Select previous command.
          case KeyEvent.VK_UP:
            if (commandHistory.hasPreviousCommand()) {
              textField.setText(
                  commandHistory.getPreviousCommand(textField.getText()));
            }
            break;

          // Select next command.
          case KeyEvent.VK_DOWN:
            if (commandHistory.hasNextCommand()) {
              textField.setText(commandHistory.getNextCommand());
            }
            break;

          // Reset history location, restoring the last unexecuted command.
          case KeyEvent.VK_ESCAPE:
            textField.setText(commandHistory.resetHistoryLocation());
            break;
        }
      }
    });
  }

  private void send(String s) {
    if (serial != null) {

	int l = s.length();
        int x = 0;
        while (x<l) {
        	int c = (int) s.charAt(x);
        	
        	if (c==47) {
        		x++;
        		if (x<l) {
        		  switch (s.charAt(x)) {
        			case 'n':
        				serial.write((byte) 10);
        				break;
        			case 'r':
        				serial.write((byte) 13);
        				break;
        			case '/':
        				serial.write((byte) 47);
        				break;
        			case '0':
        			  if ((x<(l-3))&&(s.charAt(x+1)=='x')){
        		            try {
              				String str = new StringBuilder().append(s.charAt(x+2)).append(s.charAt(x+3)).toString();
              				serial.write((byte) Integer.parseInt(str, 16));
        				x = x + 3; 
        		            } catch (NumberFormatException nfe) {
        		                //nfe.printStackTrace();
        		            }
        			  }	
        		  }
			}   		
        	} else if ((c>31)&&(c<127)) serial.write((byte) c);

		x++;
        }
    }
  }

  @Override
  public void open() throws Exception {
    super.open();

    if (serial != null) return;

    serial = new Serial(getBoardPort().getAddress(), serialRate) {
      @Override
      protected void message(char buff[], int n) {
        addToUpdateBuffer(buff, n);
      }
    };
  }

  @Override
  public void close() throws Exception {
    super.close();
    if (serial != null) {
      int[] location = getPlacement();
      String locationStr = PApplet.join(PApplet.str(location), ",");
      PreferencesData.set("last.serial.location", locationStr);
      serial.dispose();
      serial = null;
    }
  }
  
}
