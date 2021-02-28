package strParse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class strParse {
	
	public static StringBuilder StringToBytes(String s) {
        StringBuilder sb = new StringBuilder();		
		
	int l = s.length();
        byte[] outArray = new byte [l];
        int y = 0; int x = 0;

        while (x<l) {
        	int c = (int) s.charAt(x);
        	
        	if (c==47) {
        		x++;
        		if (x<l) {
        		  byte t = 0;
        		  switch (s.charAt(x)) {
        			case 'n':
        				t = 10;
        				break;
        			case 'r':
        				t = 13;
        				break;
        			case '/':
        				t = 47;
        				break;     
        			case '0':
        				if ((x<(l-3))&&(s.charAt(x+1)=='x')){
        		          		try {
              						String str = new StringBuilder().append(s.charAt(x+2)).append(s.charAt(x+3)).toString();
              						outArray[y] =(byte) Integer.parseInt(str, 16);
        						sb.append(String.format("%02x", outArray[y]));
        						sb.append(' ');
        						y++;
        						x = x + 4; 
        		          		} catch (NumberFormatException nfe) {}
        		  		}
        		  }
        		  if (t!=0) {
        			  outArray[y] = t;
        			  sb.append(String.format("%02x", outArray[y]));
        			  sb.append(' ');
        			  x++; y++;
        		  }
        		}   		
        	} else if ((c>31)&&(c<127)) {
        		outArray[y] = (byte) c;
        		sb.append(String.format("%02x", outArray[y]));
        		sb.append(' ');
            	y++; x++;
        	} else x++;
        }
        
        System.out.println(y);
        
        return sb;
	}
	
	
	public static void main(String[] args) throws IOException {
		//String s = '"0/0xAA//dfg/dfgd/g/n/r/fghb/0xfFfÿgh_+54f\/"';
		String s;
		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        s = br.readLine();
        StringBuilder sb = StringToBytes(s);

		System.out.print(sb.toString());
	       
	}
}
