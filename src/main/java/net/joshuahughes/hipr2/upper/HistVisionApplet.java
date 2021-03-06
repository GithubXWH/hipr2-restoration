package net.joshuahughes.hipr2.upper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Color;
import java.net.*;
import javax.swing.event.*;

/**
 *
 *HistVisionApplet is a subclass of the JApplet class which is tailored
 *for use in the HIPR application. It is used with operators which 
 *only require the use of one input image, but which have two outputs
 *to display one of which is a histogram.  
 *
 *@author Timothy Sharman
 */

public class HistVisionApplet extends JApplet { 

  //The main window
  /**
   *The main container of the applet
   */
  public Container container = null;

  //The images
  /**
   *The source image
   */
  public Image src;
  /** 
   *The destination image
   */
  public Image dest;
  /**
   *The histogram image
   */
  public Image hist;

  //Their canvases
  /**
   *The source image canvas
   */
  public ImageCanvas src_canvas;
  /**
   *The destination image canvas
   */
  public ImageCanvas dest_canvas;
  /**
   *The histogram image canvas
   */
  public HistogramImageCanvas hist_canvas;

  //pixel arrays for the images
  /**
   *The source image array
   */
  public int [] src_1d;
  /**
   *The destination image array
   */
  public int [] dest_1d;
  /**
   *The histogram image array
   */
  public int [] hist_1d;

  //the width and height of the input image
  /**
   *The input image width
   */
  public int i_w; 
  /**
   *The input image height
   */
  public int i_h;
  
  //the width and height of the output image
  /**
   *The destination image width
   */
  public int d_w;
  /**
   *The destination image height
   */
  public int d_h;

  //Co-ordiante points for finding the intensity value at a selected point
  private int actualx, actualy;
  
  /**
   *Operator required to produce the histogram
   */
  public Histogram histogramalgorithm;

  /**
   *The execution time 
   */
  public long time_msec;
  
  //The layout managers
  private GridBagLayout layout = new GridBagLayout(); 
  private GridBagLayout loadlayout = new GridBagLayout();
  /**
   *The mid panel layout
   */
  public GridBagLayout midlayout = new GridBagLayout();
  /**
   *The bottom right panel layout
   */
  public GridBagLayout brlayout = new GridBagLayout();
  private GridBagLayout bllayout = new GridBagLayout();
  /**
   *The bottom middle panel layout
   */
  public GridBagLayout bmlayout = new GridBagLayout();

  private GridBagConstraints wholec = new GridBagConstraints();    
  private GridBagConstraints loadc = new GridBagConstraints();
  /**
   *The mid panel constraints
   */
  public GridBagConstraints midc = new GridBagConstraints();
  private GridBagConstraints blc = new GridBagConstraints();
  /**
   *The bottom right panel constraints
   */
  public GridBagConstraints brc = new GridBagConstraints();
  /**
   *The bottom middle panel constraints
   */
  public GridBagConstraints bmc = new GridBagConstraints();

  //the interface components
  private JPanel top = new JPanel();
  /**
   *The middle panel
   */
  public JPanel mid = new JPanel();
  /**
   *The bottom right panel
   */
  public JPanel br = new JPanel();
  /**
   *The bottom left panel
   */
  public JPanel bl = new JPanel();
  /**
   *The bottom middle panel
   */
  public JPanel bm = new JPanel();
  /**
   *The load button
   */
  public JButton load_image = new JButton("Load image");
  private JTextField input = new JTextField(30);
  private JLabel inputlab = new JLabel("Input");
  /**
   *The output image label
   */
  public JLabel output = new JLabel("Output");
  /**
   *The input image size label
   */
  public JLabel insize = new JLabel();
  /**
   *The output image size label
   */
  public JLabel outsize = new JLabel();
  private JLabel hist_label = new JLabel("Histogram");
  private JLabel hist_range = new JLabel("0               64"+
					 "           128"+
					 "          192"+
					 "          256");  

