import javax.swing.JFrame;


public class Rocket extends JFrame {

	public Rocket(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		RocketPanel fp = new RocketPanel();
		this.add(fp);
	}
	
	public static void main(String[] args) {
		Rocket f = new Rocket();
		f.pack();
		f.setVisible(true);
	}

}
