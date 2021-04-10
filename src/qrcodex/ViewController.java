/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qrcodex;

import com.itextpdf.text.pdf.BarcodeQRCode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javax.imageio.ImageIO;

/**
 *
 * @author Isaac
 */
public class ViewController implements Initializable {
    
    @FXML
    private AnchorPane root;

    /*
        @FXML
        private ImageView about;
    
    
        @FXML
        private ImageView close;
    */

    @FXML
    private JFXTextField text;

    @FXML
    private ColorPicker foreground;

    @FXML
    private ColorPicker background;

    @FXML
    private JFXCheckBox transparentColor;

    @FXML
    private TextField width;

    @FXML
    private TextField height;

    @FXML
    private JFXRadioButton pdf;

    @FXML
    private JFXRadioButton image;

    @FXML
    private Pane formatPane;
    
    @FXML
    private Pane popupPane;

    @FXML
    private JFXComboBox<String> format;

    @FXML
    private TextField folderText;

    @FXML
    private JFXButton folderButton;

    @FXML
    private TextField filename;

    @FXML
    private JFXButton generate;

    @FXML
    private Label message;

    @FXML
    private Hyperlink link;

    @FXML
    private ImageView closePopup;
    
    String DEFAULT_FILE_NAME = "output";
    
    Popup pop = new Popup();
    
    int x,y;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        popupPane.setVisible(false);
        formatPane.setVisible(false);
        
        x=(int)(root.getHeight() - popupPane.getHeight())/2;
        y=(int)(root.getWidth() - popupPane.getWidth())/2;
        
        format.getItems().addAll("png","jpg","svg","bmp");
        format.setValue("png");
        
        image.selectedProperty().addListener((observable, oldValue, newValue) -> {
            formatPane.setVisible(newValue);
            if(newValue){
                launchTransition(formatPane);
            }
            pdf.setSelected(!newValue);
        });
        
        pdf.selectedProperty().addListener((observable, oldValue, newValue) -> {
            formatPane.setVisible(!newValue);
            image.setSelected(!newValue);
        });
        
        transparentColor.selectedProperty().addListener((observable, oldValue, newValue) -> {
            background.setDisable(newValue);
        });
        
        
    }    
    
    @FXML
    void chooseFolder(ActionEvent event) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Output Directory");
            if(!folderText.getText().isEmpty()){
                chooser.setInitialDirectory( new File(folderText.getText()) );
            }
            File file = chooser.showDialog((Window)getStage(event));
            if(file!=null && file.isDirectory()) {
                 folderText.setText( file.getAbsolutePath());
            }
    }

    @FXML
    public void close(MouseEvent event) {
        this.getStage(event).close();
    }
    
    public Stage getStage(ActionEvent e){
        Node tmp = (Node)e.getSource();
        return (Stage)tmp.getScene().getWindow();
    }
    
    public Stage getStage(MouseEvent e){
        Node tmp = (Node)e.getSource();
        return (Stage)tmp.getScene().getWindow();
    }
        
    public String getFilename(){
        return (filename.getText().isEmpty()?DEFAULT_FILE_NAME:filename.getText());
    }
    
    public String getOutputPath(){
        return folderText.getText()+"\\"+getFilename()+"."+format.getValue();
    }
    
    @FXML
    void generate(ActionEvent event) {
        
        if(text.getText().isEmpty()){
            message.setText("\t  No text to encode. \n Fill the text field and try again");
        }else if( !pdf.isSelected() && !image.isSelected()){
            message.setText("\t\tNo output Format.\nSpecify the output Format and try again");
        }
        else if(folderText.getText().isEmpty()){
            message.setText("\t\tNo output Folder.\nSpecify the output Folder and try again");
        }else if(filename.getText().isEmpty()){
            message.setText("\t\tNo output file name.\nSpecify the output file name and try again");
        }else{
            if(pdf.isSelected()) message.setText("Complete version of this app is needed to export as PDF");
            else if(format.getValue().equalsIgnoreCase("svg")){
                 message.setText("Complete version of this app is needed to export as SVG");
            }else{
                message.setText("Generating QRCode ...");
               /* pop.getContent().add(popupPane);
                pop.show(getStage(event));*/
                Thread thread =  new Thread(() -> {
                    saveQrCodeAsImage();
                });
                thread.start();
            }
        }
        
        popupPane.setVisible(true);
        launchTransition(popupPane);
    }
    
    public void saveQrCodeAsImage(){
        System.out.println(">>Generating Barcode:"+getOutputPath());
        BarcodeQRCode code = new BarcodeQRCode(text.getText(),Integer.valueOf(width.getText()),Integer.valueOf(height.getText()),null);
        javafx.scene.paint.Color fore = foreground.getValue();
        Color f = new Color((float)fore.getRed(),(float)fore.getGreen(),(float)fore.getBlue());
        Color b= null;
        
        if(!transparentColor.isSelected()){
            javafx.scene.paint.Color back =  background.getValue();
            b = new Color((float)back.getRed(),(float)back.getGreen(),(float)back.getBlue());
        }else{
            b= new Color(0,0,0,0);
        }
        
        java.awt.Image img = code.createAwtImage(f,b);
        BufferedImage bImage;
        bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TRANSLUCENT);
        Graphics2D g = bImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        //g.setBackground(new Color(0,0,0,0));
        g.dispose();

        try {
            ImageIO.write(bImage, format.getValue(), new File(getOutputPath()));
            
            Platform.runLater(() -> {
                message.setText("QrCode generated successfully at \n"+getOutputPath());
            });
            
        } catch (IOException ex) {
           System.out.println(">>Error occured : Line 253 -- ViewController > saveQrCodeAsImage() ---");
           
            Platform.runLater(() -> {
                message.setText("An error occur. Contact we at https://github.com/script-0/QR-Code-Generator");
            });
        }
        
        System.out.println(">>Code successful generated");
    }
    
    @FXML
    void openLink(ActionEvent event) {
        try {
            Desktop.getDesktop().mail(new URI("mailto:?to=0.my.script.1@gmail.com&cc=lab.script0@gmail.com&subject=QrCodeX User&body=I want to get complete version Of QrCodeX.".replace(" ", "%20")));
        } catch (IOException | URISyntaxException ex) {
            System.out.println("Loading email failed");
        }
    }
    
    @FXML
    void closePopup() {
        Platform.runLater(() -> {
               /* pop.hide();
                pop.getContent().clear();*/
               closeTransition(popupPane);
               popupPane.setVisible(false);
        });
    }    
    
    public void launchTransition( Node b){
    ScaleTransition st = new ScaleTransition(Duration.seconds(.1), b);
	st.setFromX(.8);
	st.setFromY(.8);
	st.setToX(1);
	st.setToY(1);
	
	
	FadeTransition ft = new FadeTransition(Duration.seconds(.1), b);
	ft.setFromValue(.2);
	ft.setToValue(1);
        
        
        st.play();
	ft.play();	
    
    }
    
    public void closeTransition( Node b){
    ScaleTransition st = new ScaleTransition(Duration.seconds(.1), b);
	st.setFromX(1);
	st.setFromY(1);
	st.setToX(0);
	st.setToY(0);
	
	
	FadeTransition ft = new FadeTransition(Duration.seconds(.1), b);
	ft.setFromValue(1);
	ft.setToValue(0);
        
        
        st.play();
	ft.play();	
    
    }
    
}