  //Labels into which info obtained from clicking on images is placed
  private JLabel blPosition = new JLabel("Point X:  Y: ");
  private JLabel brPosition = new JLabel("Point X:  Y: ");
  private JLabel blIntensity = new JLabel("Intensity: ");
  private JLabel brIntensity = new JLabel("Intensity: ");

  //The listeners for finding point positions and intensities
  /**
   *The pixel listener for the input image
   */
  PixelListener blpl = new PixelListener();

  /**
   *The pixel listener for the output imagw
   */
  PixelListener brpl = new PixelListener();

  //the button listener
  private LoadListener ll = new LoadListener();
  
  //the image address and name
  /**
   *The address from which to load pictures
   */
  public URL theURL;
  private String image_url = "brg1.gif";
 
  /**
   *Called automatically when the applet is started. Initialises the
   *interface components ready for drawing on screen.  
   */
  
  public void init() {

    //sets up the main window
    container = this.getContentPane();
    container.setLayout(layout);
    container.setBackground(Color.white);

    //set up the default image
    set_image();
 
    //set up the interface
    set_interface();
    //add_extra is implemented by any subclasses to allow them to
    //change any of the interface window. 
    add_extra();
  
    //now the image is created, draw it on the interface
    gui_add_image();
    
  }  

  /**
   *Sets up the default image
   */

