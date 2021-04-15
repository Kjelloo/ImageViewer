package dk.easv;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class ImageViewerWindowController implements Initializable {
    public static final List<Image> images = new ArrayList<>();
    public static final List<File> files = new ArrayList<>();
    public static int currentImageIndex = 0;
    private long showtime;
    private boolean isSlideshowActive;

    @FXML
    private Button slideshowBtn;

    @FXML
    private Slider slideshowSlider;

    @FXML
    Parent root;

    @FXML
    public ImageView imageView;
    @FXML
    private Label fileLabel;
    @FXML
    public Label rgbLabel;

    private Object lock = new Object();

    private Object lockPixels = new Object();



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isSlideshowActive = true;
        showtime = (long) slideshowSlider.getValue();

        slideshowSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                showtime = (long) slideshowSlider.getValue();
            }
        });

    }

    @FXML
    private void handleBtnLoadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
        this.files.addAll(files);
        if (!files.isEmpty()) {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
            });
            displayImage();
        }
    }

    @FXML
    public void handleBtnNextAction() {
        displayNextImage();
    }

    @FXML
    private void handleBtnPreviousAction() {
        displayPreviousImage();
    }

    public void displayNextImage() {
        if (!images.isEmpty()) {
            currentImageIndex = (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

    private void displayPreviousImage() {
        if (!images.isEmpty()) {
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    public void displayImage() {
        if (!images.isEmpty()) {
            imageView.setImage(images.get(currentImageIndex));
            fileLabel.setText("File: " + files.get(currentImageIndex).getName());
            getPixelColor(images.get(currentImageIndex));
        }
    }

    @FXML
    private void handleBtnSlideshow(ActionEvent actionEvent) {

        isSlideshowActive = !isSlideshowActive;

        if (!images.isEmpty()) {
            Thread thread = new Thread(() -> {
                synchronized (lock) {
                    while(!isSlideshowActive) {
                        Platform.runLater(() -> displayNextImage());
                        try {
                            TimeUnit.SECONDS.sleep(showtime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.setDaemon(true);

            if (!isSlideshowActive) {
                slideshowBtn.setText("Stop slideshow");
                if(!thread.isAlive())
                    thread.start();

            } else {
                slideshowBtn.setText("Start slideshow");
                System.out.println("stop");
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else {
            System.out.println("Image list is empty!");
        }
    }

    public void getPixelColor(Image image) {
        AtomicInteger red = new AtomicInteger();
        AtomicInteger green = new AtomicInteger();
        AtomicInteger blue = new AtomicInteger();
        AtomicInteger mixed = new AtomicInteger();

        Thread thread = new Thread(() -> {
            synchronized (lockPixels) {
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        int pixels = image.getPixelReader().getArgb(j, i);
                        Color color = new Color(pixels, true);

                        if(color.getRed() > color.getGreen() && color.getRed() > color.getBlue()) {
                            red.set(red.get() + 1);
                        } else if(color.getGreen() > color.getRed() && color.getGreen() > color.getBlue()) {
                            green.set(green.get() + 1);
                        } else if (color.getBlue() > color.getRed() && color.getBlue() > color.getGreen()) {
                            blue.set(blue.get() + 1);
                        } else {
                            mixed.set(mixed.get() + 1);
                        }

                    }
                }
                Platform.runLater(() -> {
                    setRgbLabel(new RGB(red.get(), green.get(), blue.get(), mixed.get()));
                });
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void setRgbLabel(RGB rgb) {
        rgbLabel.setText(rgb.toString());
    }
}