package gui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;


public class ImageButton extends JButton {

	private static final long serialVersionUID = 1L;

	private int width;
	private int height;
	private Image image;
	private Image currentImage;
	private Image rolloverImage;
	private Image hitImage;
	
	public ImageButton(String imageUrl, String text) {
		super();
		currentImage = new ImageIcon(
				URLGetter.getResource(imageUrl)
				).getImage();
		
		image = currentImage;
		addEvent();
		
		this.setText(text);

		width = image.getWidth(null);
		height = image.getHeight(null);
		this.setSize(width, height);
	}
	
	public ImageButton(String imageUrl, String text, String rolloverImageUrl) {
		this(imageUrl, text);
		rolloverImage = new ImageIcon(
				URLGetter.getResource(rolloverImageUrl)
				).getImage();
	}
	
	public ImageButton(String imageUrl, String text, 
			String rolloverImageUrl, String hitImageUrl) {
		
		this(imageUrl, text, rolloverImageUrl);
		hitImage = new ImageIcon(
				URLGetter.getResource(hitImageUrl)
				).getImage();
	}
	

	public void paint(Graphics g) {
		g.drawImage(image, 0,0, width, height, null);
	}
	
	public void setImage(String imgUrl) {
		currentImage = new ImageIcon(
				URLGetter.getResource(imgUrl)).getImage();
	}

	private void addEvent() {
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent arg0) {
				if (rolloverImage != null) {
					image = rolloverImage;
					paint(getGraphics());
				}
			}
			public void mouseExited(MouseEvent arg0) {
				if (currentImage != null) {
					image = currentImage;
					paint(getGraphics());
				}
			}
			public void mouseClicked(MouseEvent arg0) {
				if (hitImage != null) {
					image = hitImage;
					paint(getGraphics());
				}
			}
		});
	}
}