  public void set_image() {
    
    //Get image name
    String image_name = "brg1.gif";    
    
    try {
      URL theURL = new URL(getDocumentBase(),"images/"+image_name);
      src = this.getImage(theURL);          
    }  catch (MalformedURLException e4) {
      JOptionPane.showMessageDialog(null,("The default file is not present"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    src_canvas = new ImageCanvas(src);
    dest_canvas = new ImageCanvas(src);
    i_w = src_canvas.getImageWidth();
    i_h = src_canvas.getImageHeight();
    
    //Fetch the image and turn it into a pixel array
    src_1d = new int[i_w * i_h];
    PixelGrabber pg = new PixelGrabber(src,0,0,i_w,i_h,src_1d,0,i_w);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      JOptionPane.showMessageDialog(null,("Unable to handle default file"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    if ((pg.status() & ImageObserver.ABORT) != 0) {
      JOptionPane.showMessageDialog(null,("The default file image cannot be loaded"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    d_w = i_w;
    d_h = i_h;
    
    //make a blank destination array 
    dest_1d = new int [d_w* d_h];
    
    for(int i = 0; i < dest_1d.length; i++){
      dest_1d[i] = src_1d[i];
    }

    //code to create the histogram which is automatically added below
    //make the histogram
    histogramalgorithm = new Histogram();
    histogramalgorithm. run();
    hist_1d = histogramalgorithm. histogram(src_1d, i_w, i_h);
    hist = createImage(new MemoryImageSource(256, 256, hist_1d, 0, 256));
    //make a canvas for the histogram
    hist_canvas = new HistogramImageCanvas(hist);
  }

  /**
   *set_interface sets up the 'standard' interface that is the same
   *regardless of which operator is being run
   */

  public void set_interface(){
   
    //The top panel layout

    top. setLayout(loadlayout);
    top. setBackground(Color.white);

    loadc.fill = GridBagConstraints.BOTH;
    loadlayout. setConstraints(load_image,loadc);
    top .add(load_image);

    loadc.gridwidth = GridBagConstraints.REMAINDER;
    loadlayout .setConstraints(input,loadc);
    top .add(input);
    input. setText(image_url);

    load_image. addActionListener(ll);

    //The mid panel layout

    mid. setLayout(midlayout);
    mid. setBackground(Color.white);
   
    //The bottom middle panel layout

    bm. setLayout(bmlayout);
    bm. setBackground(Color.white);

    bmc. gridx = 0;
    bmc. gridy = 0;
    bmlayout. setConstraints(hist_label, bmc);
    bm. add(hist_label);
    
    bmc. gridx = 0;
    bmc. gridy = 1;
    bmlayout. setConstraints(hist_canvas, bmc);
    bm. add(hist_canvas);
    
    bmc. gridx = 0;
    bmc. gridy = 2;
    bmlayout. setConstraints(hist_range, bmc);
    bm. add(hist_range);

 
    //The bottom right panel layout

    br. setLayout(brlayout);
    br. setBackground(Color.white);

    brc. gridx = 0;
    brc. gridy = 0;
    brlayout. setConstraints(output, brc);
    br. add(output);
 
    brc. gridx = 0;
    brc. gridy = 2;
    brlayout. setConstraints(outsize, brc);
    outsize. setText(d_w+" x "+d_h);
    br. add(outsize);

    brc. gridx = 0;
    brc. gridy = 3;
    brlayout. setConstraints(brPosition, brc);
    br. add(brPosition);

    brc. gridx = 0;
    brc. gridy = 4;
    brlayout. setConstraints(brIntensity, brc);
    br. add(brIntensity);

    //The bottom left panel layout

    bl. setLayout(bllayout);
    bl. setBackground(Color.white);
    
    blc. gridx = 0;
    blc. gridy = 0;
    bllayout. setConstraints(inputlab, blc);
    bl. add(inputlab);
    
    blc. gridx = 0;
    blc. gridy = 2;
    bllayout. setConstraints(insize, blc);
    insize. setText(i_w+" x "+i_h);
    bl. add(insize);

    blc. gridx = 0;
    blc. gridy = 3;
    bllayout. setConstraints(blPosition, blc);
    bl. add(blPosition);

    blc. gridx = 0;
    blc. gridy = 4;
    bllayout. setConstraints(blIntensity, blc);
    bl. add(blIntensity);
    
    //the applet layout
    wholec. gridx = 0;
    wholec. gridy = 0;
    wholec. gridwidth = 3;
    wholec. fill = GridBagConstraints.BOTH;
    wholec. weightx = 0.3;
    wholec. weighty = 0.2;
    layout. setConstraints(top, wholec);
    container. add(top);

    wholec. gridwidth = 1;
    wholec .gridx = 0;
    wholec. gridy = 1;
    layout. setConstraints(mid, wholec);
    container. add(mid);    

    wholec. gridwidth = 1;
    wholec. gridx = 0;
    wholec. gridy = 2;
    layout. setConstraints(bl, wholec);
    container. add(bl);
   
    wholec. gridx = 1;
    wholec. gridy = 2;
    layout. setConstraints(bm, wholec);
    container. add(bm);

    wholec. gridx = 2;
    wholec. gridy = 2;
    layout. setConstraints(br, wholec);
    container. add(br);
  } 

  /**
   *
   *Handles the actions performed when the mouse is used in the interface
   *to find information about image pixels. Should be added to an ImageCanvas.
   *
   *@param e The event which caused this object to be called
   *
   */
  

  public class PixelListener extends MouseMotionAdapter {
    
    //Used to try and find the intensity value at a point on the interface
    
    public void mouseDragged(MouseEvent e) {
      ImageCanvas ic = (ImageCanvas)e. getSource();
      int clickx = e. getX();
      int clicky = e. getY();
      int scalex = 0;
      int scaley = 0;
      int image_width = ic. getImageWidth();
      int image_height = ic. getImageHeight();
    
      try{
	if ((image_width >= 256) || (image_height >= 256)){
	  
	  if (image_width > image_height){
	    
	  float ratio = (float) image_width / (float) image_height;
	  scalex = 256;
	  scaley = (int) ((float) 256/ ratio);
	  actualx = (int)(((float)image_width / (float)scalex)*clickx);
	  actualy = (int)(((float)image_height / (float)scaley)*clicky);
	  }  
	
	  else if (image_height > image_width){
	    
	    float ratio = (float) image_height /  (float) image_width;
	    scalex = image_width;
	    scaley = 256;
	    scalex = (int) ( (float) 256 / ratio);
	    actualx = (int)(((float)image_width / (float)scalex)*clickx);
	    actualy = (int)(((float)image_height / (float)scaley)*clicky);
	  } 
	  else {  
	    actualx = (int)(((float)image_width / (float)256)*clickx);
	    actualy = (int)(((float)image_height / (float)256)*clicky);
	  }
	  
	}
	else {
	  actualx = clickx;
	  actualy = clicky;
	}
	
	//Need to make some chacks that click is inside the image area
	if((actualx < 0) || (actualy < 0)){
	  //do nothing
	}
	else {
	  if(actualx > image_width - 1){
	    actualx = image_width - 1;
	  }
	  if(actualy > image_height - 1){
	    actualy = image_height - 1;
	  }
	  if(ic == src_canvas){
	    int intense = src_1d[actualx+(actualy*image_width)] & 0x000000ff;
	    blPosition. setText("Point X:"+actualx+"  Y:"+actualy);
	    blIntensity. setText("Intensity:"+intense);
	  }
	  if(ic == dest_canvas){
	    int intense = dest_1d[actualx+(actualy*image_width)] & 0x000000ff;
	    brPosition. setText("Point X:"+actualx+"  Y:"+actualy);
	    brIntensity. setText("Intensity:"+intense);	
	  }
	}
      }
      //Must have clicked outside picture so ignore 
      catch(ArrayIndexOutOfBoundsException evt){
	//do nothing
      }
    }
  }


  /**
   *add_extra is used to add extra components to the window of the operator.
   *This is done by adding extra components into the blank mid panel.
   *This panel is then added to the generic panels to make up the interface.
   *The default is for this panel to be blank.
   */

  public void add_extra(){}

  
  private void gui_add_image() {
    
    //add the source image
    blc. gridx = 0;
    blc. gridy = 1;
    bllayout. setConstraints(src_canvas, blc);
    bl. add(src_canvas); 
    src_canvas. addMouseMotionListener(blpl);

    //add the destination image
    brc. gridx = 0;
    brc. gridy = 1;
    brlayout. setConstraints(dest_canvas, brc);
    br. add(dest_canvas);
    dest_canvas. addMouseMotionListener(brpl);

  }
  
  /**
   *set_src_image2 is used to add a newly loaded image to the interface
   *It is called by set_src_image once the operator thread has been 
   *recreated.
   *@param input_img The new input image array
   *@param w The width of the image
   *@param h The height of the image
   *@param name The name of the image
   */

  public void set_src_image2(int [] input_img , int w, int h, String name) {
    
    //reset the input image

    i_w = w;
    i_h = h;
    src_1d = new int[i_w*i_h]; 
    src_1d = input_img;
    src = createImage(new MemoryImageSource(i_w,i_h,src_1d,0,i_w));
    src_canvas. updateImage(src);
    insize. setText(i_w+" x "+i_h);
    
    //blank the destination image
    d_w = i_w;
    d_h = i_h;
    dest_1d = new int [d_w* d_h];
    
    dest_1d = input_img;
    dest = createImage(new MemoryImageSource(d_w,d_h,dest_1d,0,d_w));
    dest_canvas. updateImage(dest);
    outsize. setText(d_w+" x "+d_h);

    //Add the new pixel listeners
    PixelListener blpl = new PixelListener();
    PixelListener brpl = new PixelListener();
    src_canvas. addMouseMotionListener(blpl);
    dest_canvas. addMouseMotionListener(brpl);
    blPosition. setText("Point X:  Y: ");
    blIntensity. setText("Intensity: ");
    brPosition. setText("Point X:  Y: ");
    brIntensity. setText("Intensity: ");

    //redo the histogram
    hist_1d = histogramalgorithm. histogram(src_1d, i_w, i_h);
    
    hist = createImage(new MemoryImageSource(256, 256, hist_1d, 0, 256));
    
    bm. remove(hist_canvas);
    hist_canvas = new HistogramImageCanvas(hist);
    hist_canvas. lowthresh = 64;
    hist_canvas. highthresh = 192;
    bmc. gridx = 0;
    bmc. gridy = 1;
    
    bmlayout. setConstraints(hist_canvas, bmc);
    bm. add(hist_canvas);
    bm. add(hist_label);
    bm. add(hist_range);
    
    bm. doLayout();
    

    container. validate();
    container. repaint();
  }

  /**
   *set_src_image tells the applet of the image to be loaded and displayed
   *on the screen. It also is required to recreate the operator which was
   *running at the time. The method should be implemented by the subclass
   *as this default doesn't do this.
   *@param input_img The new input image array
   *@param w The width of the image
   *@param h The height of the image
   *@param name The name of the image
   */
  public void set_src_image(int [] input_img, int w, int h, String name) {
    
    //Must restart any operator here
    //This is a temporary implementation of this method
    set_src_image2(input_img, w, h, name);
 
  }    

  /**
   *
   *Handles the actions performed when load buttons on the interface 
   *are pressed.
   *
   *@param evt The event which caused this object to be called
   *
   */

  class LoadListener implements ActionListener {
    
    public void actionPerformed(ActionEvent evt) {

    //the load button was pressed
      
      image_url = input.getText();    
      try {
	theURL = new URL(image_url);
      } catch (MalformedURLException e) {

	// if a file rather than a URL, then try local file space
	// If still not a URL and not a file then report error

	try {

	  theURL = new URL(getDocumentBase(),"images/"+image_url);
	  
        } catch (MalformedURLException e4) {
	  JOptionPane.showMessageDialog(null,("File not found"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
	  return;
        }
      }

      String filename = theURL.getFile();

      //check the file is of the correct type
      if ((!filename.endsWith("jpg"))&& ! (filename.endsWith("gif"))) {
	JOptionPane.showMessageDialog(null,("Invalid file extension. Use .gif or .jpg"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
	return;
      }
      apply_send_image();                 
    }
    
  }
  
  /**
   *apply_send_image grabs the image from the URL and turns it into an
   *array of pixels which the operators can manipulate
   */
  
  public void apply_send_image(){
    
    //Turn the specified user image file name into a pixel array
    
    MediaTracker tracker;
    tracker = new MediaTracker(this);
    
    Image image = getImage(theURL);  
    tracker.addImage(image, 0);
    
    //this is to make sure the image loads without an error
    try {
      tracker. waitForID(0);
    } catch(InterruptedException e){
      JOptionPane.showMessageDialog(null,("Unable to load image - try again"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if (tracker. statusID(0, false) == tracker.ERRORED){
      JOptionPane.showMessageDialog(null,("Unable to load image - try again"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    ImageCanvas c = new ImageCanvas(image);
    int i_w = c.getImageWidth();
    int i_h = c.getImageHeight();
    int[] src_1d = new int[i_w*i_h];
    PixelGrabber pg1 = new PixelGrabber(image,0,0,i_w,i_h,src_1d,0,i_w);
    try {
      pg1.grabPixels();
    } catch (InterruptedException e) {
      JOptionPane.showMessageDialog(null,("Unable to load image - try again"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    if ((pg1.status() & ImageObserver.ABORT) != 0) {
      JOptionPane.showMessageDialog(null,("Unable to load image - try again"),
				    "Load Error", JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    set_src_image(src_1d, i_w, i_h, image_url);
    
  }
  
  
  /**
   *Used by the browser to find out what the applet is for
   *
   *@return the function of the applet
   */
  public String getAppletInfo() {
    return "An applet for running image processing operators on. Written by Timothy Sharman";
  }


}

